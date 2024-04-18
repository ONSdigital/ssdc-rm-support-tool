package uk.gov.ons.ssdc.supporttool.endpoint;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.ons.ssdc.common.model.entity.UserGroupAuthorisedActivityType;
import uk.gov.ons.ssdc.supporttool.model.repository.UserRepository;
import uk.gov.ons.ssdc.supporttool.security.AuthUser;

@RestController
@RequestMapping(value = "/api/auth")
public class AuthorisationEndpoint {
  private final UserRepository userRepository;

  private final AuthUser authUser;

  public AuthorisationEndpoint(UserRepository userRepository, AuthUser authUser) {
    this.userRepository = userRepository;
    this.authUser = authUser;
  }

  @GetMapping
  public Set<UserGroupAuthorisedActivityType> getAuthorisedActivities(
      @RequestParam(required = false, value = "surveyId") Optional<UUID> surveyId,
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {

    return authUser.getUserGroupPermission(userRepository, surveyId, userEmail);
  }
}
