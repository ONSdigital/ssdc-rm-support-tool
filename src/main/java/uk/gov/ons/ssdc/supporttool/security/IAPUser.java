package uk.gov.ons.ssdc.supporttool.security;

import static uk.gov.ons.ssdc.common.model.entity.UserGroupAuthorisedActivityType.SUPER_USER;

import com.google.api.client.json.webtoken.JsonWebToken;
import com.google.auth.oauth2.TokenVerifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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
@ConditionalOnProperty(name = "iap-enforced", havingValue = "true", matchIfMissing = true)
public class IAPUser implements AuthUser {
  private static final Logger log = LoggerFactory.getLogger(IAPUser.class);

  private final String IAP_ISSUER_URL = "https://cloud.google.com/iap";

  @Value("${iapaudience}")
  private String iapAudience;

  private static TokenVerifier tokenVerifier = null;

  private final UserRepository userRepository;

  public IAPUser(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public Set<UserGroupAuthorisedActivityType> getUserGroupPermission(
      Optional<UUID> surveyId, String userEmail) {
    User user = getUser(userEmail);

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
      String userEmail, UUID surveyId, UserGroupAuthorisedActivityType activity) {
    User user = getUser(userEmail);

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
    log.atWarn()
        .setMessage("User not authorised for attempted activity")
        .addKeyValue("userEmail", userEmail)
        .addKeyValue("activity", activity)
        .addKeyValue("httpStatus", HttpStatus.FORBIDDEN)
        .log();
    throw new ResponseStatusException(
        HttpStatus.FORBIDDEN,
        String.format("User not authorised for activity %s", activity.name()));
  }

  @Override
  public void checkGlobalUserPermission(
      String userEmail, UserGroupAuthorisedActivityType activity) {
    User user = getUser(userEmail);

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

    log.atWarn()
        .setMessage("User not authorised for attempted activity")
        .addKeyValue("userEmail", userEmail)
        .addKeyValue("activity", activity)
        .addKeyValue("httpStatus", HttpStatus.FORBIDDEN)
        .log();
    throw new ResponseStatusException(
        HttpStatus.FORBIDDEN,
        String.format("User not authorised for activity %s", activity.name()));
  }

  @Override
  public String getUserEmail(String jwtToken) {
    if (!StringUtils.hasText(jwtToken)) {
      // This request must have come from __inside__ the firewall/cluster, and should not be allowed
      log.atWarn()
          .setMessage("Requests bypassing IAP are strictly forbidden")
          .addKeyValue("httpStatus", HttpStatus.FORBIDDEN)
          .log();
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Requests bypassing IAP are strictly forbidden");
    } else {
      return verifyJwtAndGetEmail(jwtToken);
    }
  }

  public String verifyJwtAndGetEmail(String jwtToken) {
    try {
      JsonWebToken jsonWebToken = getTokenVerifier().verify(jwtToken);

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

  private User getUser(String userEmail) {
    Optional<User> userOpt = userRepository.findByEmailIgnoreCase(userEmail);
    if (userOpt.isEmpty()) {
      log.atWarn()
          .addKeyValue("userEmail", userEmail)
          .setMessage("User not known to RM")
          .addKeyValue("httpStatus", HttpStatus.FORBIDDEN)
          .log();
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User not known to RM");
    }

    return userOpt.get();
  }

  private synchronized TokenVerifier getTokenVerifier() {

    if (tokenVerifier == null) {
      tokenVerifier =
          TokenVerifier.newBuilder().setAudience(iapAudience).setIssuer(IAP_ISSUER_URL).build();
    }

    return tokenVerifier;
  }
}
