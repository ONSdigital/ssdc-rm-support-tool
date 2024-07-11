package uk.gov.ons.ssdc.supporttool.service;

import static com.google.cloud.spring.pubsub.support.PubSubTopicUtils.toProjectTopicName;

import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.ons.ssdc.common.model.entity.Case;
import uk.gov.ons.ssdc.supporttool.model.dto.messaging.EventDTO;
import uk.gov.ons.ssdc.supporttool.model.dto.messaging.EventHeaderDTO;
import uk.gov.ons.ssdc.supporttool.model.dto.messaging.InvalidCaseDTO;
import uk.gov.ons.ssdc.supporttool.model.dto.messaging.PayloadDTO;
import uk.gov.ons.ssdc.supporttool.model.dto.messaging.PrintFulfilmentDTO;
import uk.gov.ons.ssdc.supporttool.model.dto.messaging.RefusalDTO;
import uk.gov.ons.ssdc.supporttool.model.dto.messaging.UpdateSample;
import uk.gov.ons.ssdc.supporttool.model.dto.messaging.UpdateSampleSensitive;
import uk.gov.ons.ssdc.supporttool.model.dto.ui.InvalidCase;
import uk.gov.ons.ssdc.supporttool.model.dto.ui.PrintFulfilment;
import uk.gov.ons.ssdc.supporttool.model.dto.ui.Refusal;
import uk.gov.ons.ssdc.supporttool.model.repository.CaseRepository;
import uk.gov.ons.ssdc.supporttool.utility.EventHelper;

@Service
public class CaseService {

  private final CaseRepository caseRepository;
  private final PubSubTemplate pubSubTemplate;

  @Value("${queueconfig.refusal-event-topic}")
  private String refusalEventTopic;

  @Value("${queueconfig.invalid-case-event-topic}")
  private String invalidCaseEventTopic;

  @Value("${queueconfig.print-fulfilment-topic}")
  private String printFulfilmentTopic;

  @Value("${queueconfig.update-sample-sensitive-topic}")
  private String updateSampleSensitiveTopic;

  @Value("${queueconfig.update-sample-topic}")
  private String updateSampleTopic;

  @Value("${spring.cloud.gcp.pubsub.project-id}")
  private String pubsubProject;

  public CaseService(CaseRepository caseRepository, PubSubTemplate pubSubTemplate) {
    this.caseRepository = caseRepository;
    this.pubSubTemplate = pubSubTemplate;
  }

  public Case getCaseByCaseId(UUID caseId) {
    Optional<Case> cazeResult = caseRepository.findById(caseId);

    if (cazeResult.isEmpty()) {
      throw new RuntimeException(String.format("Case ID '%s' not found", caseId));
    }
    return cazeResult.get();
  }

  public void buildAndSendRefusalEvent(Refusal refusal, Case caze, String userEmail) {
    RefusalDTO refusalDTO = new RefusalDTO();
    refusalDTO.setCaseId(caze.getId());
    refusalDTO.setType(refusal.getType());

    PayloadDTO payloadDTO = new PayloadDTO();
    payloadDTO.setRefusal(refusalDTO);

    EventDTO event = new EventDTO();

    EventHeaderDTO eventHeader = EventHelper.createEventDTO(refusalEventTopic, userEmail);
    event.setHeader(eventHeader);
    event.setPayload(payloadDTO);

    String topic = toProjectTopicName(refusalEventTopic, pubsubProject).toString();
    pubSubTemplate.publish(topic, event);
  }

  public void buildAndSendUpdateSensitiveSampleEvent(
      UpdateSampleSensitive updateSampleSensitive, String userEmail) {
    PayloadDTO payloadDTO = new PayloadDTO();
    payloadDTO.setUpdateSampleSensitive(updateSampleSensitive);

    EventDTO event = new EventDTO();

    EventHeaderDTO eventHeader = EventHelper.createEventDTO(updateSampleSensitiveTopic, userEmail);
    event.setHeader(eventHeader);
    event.setPayload(payloadDTO);

    String topic = toProjectTopicName(updateSampleSensitiveTopic, pubsubProject).toString();
    pubSubTemplate.publish(topic, event);
  }

  public void buildAndSendUpdateSampleEvent(UpdateSample updateSample, String userEmail) {
    PayloadDTO payloadDTO = new PayloadDTO();
    payloadDTO.setUpdateSample(updateSample);

    EventDTO event = new EventDTO();

    EventHeaderDTO eventHeader = EventHelper.createEventDTO(updateSampleTopic, userEmail);
    event.setHeader(eventHeader);
    event.setPayload(payloadDTO);

    String topic = toProjectTopicName(updateSampleTopic, pubsubProject).toString();
    pubSubTemplate.publish(topic, event);
  }

  public void buildAndSendInvalidAddressCaseEvent(
      InvalidCase invalidCase, Case caze, String userEmail) {
    InvalidCaseDTO invalidCaseDTO = new InvalidCaseDTO();
    invalidCaseDTO.setCaseId(caze.getId());
    invalidCaseDTO.setReason(invalidCase.getReason());

    PayloadDTO payloadDTO = new PayloadDTO();
    payloadDTO.setInvalidCase(invalidCaseDTO);

    EventDTO event = new EventDTO();

    EventHeaderDTO eventHeader = EventHelper.createEventDTO(invalidCaseEventTopic, userEmail);
    event.setHeader(eventHeader);
    event.setPayload(payloadDTO);

    String topic = toProjectTopicName(invalidCaseEventTopic, pubsubProject).toString();
    pubSubTemplate.publish(topic, event);
  }

  public void buildAndSendPrintFulfilmentCaseEvent(
      PrintFulfilment printFulfilment, Case caze, String userEmail) {

    PrintFulfilmentDTO printFulfilmentDTO = new PrintFulfilmentDTO();
    printFulfilmentDTO.setCaseId(caze.getId());
    printFulfilmentDTO.setPackCode(printFulfilment.getPackCode());
    printFulfilmentDTO.setUacMetadata(printFulfilment.getUacMetadata());
    printFulfilmentDTO.setPersonalisation(printFulfilment.getPersonalisation());

    PayloadDTO payloadDTO = new PayloadDTO();
    payloadDTO.setPrintFulfilment(printFulfilmentDTO);

    EventDTO event = new EventDTO();

    EventHeaderDTO eventHeader = EventHelper.createEventDTO(printFulfilmentTopic, userEmail);
    event.setHeader(eventHeader);
    event.setPayload(payloadDTO);

    String topic = toProjectTopicName(printFulfilmentTopic, pubsubProject).toString();
    pubSubTemplate.publish(topic, event);
  }
}
