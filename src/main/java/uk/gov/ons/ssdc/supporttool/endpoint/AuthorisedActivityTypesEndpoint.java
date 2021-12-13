package uk.gov.ons.ssdc.supporttool.endpoint;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.ons.ssdc.common.model.entity.UserGroupAuthorisedActivityType;

@RestController
@RequestMapping(value = "/api/authorisedActivityTypes")
public class AuthorisedActivityTypesEndpoint {
  private static final Set<UserGroupAuthorisedActivityType> ALL_AUTHORISED_ACTIVITY_TYPES =
      Set.of(UserGroupAuthorisedActivityType.values());

  private static final Set<UserGroupAuthorisedActivityType> GLOBAL_AUTHORISED_ACTIVITY_TYPES =
      Arrays.stream(UserGroupAuthorisedActivityType.values())
          .filter(UserGroupAuthorisedActivityType::isGlobal)
          .collect(Collectors.toSet());

  @GetMapping
  Set<UserGroupAuthorisedActivityType> getAuthorisedActivities(
      @RequestParam(value = "globalOnly", required = false, defaultValue = "false")
          boolean globalOnly) {
    if (globalOnly) {
      return GLOBAL_AUTHORISED_ACTIVITY_TYPES;
    } else {
      return ALL_AUTHORISED_ACTIVITY_TYPES;
    }
  }
}
