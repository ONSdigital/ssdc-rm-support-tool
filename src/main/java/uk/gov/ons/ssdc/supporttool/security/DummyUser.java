package uk.gov.ons.ssdc.supporttool.security;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DummyUser {
  private static final Logger log = LoggerFactory.getLogger(DummyUser.class);

  @Value("${dummyuseridentity-allowed}")
  private boolean dummyUserIdentityAllowed;

  @Value("${dummyuseridentity}")
  private String dummyUserIdentity;

  @Value("${dummysuperuseridentity}")
  private String dummySuperUserIdentity;

  public DummyUser() {
    if (dummyUserIdentityAllowed) {
      log.error("*** SECURITY ALERT *** IF YOU SEE THIS IN PRODUCTION, SHUT DOWN IMMEDIATELY!!!");
    }
  }

  public boolean isDummyUserAllowedAndDoesEmailMatch(String userEmail) {
    return dummyUserIdentityAllowed && userEmail.equalsIgnoreCase(dummySuperUserIdentity);
  }

  public Optional<String> getDummyUserIfAllowed() {
    if (dummyUserIdentityAllowed) {
      return Optional.of(dummyUserIdentity);
    }

    return Optional.empty();
  }

  public boolean isDummyUserIdentityAllowed() {
    return dummyUserIdentityAllowed;
  }
}
