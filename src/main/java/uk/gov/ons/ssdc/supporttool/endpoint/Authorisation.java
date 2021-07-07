package uk.gov.ons.ssdc.supporttool.endpoint;

import static uk.gov.ons.ssdc.supporttool.model.entity.UserGroupAuthorisedActivityType.SUPER_USER;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.ons.ssdc.supporttool.model.entity.User;
import uk.gov.ons.ssdc.supporttool.model.entity.UserGroupAuthorisedActivityType;
import uk.gov.ons.ssdc.supporttool.model.entity.UserGroupMember;
import uk.gov.ons.ssdc.supporttool.model.entity.UserGroupPermission;
import uk.gov.ons.ssdc.supporttool.model.repository.UserRepository;
import uk.gov.ons.ssdc.supporttool.security.UserIdentity;

@RestController
@RequestMapping(value = "/auth")
public class Authorisation {
  private final UserIdentity userIdentity;
  private final UserRepository userRepository;

  public Authorisation(UserIdentity userIdentity, UserRepository userRepository) {
    this.userIdentity = userIdentity;
    this.userRepository = userRepository;
  }

  @GetMapping
  public Set<UserGroupAuthorisedActivityType> getAuthorisedActivities(
      @RequestHeader(required = false, value = "x-goog-iap-jwt-assertion") String jwtToken,
      @RequestParam(required = false, value = "surveyId") Optional<UUID> surveyId) {
    String userEmail = userIdentity.getUserEmail(jwtToken);

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
        if (permission.getAuthorisedActivity() == SUPER_USER && (permission.getSurvey() == null
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
