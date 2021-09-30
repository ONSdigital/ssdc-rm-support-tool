package uk.gov.ons.ssdc.supporttool.endpoint;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.ons.ssdc.common.model.entity.UserGroupAuthorisedActivityType;
import uk.gov.ons.ssdc.supporttool.testhelper.IntegrationTestHelper;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class AllEndpointsIT {
  @Autowired private IntegrationTestHelper integrationTestHelper;

  @LocalServerPort private int port;

  @Test
  public void testAllTheEndpoints() {
    integrationTestHelper.testGet(
        port,
        UserGroupAuthorisedActivityType.LIST_ACTION_RULES,
        (bundle) -> String.format("actionRules/?collectionExercise=%s", bundle.getCollexId()));
  }
}
