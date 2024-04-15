package uk.gov.ons.ssdc.supporttool.security;

import java.util.Set;
import uk.gov.ons.ssdc.common.model.entity.UserGroupAuthorisedActivityType;

public interface AuthUser {
  public Set<UserGroupAuthorisedActivityType> getUserGroupPermission();

  public void checkUserPermission();

  public void checkGlobalUserPermission();

  public String getUserEmail();
}
