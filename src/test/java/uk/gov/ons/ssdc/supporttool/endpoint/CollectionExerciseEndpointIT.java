package uk.gov.ons.ssdc.supporttool.endpoint;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.ons.ssdc.common.model.entity.CollectionInstrumentSelectionRule;
import uk.gov.ons.ssdc.common.model.entity.UserGroupAuthorisedActivityType;
import uk.gov.ons.ssdc.supporttool.model.dto.messaging.EventDTO;
import uk.gov.ons.ssdc.supporttool.model.dto.ui.CollectionExerciseDto;
import uk.gov.ons.ssdc.supporttool.testhelper.IntegrationTestHelper;
import uk.gov.ons.ssdc.supporttool.testhelper.PubsubHelper;
import uk.gov.ons.ssdc.supporttool.testhelper.QueueSpy;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class CollectionExerciseEndpointIT {
  private static final String COLLECTION_EXERCISE_UPDATE_TEST_SUBSCRIPTION =
      "event_collection-exercise-update_rh";

  private static final Map<String, String> TEST_COLLECTION_EXERCISE_UPDATE_METADATA =
      Map.of("TEST_COLLECTION_EXERCISE_UPDATE_METADATA", "TEST");

  @Autowired private IntegrationTestHelper integrationTestHelper;

  @Autowired private PubsubHelper pubsubHelper;

  @Value("${queueconfig.collection-exercise-update-event-topic}")
  private String collectionExerciseUpdateEventTopic;

  @LocalServerPort private int port;

  @BeforeEach
  public void setUp() {
    pubsubHelper.purgeProjectMessages(
        COLLECTION_EXERCISE_UPDATE_TEST_SUBSCRIPTION, collectionExerciseUpdateEventTopic);
  }

  @Test
  public void testPostCollectionExercise() throws InterruptedException {
    try (QueueSpy<EventDTO> collectionExerciseUpdateQueue =
        pubsubHelper.projectListen(COLLECTION_EXERCISE_UPDATE_TEST_SUBSCRIPTION, EventDTO.class)) {

      OffsetDateTime collexStartDate = OffsetDateTime.now();
      OffsetDateTime collexEndDate = OffsetDateTime.now().plusDays(2);
      AtomicReference<UUID> surveyId = new AtomicReference<>();

      integrationTestHelper.testPost(
          port,
          UserGroupAuthorisedActivityType.CREATE_COLLECTION_EXERCISE,
          (bundle) -> "collectionExercises",
          (bundle) -> {
            surveyId.set(bundle.getSurveyId());
            CollectionExerciseDto collectionExerciseDto = new CollectionExerciseDto();
            collectionExerciseDto.setSurveyId(bundle.getSurveyId());
            collectionExerciseDto.setName("Test");
            collectionExerciseDto.setReference("TEST_REFERENCE");
            collectionExerciseDto.setStartDate(collexStartDate);
            collectionExerciseDto.setEndDate(collexEndDate);
            collectionExerciseDto.setMetadata(TEST_COLLECTION_EXERCISE_UPDATE_METADATA);
            collectionExerciseDto.setCollectionInstrumentSelectionRules(
                new CollectionInstrumentSelectionRule[] {
                  new CollectionInstrumentSelectionRule(0, null, "dummyUrl", null)
                });
            return collectionExerciseDto;
          });

      EventDTO emittedEvent = collectionExerciseUpdateQueue.getQueue().poll(20, TimeUnit.SECONDS);

      // Then
      assertThat(emittedEvent).isNotNull();
      assertThat(emittedEvent.getHeader().getTopic()).isEqualTo(collectionExerciseUpdateEventTopic);
      assertThat(emittedEvent.getPayload().getCollectionExerciseUpdate().getSurveyId())
          .isEqualTo(surveyId.get());
      assertThat(emittedEvent.getPayload().getCollectionExerciseUpdate().getName())
          .isEqualTo("Test");
      assertThat(emittedEvent.getPayload().getCollectionExerciseUpdate().getReference())
          .isEqualTo("TEST_REFERENCE");
      assertThat(emittedEvent.getPayload().getCollectionExerciseUpdate().getStartDate())
          .isEqualTo(collexStartDate);
      assertThat(emittedEvent.getPayload().getCollectionExerciseUpdate().getEndDate())
          .isEqualTo(collexEndDate);
      assertThat(emittedEvent.getPayload().getCollectionExerciseUpdate().getMetadata())
          .isEqualTo(TEST_COLLECTION_EXERCISE_UPDATE_METADATA);
    }
  }
}
