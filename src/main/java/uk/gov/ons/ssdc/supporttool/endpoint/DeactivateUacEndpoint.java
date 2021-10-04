package uk.gov.ons.ssdc.supporttool.endpoint;

import static com.google.cloud.spring.pubsub.support.PubSubTopicUtils.toProjectTopicName;
import static uk.gov.ons.ssdc.common.model.entity.UserGroupAuthorisedActivityType.DEACTIVATE_UAC;

import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.ons.ssdc.common.model.entity.UacQidLink;
import uk.gov.ons.ssdc.supporttool.model.dto.messaging.DeactivateUacDTO;
import uk.gov.ons.ssdc.supporttool.model.dto.messaging.EventDTO;
import uk.gov.ons.ssdc.supporttool.model.dto.messaging.EventHeaderDTO;
import uk.gov.ons.ssdc.supporttool.model.dto.messaging.PayloadDTO;
import uk.gov.ons.ssdc.supporttool.model.repository.UacQidLinkRepository;
import uk.gov.ons.ssdc.supporttool.security.UserIdentity;
import uk.gov.ons.ssdc.supporttool.utility.EventHelper;

@RestController
@RequestMapping(value = "/api/deactivateUac")
public class DeactivateUacEndpoint {

  private final UserIdentity userIdentity;
  private final UacQidLinkRepository qidLinkRepository;
  private final PubSubTemplate pubSubTemplate;

  @Value("${queueconfig.deactivate-uac-topic}")
  private String deactivateUacTopic;

  @Value("${queueconfig.shared-pubsub-project}")
  private String sharedPubsubProject;

  public DeactivateUacEndpoint(
      UserIdentity userIdentity,
      UacQidLinkRepository qidLinkRepository,
      PubSubTemplate pubSubTemplate) {
    this.userIdentity = userIdentity;
    this.qidLinkRepository = qidLinkRepository;
    this.pubSubTemplate = pubSubTemplate;
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
        DEACTIVATE_UAC);

    EventDTO event = new EventDTO();

    EventHeaderDTO header = EventHelper.createEventDTO(deactivateUacTopic, userEmail);
    event.setHeader(header);

    PayloadDTO payload = new PayloadDTO();
    DeactivateUacDTO deactivateUac = new DeactivateUacDTO();
    deactivateUac.setQid(qid);
    payload.setDeactivateUac(deactivateUac);
    event.setPayload(payload);

    String topic = toProjectTopicName(deactivateUacTopic, sharedPubsubProject).toString();
    pubSubTemplate.publish(topic, event);
  }
}
