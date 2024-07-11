package uk.gov.ons.ssdc.supporttool.endpoint;

import static com.google.cloud.spring.pubsub.support.PubSubTopicUtils.toProjectTopicName;

import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.ons.ssdc.common.model.entity.CollectionExercise;
import uk.gov.ons.ssdc.common.model.entity.CollectionInstrumentSelectionRule;
import uk.gov.ons.ssdc.common.model.entity.Survey;
import uk.gov.ons.ssdc.common.model.entity.UserGroupAuthorisedActivityType;
import uk.gov.ons.ssdc.supporttool.model.dto.messaging.CollectionExerciseUpdateDTO;
import uk.gov.ons.ssdc.supporttool.model.dto.messaging.EventDTO;
import uk.gov.ons.ssdc.supporttool.model.dto.messaging.EventHeaderDTO;
import uk.gov.ons.ssdc.supporttool.model.dto.messaging.PayloadDTO;
import uk.gov.ons.ssdc.supporttool.model.dto.ui.CollectionExerciseDto;
import uk.gov.ons.ssdc.supporttool.model.repository.CollectionExerciseRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.SurveyRepository;
import uk.gov.ons.ssdc.supporttool.security.AuthUser;
import uk.gov.ons.ssdc.supporttool.utility.EventHelper;

@RestController
@RequestMapping(value = "/api/collectionExercises")
public class CollectionExerciseEndpoint {

  private static final Logger log = LoggerFactory.getLogger(CollectionExerciseEndpoint.class);
  private static final ExpressionParser expressionParser = new SpelExpressionParser();

  private final CollectionExerciseRepository collectionExerciseRepository;
  private final SurveyRepository surveyRepository;
  private final AuthUser authUser;
  private final PubSubTemplate pubSubTemplate;

  @Value("${queueconfig.collection-exercise-update-event-topic}")
  private String collectionExerciseUpdateEventTopic;

  @Value("${spring.cloud.gcp.pubsub.project-id}")
  private String pubsubProject;

  @Value("${queueconfig.publishtimeout}")
  private int publishTimeout;

  public CollectionExerciseEndpoint(
      CollectionExerciseRepository collectionExerciseRepository,
      SurveyRepository surveyRepository,
      AuthUser authUser,
      PubSubTemplate pubSubTemplate) {
    this.collectionExerciseRepository = collectionExerciseRepository;
    this.surveyRepository = surveyRepository;
    this.authUser = authUser;
    this.pubSubTemplate = pubSubTemplate;
  }

  @GetMapping("/{collexId}")
  public CollectionExerciseDto getCollex(
      @PathVariable(value = "collexId") UUID collexId,
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {

    CollectionExercise collectionExercise =
        collectionExerciseRepository
            .findById(collexId)
            .orElseThrow(
                () -> {
                  log.atWarn()
                      .setMessage(
                          "Failed to get collection exercise, collection exercise not found")
                      .addKeyValue("httpStatus", HttpStatus.BAD_REQUEST)
                      .addKeyValue("collexId", collexId)
                      .addKeyValue("userEmail", userEmail)
                      .log();
                  return new ResponseStatusException(
                      HttpStatus.BAD_REQUEST, "Collection exercise not found");
                });

    authUser.checkUserPermission(
        userEmail,
        collectionExercise.getSurvey().getId(),
        UserGroupAuthorisedActivityType.VIEW_COLLECTION_EXERCISE);

    return mapDto(collectionExercise.getSurvey().getId(), collectionExercise);
  }

  @GetMapping
  public List<CollectionExerciseDto> findCollexsBySurvey(
      @RequestParam(value = "surveyId") UUID surveyId,
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {

    authUser.checkUserPermission(
        userEmail, surveyId, UserGroupAuthorisedActivityType.LIST_COLLECTION_EXERCISES);

    Survey survey =
        surveyRepository
            .findById(surveyId)
            .orElseThrow(
                () -> {
                  log.atWarn()
                      .setMessage("Failed to find collection exercise, Survey not found")
                      .addKeyValue("httpStatus", HttpStatus.BAD_REQUEST)
                      .addKeyValue("surveyId", surveyId)
                      .addKeyValue("userEmail", userEmail)
                      .log();
                  return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Survey not found");
                });

    return collectionExerciseRepository.findBySurvey(survey).stream()
        .map(collex -> mapDto(surveyId, collex))
        .collect(Collectors.toList());
  }

  private CollectionExerciseDto mapDto(UUID surveyId, CollectionExercise collex) {
    CollectionExerciseDto collectionExerciseDto = new CollectionExerciseDto();
    collectionExerciseDto.setId(collex.getId());
    collectionExerciseDto.setSurveyId(surveyId);
    collectionExerciseDto.setName(collex.getName());
    collectionExerciseDto.setReference(collex.getReference());
    collectionExerciseDto.setStartDate(collex.getStartDate());
    collectionExerciseDto.setEndDate(collex.getEndDate());
    collectionExerciseDto.setMetadata(collex.getMetadata());
    collectionExerciseDto.setCollectionInstrumentSelectionRules(
        collex.getCollectionInstrumentSelectionRules());
    return collectionExerciseDto;
  }

  @PostMapping
  @Transactional
  public ResponseEntity<UUID> createCollectionExercises(
      @RequestBody CollectionExerciseDto collectionExerciseDto,
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {

    authUser.checkUserPermission(
        userEmail,
        collectionExerciseDto.getSurveyId(),
        UserGroupAuthorisedActivityType.CREATE_COLLECTION_EXERCISE);

    Survey survey =
        surveyRepository
            .findById(collectionExerciseDto.getSurveyId())
            .orElseThrow(
                () -> {
                  log.atWarn()
                      .setMessage("Failed to create collection exercise, Survey not found")
                      .addKeyValue("httpStatus", HttpStatus.BAD_REQUEST)
                      .addKeyValue("surveyId", collectionExerciseDto.getSurveyId())
                      .addKeyValue("userEmail", userEmail)
                      .log();
                  return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Survey not found");
                });

    validateCollectionInstrumentRules(
        collectionExerciseDto.getCollectionInstrumentSelectionRules(), userEmail);

    CollectionExercise collectionExercise = new CollectionExercise();
    collectionExercise.setId(UUID.randomUUID());
    collectionExercise.setName(collectionExerciseDto.getName());
    collectionExercise.setSurvey(survey);
    collectionExercise.setReference(collectionExerciseDto.getReference());
    collectionExercise.setStartDate(collectionExerciseDto.getStartDate());
    collectionExercise.setEndDate(collectionExerciseDto.getEndDate());
    collectionExercise.setMetadata(collectionExerciseDto.getMetadata());
    collectionExercise.setCollectionInstrumentSelectionRules(
        collectionExerciseDto.getCollectionInstrumentSelectionRules());
    collectionExercise = collectionExerciseRepository.saveAndFlush(collectionExercise);

    CollectionExerciseUpdateDTO collectionExerciseUpdate = new CollectionExerciseUpdateDTO();
    collectionExerciseUpdate.setCollectionExerciseId(collectionExercise.getId());
    collectionExerciseUpdate.setName(collectionExercise.getName());
    collectionExerciseUpdate.setSurveyId(collectionExercise.getSurvey().getId());
    collectionExerciseUpdate.setReference(collectionExercise.getReference());
    collectionExerciseUpdate.setStartDate(collectionExercise.getStartDate());
    collectionExerciseUpdate.setEndDate(collectionExercise.getEndDate());
    collectionExerciseUpdate.setMetadata(collectionExercise.getMetadata());
    collectionExerciseUpdate.setCollectionInstrumentRules(
        collectionExercise.getCollectionInstrumentSelectionRules());

    PayloadDTO payloadDTO = new PayloadDTO();
    payloadDTO.setCollectionExerciseUpdate(collectionExerciseUpdate);

    EventDTO event = new EventDTO();

    EventHeaderDTO eventHeader =
        EventHelper.createEventDTO(collectionExerciseUpdateEventTopic, userEmail);
    event.setHeader(eventHeader);
    event.setPayload(payloadDTO);

    String topic = toProjectTopicName(collectionExerciseUpdateEventTopic, pubsubProject).toString();
    CompletableFuture<String> future = pubSubTemplate.publish(topic, event);

    try {
      future.get(publishTimeout, TimeUnit.SECONDS);
    } catch (InterruptedException | ExecutionException | TimeoutException e) {
      throw new RuntimeException(e);
    }

    return new ResponseEntity<>(collectionExercise.getId(), HttpStatus.CREATED);
  }

  private void validateCollectionInstrumentRules(
      CollectionInstrumentSelectionRule[] collectionInstrumentSelectionRules, String userEmail) {
    boolean foundDefaultRuleWithNullExpression = false;

    for (CollectionInstrumentSelectionRule collectionInstrumentSelectionRule :
        collectionInstrumentSelectionRules) {
      if (!StringUtils.hasText(collectionInstrumentSelectionRule.getCollectionInstrumentUrl())) {
        log.atWarn()
            .setMessage("Failed to create collection exercise, CI URL cannot be blank")
            .addKeyValue("httpStatus", HttpStatus.BAD_REQUEST)
            .addKeyValue("userEmail", userEmail)
            .log();
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CI URL cannot be blank");
      }

      String spelExpression = collectionInstrumentSelectionRule.getSpelExpression();

      if (spelExpression == null) {
        if (collectionInstrumentSelectionRule.getPriority() == 0) {
          foundDefaultRuleWithNullExpression = true;
        }

        continue;
      } else if (!StringUtils.hasText(spelExpression)) {
        log.atWarn()
            .setMessage("Failed to create collection exercise, SPEL expression cannot be blank")
            .addKeyValue("httpStatus", HttpStatus.BAD_REQUEST)
            .addKeyValue("userEmail", userEmail)
            .log();
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, "SPEL expression cannot be blank");
      }

      try {
        expressionParser.parseExpression(spelExpression);
      } catch (Exception e) {
        log.atWarn()
            .setMessage("Failed to create collection exercise, Invalid SPEL")
            .addKeyValue("httpStatus", HttpStatus.BAD_REQUEST)
            .addKeyValue("spelExpression", spelExpression)
            .addKeyValue("userEmail", userEmail)
            .log();
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, "Invalid SPEL: " + spelExpression, e);
      }
    }

    if (!foundDefaultRuleWithNullExpression) {
      log.atWarn()
          .setMessage(
              "Failed to create collection exercise, Rules must include zero priority default with null expression")
          .addKeyValue("httpStatus", HttpStatus.BAD_REQUEST)
          .addKeyValue("userEmail", userEmail)
          .log();
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Rules must include zero priority default with null expression");
    }
  }
}
