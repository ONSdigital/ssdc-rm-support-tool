package uk.gov.ons.ssdc.supporttool.security;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uk.gov.ons.ssdc.common.model.entity.UserGroupAuthorisedActivityType;
import uk.gov.ons.ssdc.supporttool.model.repository.UserRepository;

@Component
@ConditionalOnProperty(name = "iap-enforced", havingValue = "false")
public class DummyUser implements AuthUser {
  private static final Logger log = LoggerFactory.getLogger(DummyUser.class);
  private final IAPUser iapUser;

  private final String dummyUserIdentity;

  private final String dummySuperUserIdentity;

  public DummyUser(
      UserRepository userRepository,
      @Value("${dummyuseridentity}") String dummyUserIdentity,
      @Value("${dummysuperuseridentity}") String dummySuperUserIdentity) {
    log.error("*** SECURITY ALERT *** IF YOU SEE THIS IN PRODUCTION, SHUT DOWN IMMEDIATELY!!!");
    this.dummySuperUserIdentity = dummySuperUserIdentity;
    this.dummyUserIdentity = dummyUserIdentity;
    // Can't use spring injection due to ConditionOnProperty
    this.iapUser = new IAPUser(userRepository);
  }

  @Override
  public Set<UserGroupAuthorisedActivityType> getUserGroupPermission(
      Optional<UUID> surveyId, String userEmail) {
    if (isDummyUser(userEmail)) {
      return Set.of(UserGroupAuthorisedActivityType.values());
    }
    // If user isn't the dummy user, it should be treated as an IAPUser
    return iapUser.getUserGroupPermission(surveyId, userEmail);
  }

  @Override
  public void checkUserPermission(
      String userEmail, UUID surveyId, UserGroupAuthorisedActivityType activity) {
    // If user isn't the dummy user, it should be treated as an IAPUser
    if (!isDummyUser(userEmail)) {
      iapUser.checkUserPermission(userEmail, surveyId, activity);
    }
  }

  @Override
  public void checkGlobalUserPermission(
      String userEmail, UserGroupAuthorisedActivityType activity) {
    // If user isn't the dummy user, it should be treated as an IAPUser
    if (!isDummyUser(userEmail)) {
      iapUser.checkGlobalUserPermission(userEmail, activity);
    }
  }

  @Override
  public String getUserEmail(String jwtToken) {
    if (StringUtils.hasText(jwtToken)) {
      // If there is a token, we should get the user email for that token and not the dummy
      return iapUser.verifyJwtAndGetEmail(jwtToken);
    }
    return dummyUserIdentity;
  }

  /**
   * Checks if the user is a DummyUser
   *
   * @param userEmail Email to check
   * @return If email is Dummy's email
   */
  private boolean isDummyUser(String userEmail) {
    return userEmail.equalsIgnoreCase(dummySuperUserIdentity);
  }
}
