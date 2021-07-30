package uk.gov.ons.ssdc.supporttool.service;

import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gcp.pubsub.core.PubSubTemplate;
import org.springframework.stereotype.Service;
import uk.gov.ons.ssdc.supporttool.model.dto.messaging.CollectionCase;
import uk.gov.ons.ssdc.supporttool.model.dto.messaging.EventDTO;
import uk.gov.ons.ssdc.supporttool.model.dto.messaging.EventTypeDTO;
import uk.gov.ons.ssdc.supporttool.model.dto.messaging.FulfilmentDTO;
import uk.gov.ons.ssdc.supporttool.model.dto.messaging.InvalidAddressDTO;
import uk.gov.ons.ssdc.supporttool.model.dto.messaging.PayloadDTO;
import uk.gov.ons.ssdc.supporttool.model.dto.messaging.RefusalDTO;
import uk.gov.ons.ssdc.supporttool.model.dto.messaging.ResponseManagementEvent;
import uk.gov.ons.ssdc.supporttool.model.dto.ui.Fulfilment;
import uk.gov.ons.ssdc.supporttool.model.dto.ui.InvalidAddress;
import uk.gov.ons.ssdc.supporttool.model.dto.ui.Refusal;
import uk.gov.ons.ssdc.supporttool.model.entity.Case;
import uk.gov.ons.ssdc.supporttool.model.repository.CaseRepository;
import uk.gov.ons.ssdc.supporttool.utility.EventHelper;

@Service
public class CaseService {

  private final CaseRepository caseRepository;
  private final PubSubTemplate pubSubTemplate;

  @Value("${queueconfig.refusal-event-topic}")
  private String refusalEventTopic;

  @Value("${queueconfig.invalid-address-event-topic}")
  private String invalidAddressEventTopic;

  @Value("${queueconfig.fulfilment-topic}")
  private String fulfilmentTopic;

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

  public void buildAndSendRefusalEvent(Refusal refusal, Case caze) {
    CollectionCase collectionCase = new CollectionCase();
    collectionCase.setCaseId(caze.getId());

    RefusalDTO refusalDTO = new RefusalDTO();
    refusalDTO.setCollectionCase(collectionCase);
    refusalDTO.setType(refusal.getType());
    refusalDTO.setAgentId(refusal.getAgentId());
    refusalDTO.setCallId(refusal.getCallId());

    PayloadDTO payloadDTO = new PayloadDTO();
    payloadDTO.setRefusal(refusalDTO);

    ResponseManagementEvent responseManagementEvent = new ResponseManagementEvent();

    EventDTO eventDTO = EventHelper.createEventDTO(EventTypeDTO.REFUSAL_RECEIVED);
    responseManagementEvent.setEvent(eventDTO);
    responseManagementEvent.setPayload(payloadDTO);

    pubSubTemplate.publish(refusalEventTopic, responseManagementEvent);
  }

  public void buildAndSendInvalidAddressCaseEvent(InvalidAddress invalidAddress, Case caze) {
    InvalidAddressDTO invalidAddressDTO = new InvalidAddressDTO();
    invalidAddressDTO.setCaseId(caze.getId());
    invalidAddressDTO.setReason(invalidAddress.getReason());
    invalidAddressDTO.setNotes(invalidAddress.getNotes());

    PayloadDTO payloadDTO = new PayloadDTO();
    payloadDTO.setInvalidAddress(invalidAddressDTO);

    ResponseManagementEvent responseManagementEvent = new ResponseManagementEvent();

    EventDTO eventDTO = EventHelper.createEventDTO(EventTypeDTO.ADDRESS_NOT_VALID);
    responseManagementEvent.setEvent(eventDTO);
    responseManagementEvent.setPayload(payloadDTO);

    pubSubTemplate.publish(invalidAddressEventTopic, responseManagementEvent);
  }

  public void buildAndSendFulfilmentCaseEvent(Fulfilment fulfilment, Case caze) {

    FulfilmentDTO fulfilmentDTO = new FulfilmentDTO();
    fulfilmentDTO.setCaseId(caze.getId());
    fulfilmentDTO.setPackCode(fulfilment.getPackCode());

    PayloadDTO payloadDTO = new PayloadDTO();
    payloadDTO.setFulfilment(fulfilmentDTO);

    ResponseManagementEvent responseManagementEvent = new ResponseManagementEvent();

    EventDTO eventDTO = EventHelper.createEventDTO(EventTypeDTO.FULFILMENT);
    responseManagementEvent.setEvent(eventDTO);
    responseManagementEvent.setPayload(payloadDTO);

    pubSubTemplate.publish(fulfilmentTopic, responseManagementEvent);
  }
}
