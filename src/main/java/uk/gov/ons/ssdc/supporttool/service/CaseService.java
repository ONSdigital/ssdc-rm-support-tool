package uk.gov.ons.ssdc.supporttool.service;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
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

@Service
public class CaseService {

  private static final String EVENT_SOURCE = "SUPPORT_TOOL";
  private static final String EVENT_CHANNEL = "RM";

  private final CaseRepository caseRepository;
  private final RabbitTemplate rabbitTemplate;

  @Value("${queueconfig.case-event-exchange}")
  private String eventsExchange;

  @Value("${queueconfig.refusal-event-routing-key}")
  private String refusalEventRoutingKey;

  @Value("${queueconfig.invalid-address-event-routing-key}")
  private String invalidAddressEventRoutingKey;

  @Value("${queueconfig.fulfilment-routing-key}")
  private String fulfilmentRoutingKey;

  public CaseService(CaseRepository caseRepository, RabbitTemplate rabbitTemplate) {
    this.caseRepository = caseRepository;
    this.rabbitTemplate = rabbitTemplate;
  }

  public Case getCaseByCaseId(UUID caseId) {
    Optional<Case> cazeResult = caseRepository.findById(caseId);

    if (cazeResult.isEmpty()) {
      throw new RuntimeException(String.format("Case ID '%s' not found", caseId));
    }
    return cazeResult.get();
  }

  public void buildAndSendRefusalEvent(Refusal refusal, Case caze) {
    EventDTO eventDTO = new EventDTO();
    eventDTO.setType(EventTypeDTO.REFUSAL_RECEIVED);
    eventDTO.setDateTime(OffsetDateTime.now());
    eventDTO.setTransactionId(UUID.randomUUID());
    eventDTO.setChannel(EVENT_CHANNEL);
    eventDTO.setSource(EVENT_SOURCE);

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

    responseManagementEvent.setEvent(eventDTO);
    responseManagementEvent.setPayload(payloadDTO);

    rabbitTemplate.convertAndSend(eventsExchange, refusalEventRoutingKey, responseManagementEvent);
  }

  public void buildAndSendInvalidAddressCaseEvent(InvalidAddress invalidAddress, Case caze) {
    EventDTO eventDTO = new EventDTO();
    eventDTO.setType(EventTypeDTO.ADDRESS_NOT_VALID);
    eventDTO.setDateTime(OffsetDateTime.now());
    eventDTO.setTransactionId(UUID.randomUUID());
    eventDTO.setChannel(EVENT_CHANNEL);
    eventDTO.setSource(EVENT_SOURCE);

    InvalidAddressDTO invalidAddressDTO = new InvalidAddressDTO();
    invalidAddressDTO.setCaseId(caze.getId());
    invalidAddressDTO.setReason(invalidAddress.getReason());
    invalidAddressDTO.setNotes(invalidAddress.getNotes());

    PayloadDTO payloadDTO = new PayloadDTO();
    payloadDTO.setInvalidAddress(invalidAddressDTO);

    ResponseManagementEvent responseManagementEvent = new ResponseManagementEvent();

    responseManagementEvent.setEvent(eventDTO);
    responseManagementEvent.setPayload(payloadDTO);

    rabbitTemplate.convertAndSend(
        eventsExchange, invalidAddressEventRoutingKey, responseManagementEvent);
  }

  public void buildAndSendFulfilmentCaseEvent(Fulfilment fulfilment, Case caze) {
    EventDTO eventDTO = new EventDTO();
    eventDTO.setType(EventTypeDTO.FULFILMENT);
    eventDTO.setDateTime(OffsetDateTime.now());
    eventDTO.setTransactionId(UUID.randomUUID());
    eventDTO.setChannel(EVENT_CHANNEL);
    eventDTO.setSource(EVENT_SOURCE);

    FulfilmentDTO fulfilmentDTO = new FulfilmentDTO();
    fulfilmentDTO.setCaseId(caze.getId());
    fulfilmentDTO.setPackCode(fulfilment.getPackCode());

    PayloadDTO payloadDTO = new PayloadDTO();
    payloadDTO.setFulfilment(fulfilmentDTO);

    ResponseManagementEvent responseManagementEvent = new ResponseManagementEvent();

    responseManagementEvent.setEvent(eventDTO);
    responseManagementEvent.setPayload(payloadDTO);

    rabbitTemplate.convertAndSend(eventsExchange, fulfilmentRoutingKey, responseManagementEvent);
  }
}
