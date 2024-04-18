package uk.gov.ons.ssdc.supporttool.security;

import static uk.gov.ons.ssdc.common.model.entity.UserGroupAuthorisedActivityType.SUPER_USER;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.google.api.client.json.webtoken.JsonWebToken;
import com.google.auth.oauth2.TokenVerifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.ons.ssdc.common.model.entity.User;
import uk.gov.ons.ssdc.common.model.entity.UserGroupAuthorisedActivityType;
import uk.gov.ons.ssdc.common.model.entity.UserGroupMember;
import uk.gov.ons.ssdc.common.model.entity.UserGroupPermission;
import uk.gov.ons.ssdc.supporttool.model.repository.UserRepository;

@Component
@ConditionalOnProperty(name = "iap-enabled", havingValue = "true", matchIfMissing = true)
public class IAPUser implements AuthUser {
  private static final Logger log = LoggerFactory.getLogger(IAPUser.class);

  public IAPUser() {}

  @Override
  public Set<UserGroupAuthorisedActivityType> getUserGroupPermission(
      UserRepository userRepository, Optional<UUID> surveyId, String userEmail) {
    User user = getUser(userRepository, userEmail);

    Set<UserGroupAuthorisedActivityType> result = new HashSet<>();
    for (UserGroupMember groupMember : user.getMemberOf()) {
      for (UserGroupPermission permission : groupMember.getGroup().getPermissions()) {
        if (permission.getAuthorisedActivity() == SUPER_USER
            && (permission.getSurvey() == null
                || (surveyId.isPresent()
                    && permission.getSurvey().getId().equals(surveyId.get())))) {
          if (permission.getSurvey() == null) {
            // User is a global super user so give ALL permissions
            return Set.of(UserGroupAuthorisedActivityType.values());
          } else {
            // User is a super user ONLY ON ONE SPECIFIC SURVEY so just give non-global permissions
            result.addAll(
                Arrays.stream(UserGroupAuthorisedActivityType.values())
                    .filter(activityType -> !activityType.isGlobal())
                    .collect(Collectors.toSet()));
          }
        } else if (permission.getSurvey() != null
            && surveyId.isPresent()
            && permission.getSurvey().getId().equals(surveyId.get())) {
          // The user has permission on a specific survey, so we can include it
          result.add(permission.getAuthorisedActivity());
        } else if (permission.getSurvey() == null) {
          // The user has permission on ALL surveys - global permission - so we can include it
          result.add(permission.getAuthorisedActivity());
        }
      }
    }

    return result;
  }

  @Override
  public void checkUserPermission(
      UserRepository userRepository,
      UUID surveyId,
      String userEmail,
      UserGroupAuthorisedActivityType activity) {
    User user = getUser(userRepository, userEmail);

    for (UserGroupMember groupMember : user.getMemberOf()) {
      for (UserGroupPermission permission : groupMember.getGroup().getPermissions()) {
        // SUPER USER without a survey = GLOBAL super user (all permissions)
        if ((permission.getAuthorisedActivity() == UserGroupAuthorisedActivityType.SUPER_USER
                && permission.getSurvey() == null)
            // SUPER USER with a survey = super user only on the specified survey
            || (permission.getAuthorisedActivity() == UserGroupAuthorisedActivityType.SUPER_USER
                    && permission.getSurvey() != null
                    && permission.getSurvey().getId().equals(surveyId)
                // Otherwise, user must have specific activity/survey combo to be authorised
                || (permission.getAuthorisedActivity() == activity
                    && (permission.getSurvey() == null
                        || (permission.getSurvey() != null
                            && permission.getSurvey().getId().equals(surveyId)))))) {
          return; // User is authorised
        }
      }
    }
  }

  @Override
  public void checkGlobalUserPermission(
      UserRepository userRepository, String userEmail, UserGroupAuthorisedActivityType activity) {
    User user = getUser(userRepository, userEmail);

    for (UserGroupMember groupMember : user.getMemberOf()) {
      for (UserGroupPermission permission : groupMember.getGroup().getPermissions()) {
        // SUPER USER without a survey = GLOBAL super user (all permissions)
        if ((permission.getAuthorisedActivity() == UserGroupAuthorisedActivityType.SUPER_USER
                && permission.getSurvey() == null)
            // Otherwise, user must have specific activity to be authorised
            || (permission.getAuthorisedActivity() == activity)) {
          return; // User is authorised
        }
      }
    }

    log.with("userEmail", userEmail)
        .with("activity", activity)
        .with("httpStatus", HttpStatus.FORBIDDEN)
        .warn("User not authorised for attempted activity");
    throw new ResponseStatusException(
        HttpStatus.FORBIDDEN,
        String.format("User not authorised for activity %s", activity.name()));
  }

  @Override
  public String getUserEmail(
      UserRepository userRepository, TokenVerifier tokenVerifier, String jwtToken) {
    if (!StringUtils.hasText(jwtToken)) {
      // This request must have come from __inside__ the firewall/cluster, and should not be allowed
      log.with("httpStatus", HttpStatus.FORBIDDEN)
          .warn("Requests bypassing IAP are strictly forbidden");
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, String.format("Requests bypassing IAP are strictly forbidden"));
    } else {
      return verifyJwtAndGetEmail(jwtToken, tokenVerifier);
    }
  }

  private User getUser(UserRepository userRepository, String userEmail) {
    Optional<User> userOpt = userRepository.findByEmailIgnoreCase(userEmail);

    if (userOpt.isEmpty()) {
      log.with("userEmail", userEmail)
          .with("httpStatus", HttpStatus.FORBIDDEN)
          .warn("User not known to RM");
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User not known to RM");
    }

    return userOpt.get();
  }

  private String verifyJwtAndGetEmail(String jwtToken, TokenVerifier tokenVerifier) {
    try {
      JsonWebToken jsonWebToken = tokenVerifier.verify(jwtToken);

      // Verify that the token contain subject and email claims
      JsonWebToken.Payload payload = jsonWebToken.getPayload();
      if (payload.getSubject() != null && payload.get("email") != null) {
        return (String) payload.get("email");
      } else {
        return null;
      }
    } catch (TokenVerifier.VerificationException e) {
      throw new RuntimeException(e);
    }
  }
}
