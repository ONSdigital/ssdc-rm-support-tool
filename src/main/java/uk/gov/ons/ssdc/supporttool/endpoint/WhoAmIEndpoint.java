package uk.gov.ons.ssdc.supporttool.endpoint;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/whoami")
public class WhoAmIEndpoint {
  @GetMapping
  public Map<String, String> getWhoIAm(
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {
    return Map.of("user", userEmail);
  }
}
