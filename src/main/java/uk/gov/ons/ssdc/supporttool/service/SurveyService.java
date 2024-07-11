package uk.gov.ons.ssdc.supporttool.service;

import static com.google.cloud.spring.pubsub.support.PubSubTopicUtils.toProjectTopicName;

import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.ons.ssdc.common.model.entity.Survey;
import uk.gov.ons.ssdc.supporttool.model.dto.messaging.AllowedFulfilmentDto;
import uk.gov.ons.ssdc.supporttool.model.dto.messaging.EventDTO;
import uk.gov.ons.ssdc.supporttool.model.dto.messaging.EventHeaderDTO;
import uk.gov.ons.ssdc.supporttool.model.dto.messaging.PayloadDTO;
import uk.gov.ons.ssdc.supporttool.model.dto.messaging.SurveyUpdateDto;
import uk.gov.ons.ssdc.supporttool.utility.EventHelper;

@Component
public class SurveyService {
  private final PubSubTemplate pubSubTemplate;

  @Value("${queueconfig.survey-update-event-topic}")
  private String surveyUpdateEventTopic;

  @Value("${spring.cloud.gcp.pubsub.project-id}")
  private String pubsubProject;

  @Value("${queueconfig.publishtimeout}")
  private int publishTimeout;

  public SurveyService(PubSubTemplate pubSubTemplate) {
    this.pubSubTemplate = pubSubTemplate;
  }

  public void publishSurveyUpdate(Survey survey, String userEmail) {
    SurveyUpdateDto surveyUpdate = new SurveyUpdateDto();
    surveyUpdate.setSurveyId(survey.getId());
    surveyUpdate.setName(survey.getName());
    surveyUpdate.setMetadata(survey.getMetadata());
    surveyUpdate.setSampleDefinition(survey.getSampleValidationRules());
    surveyUpdate.setSampleDefinitionUrl(survey.getSampleDefinitionUrl());

    List<AllowedFulfilmentDto> allowedPrintFulfilments;

    if (survey.getFulfilmentExportFileTemplates() == null) {
      allowedPrintFulfilments = Collections.emptyList();
    } else {
      // TODO: This will need to filter to only "PRINT" type, when type exists
      allowedPrintFulfilments =
          survey.getFulfilmentExportFileTemplates().stream()
              .map(
                  template -> {
                    AllowedFulfilmentDto allowedFulfilmentDto = new AllowedFulfilmentDto();
                    allowedFulfilmentDto.setPackCode(
                        template.getExportFileTemplate().getPackCode());
                    allowedFulfilmentDto.setDescription(
                        template.getExportFileTemplate().getDescription());
                    allowedFulfilmentDto.setMetadata(
                        template.getExportFileTemplate().getMetadata());
                    return allowedFulfilmentDto;
                  })
              .collect(Collectors.toList());
    }

    surveyUpdate.setAllowedPrintFulfilments(allowedPrintFulfilments);

    List<AllowedFulfilmentDto> allowedSmsFulfilments;
    if (survey.getSmsTemplates() == null) {
      allowedSmsFulfilments = Collections.emptyList();
    } else {
      allowedSmsFulfilments =
          survey.getSmsTemplates().stream()
              .map(
                  template -> {
                    AllowedFulfilmentDto allowedFulfilmentDto = new AllowedFulfilmentDto();
                    allowedFulfilmentDto.setPackCode(template.getSmsTemplate().getPackCode());
                    allowedFulfilmentDto.setDescription(template.getSmsTemplate().getDescription());
                    allowedFulfilmentDto.setMetadata(template.getSmsTemplate().getMetadata());
                    return allowedFulfilmentDto;
                  })
              .collect(Collectors.toList());
    }

    surveyUpdate.setAllowedSmsFulfilments(allowedSmsFulfilments);

    List<AllowedFulfilmentDto> alloweEmailFulfilments;
    if (survey.getEmailTemplates() == null) {
      alloweEmailFulfilments = Collections.emptyList();
    } else {
      alloweEmailFulfilments =
          survey.getEmailTemplates().stream()
              .map(
                  template -> {
                    AllowedFulfilmentDto allowedFulfilmentDto = new AllowedFulfilmentDto();
                    allowedFulfilmentDto.setPackCode(template.getEmailTemplate().getPackCode());
                    allowedFulfilmentDto.setDescription(
                        template.getEmailTemplate().getDescription());
                    allowedFulfilmentDto.setMetadata(template.getEmailTemplate().getMetadata());
                    return allowedFulfilmentDto;
                  })
              .collect(Collectors.toList());
    }

    surveyUpdate.setAllowedEmailFulfilments(alloweEmailFulfilments);

    PayloadDTO payloadDTO = new PayloadDTO();
    payloadDTO.setSurveyUpdate(surveyUpdate);

    EventDTO event = new EventDTO();

    EventHeaderDTO eventHeader = EventHelper.createEventDTO(surveyUpdateEventTopic, userEmail);
    event.setHeader(eventHeader);
    event.setPayload(payloadDTO);

    String topic = toProjectTopicName(surveyUpdateEventTopic, pubsubProject).toString();
    CompletableFuture<String> future = pubSubTemplate.publish(topic, event);

    try {
      future.get(publishTimeout, TimeUnit.SECONDS);
    } catch (InterruptedException | ExecutionException | TimeoutException e) {
      throw new RuntimeException(e);
    }
  }
}
