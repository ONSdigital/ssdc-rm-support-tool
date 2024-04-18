package uk.gov.ons.ssdc.supporttool.security;

import com.google.auth.oauth2.TokenVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.ons.ssdc.common.model.entity.Survey;
import uk.gov.ons.ssdc.common.model.entity.UserGroupAuthorisedActivityType;
import uk.gov.ons.ssdc.supporttool.model.repository.UserRepository;

@Component
public class UserIdentity {
  private static final String IAP_ISSUER_URL = "https://cloud.google.com/iap";

  private final UserRepository userRepository;
  private final String iapAudience;

  private final AuthUser authUser;

  private TokenVerifier tokenVerifier = null;

  public UserIdentity(
      UserRepository userRepository,
      AuthUser authUser,
      @Value("${iapaudience}") String iapAudience) {
    this.userRepository = userRepository;
    this.iapAudience = iapAudience;
    this.authUser = authUser;
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
