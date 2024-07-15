package uk.gov.ons.ssdc.supporttool.endpoint;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.ons.ssdc.common.model.entity.UserGroupAuthorisedActivityType;
import uk.gov.ons.ssdc.common.validation.ColumnValidator;
import uk.gov.ons.ssdc.common.validation.Rule;
import uk.gov.ons.ssdc.supporttool.model.dto.messaging.EventDTO;
import uk.gov.ons.ssdc.supporttool.model.dto.ui.SurveyDto;
import uk.gov.ons.ssdc.supporttool.testhelper.IntegrationTestHelper;
import uk.gov.ons.ssdc.supporttool.testhelper.PubsubHelper;
import uk.gov.ons.ssdc.supporttool.testhelper.QueueSpy;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class SurveyEndpointIT {
  private static final String SURVEY_UPDATE_TEST_SUBSCRIPTION = "event_survey-update_it";

  @Autowired private IntegrationTestHelper integrationTestHelper;

  @Autowired private PubsubHelper pubsubHelper;

  @Value("${queueconfig.survey-update-event-topic}")
  private String surveyUpdateEventTopic;

  @LocalServerPort private int port;

  @BeforeEach
  public void setUp() {
    pubsubHelper.purgeProjectMessages(SURVEY_UPDATE_TEST_SUBSCRIPTION, surveyUpdateEventTopic);
  }

  @Test
  public void testPostSurvey() throws InterruptedException {
    try (QueueSpy<EventDTO> surveyUpdateQueue =
        pubsubHelper.projectListen(SURVEY_UPDATE_TEST_SUBSCRIPTION, EventDTO.class)) {

      ColumnValidator[] testRules =
          new ColumnValidator[] {new ColumnValidator("foo", false, new Rule[] {})};

      integrationTestHelper.testPost(
          port,
          UserGroupAuthorisedActivityType.CREATE_SURVEY,
          (bundle) -> "surveys",
          (bundle) -> {
            SurveyDto surveyDto = new SurveyDto();
            surveyDto.setName("Test New Survey");
            surveyDto.setSampleSeparator(',');
            surveyDto.setSampleValidationRules(testRules);
            surveyDto.setSampleDefinitionUrl("http://foo.bar");
            surveyDto.setMetadata(Map.of("foo", "bar"));
            return surveyDto;
          });

      EventDTO emittedEvent = surveyUpdateQueue.getQueue().poll(20, TimeUnit.SECONDS);

      // Then
      assertThat(emittedEvent).isNotNull();
      assertThat(emittedEvent.getHeader().getTopic()).isEqualTo(surveyUpdateEventTopic);
      assertThat(emittedEvent.getPayload().getSurveyUpdate().getName())
          .isEqualTo("Test New Survey");
      assertThat(emittedEvent.getPayload().getSurveyUpdate().getMetadata())
          .isEqualTo(Map.of("foo", "bar"));
      RecursiveComparisonConfiguration configuration =
          RecursiveComparisonConfiguration.builder().build();
      assertThat(emittedEvent.getPayload().getSurveyUpdate().getSampleDefinition())
          .usingRecursiveComparison(configuration)
          .isEqualTo(testRules);
      assertThat(emittedEvent.getPayload().getSurveyUpdate().getSampleDefinitionUrl())
          .isEqualTo("http://foo.bar");
    }
  }
}
