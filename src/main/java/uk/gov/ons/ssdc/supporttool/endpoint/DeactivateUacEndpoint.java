package uk.gov.ons.ssdc.supporttool.endpoint;

import static uk.gov.ons.ssdc.supporttool.model.entity.UserGroupAuthorisedActivityType.CREATE_PRINT_TEMPLATE;

import java.util.Optional;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.ons.ssdc.supporttool.model.dto.messaging.DeactivateUacDTO;
import uk.gov.ons.ssdc.supporttool.model.dto.messaging.EventDTO;
import uk.gov.ons.ssdc.supporttool.model.dto.messaging.EventTypeDTO;
import uk.gov.ons.ssdc.supporttool.model.dto.messaging.PayloadDTO;
import uk.gov.ons.ssdc.supporttool.model.dto.messaging.ResponseManagementEvent;
import uk.gov.ons.ssdc.supporttool.model.entity.UacQidLink;
import uk.gov.ons.ssdc.supporttool.model.repository.UacQidLinkRepository;
import uk.gov.ons.ssdc.supporttool.security.UserIdentity;
import uk.gov.ons.ssdc.supporttool.utility.EventHelper;

@RestController
@RequestMapping(value = "/deactivateUac")
public class DeactivateUacEndpoint {

  private final RabbitTemplate rabbitTemplate;
  private final UserIdentity userIdentity;
  private final UacQidLinkRepository qidLinkRepository;

  @Value("${queueconfig.case-event-exchange}")
  private String outboundExchange;

  @Value("${queueconfig.deactivate-uac-routing-key}")
  private String deactivateUacRoutingKey;

  public DeactivateUacEndpoint(
      RabbitTemplate rabbitTemplate,
      UserIdentity userIdentity,
      UacQidLinkRepository qidLinkRepository) {
    this.rabbitTemplate = rabbitTemplate;
    this.userIdentity = userIdentity;
    this.qidLinkRepository = qidLinkRepository;
  }

  @GetMapping(value = "/{qid}")
  public void deactivateUac(
      @PathVariable("qid") String qid,
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {
    Optional<UacQidLink> uacQidLinkOpt = qidLinkRepository.findByQid(qid);
    if (!uacQidLinkOpt.isPresent()) {
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND, String.format("Could not find QID %s", qid));
    }

    userIdentity.checkUserPermission(
        userEmail,
        uacQidLinkOpt.get().getCaze().getCollectionExercise().getSurvey(),
        CREATE_PRINT_TEMPLATE);

    ResponseManagementEvent rme = new ResponseManagementEvent();

    EventDTO event = EventHelper.createEventDTO(EventTypeDTO.DEACTIVATE_UAC);
    rme.setEvent(event);

    PayloadDTO payload = new PayloadDTO();
    DeactivateUacDTO deactivateUac = new DeactivateUacDTO();
    deactivateUac.setQid(qid);
    payload.setDeactivateUac(deactivateUac);
    rme.setPayload(payload);

    rabbitTemplate.convertAndSend(outboundExchange, deactivateUacRoutingKey, rme);
  }
}
