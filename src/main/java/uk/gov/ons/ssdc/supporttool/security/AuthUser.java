package uk.gov.ons.ssdc.supporttool.security;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import uk.gov.ons.ssdc.common.model.entity.UserGroupAuthorisedActivityType;

public interface AuthUser {
  public Set<UserGroupAuthorisedActivityType> getUserGroupPermission(
      Optional<UUID> surveyId, String userEmail);

  public void checkUserPermission(
      String userEmail, UUID surveyId, UserGroupAuthorisedActivityType activity);

  public void checkGlobalUserPermission(String userEmail, UserGroupAuthorisedActivityType activity);

  public String getUserEmail(String jwtToken);
}
