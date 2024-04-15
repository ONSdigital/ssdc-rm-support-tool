package uk.gov.ons.ssdc.supporttool.security;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.ons.ssdc.common.model.entity.UserGroupAuthorisedActivityType;

public class DummyUser implements AuthUser {
  private static final Logger log = LoggerFactory.getLogger(DummyUser.class);

  @Value("${dummyuseridentity}")
  private static String dummyUserIdentity;

  @Value("${dummysuperuseridentity}")
  private static String dummySuperUserIdentity;

  private String userEmail;

  public DummyUser(String userEmail) {
    log.error("*** SECURITY ALERT *** IF YOU SEE THIS IN PRODUCTION, SHUT DOWN IMMEDIATELY!!!");
    this.userEmail = userEmail;
  }

  @Override
  public Set<UserGroupAuthorisedActivityType> getUserGroupPermission() {

    if (isEmailValid(userEmail)) {
      return Set.of(UserGroupAuthorisedActivityType.values());
    }
    log.with("httpStatus", HttpStatus.FORBIDDEN)
        .with("userEmail", userEmail)
        .warn("Failed to get authorised activities, User not known to RM");
    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User not known to RM");
  }

  private boolean isEmailValid(String userEmail) {
    return userEmail.equalsIgnoreCase(dummySuperUserIdentity);
  }
}
