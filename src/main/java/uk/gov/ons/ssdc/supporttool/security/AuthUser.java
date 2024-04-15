package uk.gov.ons.ssdc.supporttool.security;

import java.util.Set;
import uk.gov.ons.ssdc.common.model.entity.UserGroupAuthorisedActivityType;

public interface AuthUser {
  // public boolean checkEmail();

  public Set<UserGroupAuthorisedActivityType> getUserGroupPermission();
}
