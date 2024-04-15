package uk.gov.ons.ssdc.supporttool.endpoint;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
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
import uk.gov.ons.ssdc.supporttool.security.DummyUser;
import uk.gov.ons.ssdc.supporttool.security.IAPUser;

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

    AuthUser user;
    if (dummyUserIdentityAllowed) {
      user = new DummyUser(userEmail);
    } else {
      user = new IAPUser(userRepository, surveyId, userEmail);
    }

    return user.getUserGroupPermission();
  }
}
