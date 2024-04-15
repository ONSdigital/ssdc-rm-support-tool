package uk.gov.ons.ssdc.supporttool.security;

import static uk.gov.ons.ssdc.common.model.entity.UserGroupAuthorisedActivityType.SUPER_USER;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.ons.ssdc.common.model.entity.User;
import uk.gov.ons.ssdc.common.model.entity.UserGroupAuthorisedActivityType;
import uk.gov.ons.ssdc.common.model.entity.UserGroupMember;
import uk.gov.ons.ssdc.common.model.entity.UserGroupPermission;
import uk.gov.ons.ssdc.supporttool.model.repository.UserRepository;

public class IAPUser implements AuthUser {
  private static final Logger log = LoggerFactory.getLogger(IAPUser.class);

  private UserRepository userRepository;
  private Optional<UUID> surveyId;
  private String userEmail;

  public IAPUser(UserRepository userRepository, Optional<UUID> surveyId, String userEmail) {
    this.userRepository = userRepository;
    this.surveyId = surveyId;
    this.userEmail = userEmail;
  }

  @Override
  public Set<UserGroupAuthorisedActivityType> getUserGroupPermission() {
    Optional<User> userOpt = getUser(userRepository, userEmail);

    if (userOpt.isEmpty()) {
      log.with("httpStatus", HttpStatus.FORBIDDEN)
          .with("userEmail", userEmail)
          .warn("Failed to get authorised activities, User not known to RM");
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User not known to RM");
    }

    User user = userOpt.get();

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

  private Optional<User> getUser(UserRepository userRepository, String userEmail) {
    return userRepository.findByEmailIgnoreCase(userEmail);
  }
}
