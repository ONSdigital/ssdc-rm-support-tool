package uk.gov.ons.ssdc.supporttool.endpoint;

import static com.google.cloud.spring.pubsub.support.PubSubTopicUtils.toProjectTopicName;

import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.ons.ssdc.common.model.entity.Survey;
import uk.gov.ons.ssdc.common.model.entity.UserGroupAuthorisedActivityType;
import uk.gov.ons.ssdc.supporttool.model.dto.messaging.EventDTO;
import uk.gov.ons.ssdc.supporttool.model.dto.messaging.EventHeaderDTO;
import uk.gov.ons.ssdc.supporttool.model.dto.messaging.PayloadDTO;
import uk.gov.ons.ssdc.supporttool.model.dto.messaging.SurveyUpdateDto;
import uk.gov.ons.ssdc.supporttool.model.dto.ui.SurveyDto;
import uk.gov.ons.ssdc.supporttool.model.repository.SurveyRepository;
import uk.gov.ons.ssdc.supporttool.security.UserIdentity;
import uk.gov.ons.ssdc.supporttool.utility.EventHelper;

@RestController
@RequestMapping(value = "/api/surveys")
public class SurveyEndpoint {
  private final SurveyRepository surveyRepository;
  private final UserIdentity userIdentity;
  private final PubSubTemplate pubSubTemplate;

  @Value("${queueconfig.survey-update-event-topic}")
  private String surveyUpdateEventTopic;

  @Value("${queueconfig.shared-pubsub-project}")
  private String sharedPubsubProject;

  @Value("${queueconfig.publishtimeout}")
  private int publishTimeout;

  public SurveyEndpoint(
      SurveyRepository surveyRepository, UserIdentity userIdentity, PubSubTemplate pubSubTemplate) {
    this.surveyRepository = surveyRepository;
    this.userIdentity = userIdentity;
    this.pubSubTemplate = pubSubTemplate;
  }

  @GetMapping
  public List<SurveyDto> getSurveys(
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {
    userIdentity.checkGlobalUserPermission(userEmail, UserGroupAuthorisedActivityType.LIST_SURVEYS);

    return surveyRepository.findAll().stream().map(this::mapToDto).collect(Collectors.toList());
  }

  private SurveyDto mapToDto(Survey survey) {
    SurveyDto surveyDto = new SurveyDto();
    surveyDto.setId(survey.getId());
    surveyDto.setName(survey.getName());
    surveyDto.setSampleSeparator(survey.getSampleSeparator());
    surveyDto.setSampleValidationRules(survey.getSampleValidationRules());
    surveyDto.setSampleWithHeaderRow(survey.isSampleWithHeaderRow());
    surveyDto.setSampleDefinitionUrl(survey.getSampleDefinitionUrl());
    surveyDto.setMetadata(survey.getMetadata());
    return surveyDto;
  }

  @GetMapping("/{surveyId}")
  public SurveyDto getSurvey(
      @PathVariable(value = "surveyId") UUID surveyId,
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {
    Survey survey =
        surveyRepository
            .findById(surveyId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Survey not found"));

    userIdentity.checkUserPermission(
        userEmail, survey, UserGroupAuthorisedActivityType.VIEW_SURVEY);

    return mapToDto(survey);
  }

  @PostMapping
  public ResponseEntity<UUID> createSurvey(
      @RequestBody SurveyDto surveyDto,
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {
    userIdentity.checkGlobalUserPermission(
        userEmail, UserGroupAuthorisedActivityType.CREATE_SURVEY);

    Survey survey = new Survey();
    survey.setId(UUID.randomUUID());
    survey.setName(surveyDto.getName());
    survey.setSampleSeparator(surveyDto.getSampleSeparator());
    survey.setSampleValidationRules(surveyDto.getSampleValidationRules());
    survey.setSampleWithHeaderRow(surveyDto.isSampleWithHeaderRow());
    survey.setSampleDefinitionUrl(surveyDto.getSampleDefinitionUrl());
    survey.setMetadata(surveyDto.getMetadata());
    survey = surveyRepository.saveAndFlush(survey);

    SurveyUpdateDto surveyUpdate = new SurveyUpdateDto();
    surveyUpdate.setSurveyId(survey.getId());
    surveyUpdate.setName(surveyDto.getName());
    surveyUpdate.setMetadata(surveyDto.getMetadata());
    surveyUpdate.setSampleDefinition(surveyDto.getSampleValidationRules());
    surveyUpdate.setSampleDefinitionUrl(surveyDto.getSampleDefinitionUrl());

    PayloadDTO payloadDTO = new PayloadDTO();
    payloadDTO.setSurveyUpdate(surveyUpdate);

    EventDTO event = new EventDTO();

    EventHeaderDTO eventHeader = EventHelper.createEventDTO(surveyUpdateEventTopic, userEmail);
    event.setHeader(eventHeader);
    event.setPayload(payloadDTO);

    String topic = toProjectTopicName(surveyUpdateEventTopic, sharedPubsubProject).toString();
    ListenableFuture<String> future = pubSubTemplate.publish(topic, event);

    try {
      future.get(publishTimeout, TimeUnit.SECONDS);
    } catch (InterruptedException | ExecutionException | TimeoutException e) {
      throw new RuntimeException(e);
    }

    return new ResponseEntity<>(survey.getId(), HttpStatus.CREATED);
  }
}
