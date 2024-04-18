package uk.gov.ons.ssdc.supporttool.security;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.google.auth.oauth2.TokenVerifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.ons.ssdc.common.model.entity.Survey;
import uk.gov.ons.ssdc.common.model.entity.UserGroupAuthorisedActivityType;
import uk.gov.ons.ssdc.supporttool.model.repository.UserRepository;

@Component
public class UserIdentity {
  private static final Logger log = LoggerFactory.getLogger(UserIdentity.class);
  private static final String IAP_ISSUER_URL = "https://cloud.google.com/iap";

  private final UserRepository userRepository;
  private final String iapAudience;
  private final boolean dummyUserIdentityAllowed;
  private final String dummyUserIdentity;
  private final String dummySuperUserIdentity;

  private TokenVerifier tokenVerifier = null;

  @Autowired private AuthUser authUser;

  public UserIdentity(
      UserRepository userRepository,
      @Value("${iapaudience}") String iapAudience,
      @Value("${dummyuseridentity-allowed}") boolean dummyUserIdentityAllowed,
      @Value("${dummyuseridentity}") String dummyUserIdentity,
      @Value("${dummysuperuseridentity}") String dummySuperUserIdentity) {
    this.userRepository = userRepository;
    this.iapAudience = iapAudience;
    this.dummyUserIdentityAllowed = dummyUserIdentityAllowed;
    this.dummyUserIdentity = dummyUserIdentity;
    this.dummySuperUserIdentity = dummySuperUserIdentity;

    if (dummyUserIdentityAllowed) {
      log.error("*** SECURITY ALERT *** IF YOU SEE THIS IN PRODUCTION, SHUT DOWN IMMEDIATELY!!!");
    }
  }

  public void checkUserPermission(
      String userEmail, Survey survey, UserGroupAuthorisedActivityType activity) {
    authUser.checkUserPermission(userRepository, survey.getId(), userEmail, activity);
  }

  public void checkGlobalUserPermission(
      String userEmail, UserGroupAuthorisedActivityType activity) {
    authUser.checkGlobalUserPermission(userRepository, userEmail, activity);
  }

  public String getUserEmail(String jwtToken) {
    return authUser.getUserEmail(userRepository, getTokenVerifier(), jwtToken);
  }

  private synchronized TokenVerifier getTokenVerifier() {

    if (tokenVerifier == null) {
      tokenVerifier =
          TokenVerifier.newBuilder().setAudience(iapAudience).setIssuer(IAP_ISSUER_URL).build();
    }

    return tokenVerifier;
  }
}
