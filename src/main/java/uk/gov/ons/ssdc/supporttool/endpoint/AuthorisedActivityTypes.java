package uk.gov.ons.ssdc.supporttool.endpoint;

import java.util.Set;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.ons.ssdc.supporttool.model.entity.UserGroupAuthorisedActivityType;

@RestController
@RequestMapping(value = "/api/authorisedActivityTypes")
public class AuthorisedActivityTypes {
  private static final Set<UserGroupAuthorisedActivityType> AUTHORISED_ACTIVITY_TYPES = Set.of(UserGroupAuthorisedActivityType.values());

  @GetMapping
  Set<UserGroupAuthorisedActivityType> getAllAuthorisedActivities() {
    return AUTHORISED_ACTIVITY_TYPES;
  }
}
