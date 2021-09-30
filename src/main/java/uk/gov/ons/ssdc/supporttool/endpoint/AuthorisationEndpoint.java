package uk.gov.ons.ssdc.supporttool.endpoint;

import static uk.gov.ons.ssdc.common.model.entity.UserGroupAuthorisedActivityType.SUPER_USER;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
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
  private final UserRepository userRepository;

  public AuthorisationEndpoint(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @GetMapping
  public Set<UserGroupAuthorisedActivityType> getAuthorisedActivities(
      @RequestParam(required = false, value = "surveyId") Optional<UUID> surveyId,
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {

    // TODO: Remove this before releasing to production!
    if (userEmail.equals("dummy@fake-email.com")) {
      return Set.of(UserGroupAuthorisedActivityType.values());
    }

    Optional<User> userOpt = userRepository.findByEmail(userEmail);

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
          // User is a global super user or super user on the specified survey: give all permissions
          return Set.of(UserGroupAuthorisedActivityType.values());
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