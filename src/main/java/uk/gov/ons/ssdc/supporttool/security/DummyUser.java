package uk.gov.ons.ssdc.supporttool.security;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.google.auth.oauth2.TokenVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.ons.ssdc.common.model.entity.UserGroupAuthorisedActivityType;
import uk.gov.ons.ssdc.supporttool.model.repository.UserRepository;

@Component
@ConditionalOnProperty(name = "dummyuseridentity-allowed", havingValue = "true")
public class DummyUser implements AuthUser {
  private static final Logger log = LoggerFactory.getLogger(DummyUser.class);

  @Value("${dummyuseridentity}")
  private String dummyUserIdentity;

  @Value("${dummysuperuseridentity}")
  private String dummySuperUserIdentity;


  public DummyUser() {
    log.error("*** SECURITY ALERT *** IF YOU SEE THIS IN PRODUCTION, SHUT DOWN IMMEDIATELY!!!");
  }

  @Override
  public Set<UserGroupAuthorisedActivityType> getUserGroupPermission(UserRepository userRepository, Optional<UUID> surveyId, String userEmail) {

    if (isEmailValid(userEmail)) {
      return Set.of(UserGroupAuthorisedActivityType.values());
    }
    log.with("httpStatus", HttpStatus.FORBIDDEN)
        .with("userEmail", userEmail)
        .warn("Failed to get authorised activities, User not known to RM");
    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User not known to RM");
  }

  @Override
  public void checkUserPermission(UserRepository userRepository,
                                  UUID surveyId,
                                  String userEmail,
                                  UserGroupAuthorisedActivityType activity) {
    checkGlobalUserPermission(userRepository, userEmail, activity);
  }

  @Override
  public void checkGlobalUserPermission(UserRepository userRepository, String userEmail, UserGroupAuthorisedActivityType activity) {
    if (!isEmailValid(userEmail)) {
      log.with("userEmail", userEmail)
          .with("httpStatus", HttpStatus.FORBIDDEN)
          .warn("User not known to RM");
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User not known to RM");
    }
  }

  @Override
  public String getUserEmail(UserRepository userRepository, TokenVerifier tokenVerifier, String jwtToken) {
    return dummyUserIdentity;
  }

  private boolean isEmailValid(String userEmail) {
    log.with("userEmail: ", userEmail).with("dumyIdentity: ", dummySuperUserIdentity).info("here");
    return userEmail.equalsIgnoreCase(dummySuperUserIdentity);
  }
}
