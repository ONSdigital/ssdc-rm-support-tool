package uk.gov.ons.ssdc.supporttool.security;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.google.auth.oauth2.TokenVerifier;
import uk.gov.ons.ssdc.common.model.entity.UserGroupAuthorisedActivityType;
import uk.gov.ons.ssdc.supporttool.model.repository.UserRepository;

public interface AuthUser {
  public Set<UserGroupAuthorisedActivityType> getUserGroupPermission(UserRepository userRepository, Optional<UUID> surveyId, String userEmail);

  public void checkUserPermission(UserRepository userRepository,
                                  UUID surveyId,
                                  String userEmail,
                                  UserGroupAuthorisedActivityType activity);

  public void checkGlobalUserPermission(UserRepository userRepository, String userEmail, UserGroupAuthorisedActivityType activity);

  public String getUserEmail(UserRepository userRepository, TokenVerifier tokenVerifier, String jwtToken);
}
