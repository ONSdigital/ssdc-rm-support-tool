package uk.gov.ons.ssdc.supporttool.endpoint;

import static uk.gov.ons.ssdc.common.model.entity.UserGroupAuthorisedActivityType.SUPER_USER;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.ons.ssdc.common.model.entity.User;
import uk.gov.ons.ssdc.common.model.entity.UserGroupAuthorisedActivityType;
import uk.gov.ons.ssdc.common.model.entity.UserGroupMember;
import uk.gov.ons.ssdc.common.model.entity.UserGroupPermission;
import uk.gov.ons.ssdc.supporttool.model.repository.UserRepository;

@RestController
@RequestMapping(value = "/api/auth")
public class AuthorisationEndpoint {
  private static final Logger log = LoggerFactory.getLogger(AuthorisationEndpoint.class);

  private final UserRepository userRepository;
  private final boolean dummyUserIdentityAllowed;
  private final String dummySuperUserIdentity;

  public AuthorisationEndpoint(
      UserRepository userRepository,
      @Value("${dummyuseridentity-allowed}") boolean dummyUserIdentityAllowed,
      @Value("${dummysuperuseridentity}") String dummySuperUserIdentity) {
    this.userRepository = userRepository;
    this.dummyUserIdentityAllowed = dummyUserIdentityAllowed;
    this.dummySuperUserIdentity = dummySuperUserIdentity;

    if (dummyUserIdentityAllowed) {
      log.error("*** SECURITY ALERT *** IF YOU SEE THIS IN PRODUCTION, SHUT DOWN IMMEDIATELY!!!");
    }
  }

  @GetMapping
  public Set<UserGroupAuthorisedActivityType> getAuthorisedActivities(
      @RequestParam(required = false, value = "surveyId") Optional<UUID> surveyId,
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {

    if (dummyUserIdentityAllowed && userEmail.equals(dummySuperUserIdentity)) {
      // Dummy test super user is fully authorised, bypassing all security
      // This is **STRICTLY** for ease of dev/testing in non-production environments
      return Set.of(UserGroupAuthorisedActivityType.values());
    }

    Optional<User> userOpt = userRepository.findByEmailIgnoreCase(userEmail);

    if (!userOpt.isPresent()) {
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
}
