package uk.gov.ons.ssdc.supporttool.endpoint;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class WhoAmIEndpointIT {

  @LocalServerPort private int port;

  @Test
  public void testWhoAmI() {
    RestTemplate restTemplate = new RestTemplate();
    String url = "http://localhost:" + port + "/api/whoami";
    ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
    assertThat(response.getBody().get("user")).isEqualTo("dummy@fake-email.com");
  }
}
