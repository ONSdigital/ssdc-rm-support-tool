package uk.gov.ons.ssdc.supporttool.service;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.ons.ssdc.supporttool.model.dto.CollectionCase;
import uk.gov.ons.ssdc.supporttool.model.dto.EventDTO;
import uk.gov.ons.ssdc.supporttool.model.dto.EventTypeDTO;
import uk.gov.ons.ssdc.supporttool.model.dto.PayloadDTO;
import uk.gov.ons.ssdc.supporttool.model.dto.RefusalDTO;
import uk.gov.ons.ssdc.supporttool.model.dto.ResponseManagementEvent;
import uk.gov.ons.ssdc.supporttool.model.entity.Case;
import uk.gov.ons.ssdc.supporttool.model.repository.CaseRepository;


@Service
public class CaseService {

  private final CaseRepository caseRepository;
  private final RabbitTemplate rabbitTemplate;

  @Value("${queueconfig.events-exchange}")
  private String eventsExchange;

  @Value("${queueconfig.refusal-event-routing-key}")
  private String refusalEventRoutingKey;

  public CaseService(CaseRepository caseRepository,
      RabbitTemplate rabbitTemplate) {
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


  public void buildAndSendRefusalEvent(RefusalDTO refusalDTO, Case caze) {
    EventDTO eventDTO = new EventDTO();
    eventDTO.setType(EventTypeDTO.REFUSAL_RECEIVED);
    eventDTO.setDateTime(OffsetDateTime.now());
    eventDTO.setTransactionId(UUID.randomUUID());
    eventDTO.setChannel("RM");
    eventDTO.setSource("SUPPORT_TOOL");

    CollectionCase collectionCase = new CollectionCase();
    collectionCase.setCaseId(caze.getId());

    PayloadDTO payloadDTO = new PayloadDTO();
    payloadDTO.setRefusal(refusalDTO);

    ResponseManagementEvent responseManagementEvent =
        new ResponseManagementEvent(eventDTO, payloadDTO);

    rabbitTemplate.convertAndSend(
        eventsExchange, refusalEventRoutingKey, responseManagementEvent);
  }
}
