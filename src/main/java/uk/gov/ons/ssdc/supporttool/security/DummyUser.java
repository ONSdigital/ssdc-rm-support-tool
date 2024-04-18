package uk.gov.ons.ssdc.supporttool.security;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.google.auth.oauth2.TokenVerifier;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import uk.gov.ons.ssdc.common.model.entity.UserGroupAuthorisedActivityType;
import uk.gov.ons.ssdc.supporttool.model.repository.UserRepository;

@Component
@ConditionalOnProperty(name = "dummyuseridentity-allowed", havingValue = "true")
public class DummyUser extends IAPUser {
  private static final Logger log = LoggerFactory.getLogger(DummyUser.class);

  @Value("${dummyuseridentity}")
  private String dummyUserIdentity;

  @Value("${dummysuperuseridentity}")
  private String dummySuperUserIdentity;

  public DummyUser() {
    log.error("*** SECURITY ALERT *** IF YOU SEE THIS IN PRODUCTION, SHUT DOWN IMMEDIATELY!!!");
  }

  @Override
  public Set<UserGroupAuthorisedActivityType> getUserGroupPermission(
      UserRepository userRepository, Optional<UUID> surveyId, String userEmail) {

    if (isEmailValid(userEmail)) {
      return Set.of(UserGroupAuthorisedActivityType.values());
    }
    return super.getUserGroupPermission(userRepository, surveyId, userEmail);
  }

  @Override
  public void checkUserPermission(
      UserRepository userRepository,
      UUID surveyId,
      String userEmail,
      UserGroupAuthorisedActivityType activity) {
    if (!isEmailValid(userEmail)) {
      super.checkUserPermission(userRepository, surveyId, userEmail, activity);
    }
  }

  @Override
  public void checkGlobalUserPermission(
      UserRepository userRepository, String userEmail, UserGroupAuthorisedActivityType activity) {
    if (!isEmailValid(userEmail)) {
      super.checkGlobalUserPermission(userRepository, userEmail, activity);
    }
  }

  @Override
  public String getUserEmail(
      UserRepository userRepository, TokenVerifier tokenVerifier, String jwtToken) {
    return dummyUserIdentity;
  }

  private boolean isEmailValid(String userEmail) {
    return userEmail.equalsIgnoreCase(dummySuperUserIdentity);
  }
}
