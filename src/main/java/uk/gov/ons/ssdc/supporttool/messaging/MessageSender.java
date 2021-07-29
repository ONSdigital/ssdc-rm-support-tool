package uk.gov.ons.ssdc.supporttool.messaging;

import java.util.UUID;
import org.springframework.stereotype.Component;
import uk.gov.ons.ssdc.supporttool.model.entity.MessageToSend;
import uk.gov.ons.ssdc.supporttool.model.repository.MessageToSendRepository;
import uk.gov.ons.ssdc.supporttool.utility.JsonHelper;

@Component
public class MessageSender {
  private final MessageToSendRepository messageToSendRepository;

  public MessageSender(MessageToSendRepository messageToSendRepository) {
    this.messageToSendRepository = messageToSendRepository;
  }

  public void sendMessage(String destinationTopic, Object message) {
    MessageToSend messageToSend = new MessageToSend();
    messageToSend.setId(UUID.randomUUID());
    messageToSend.setDestinationTopic(destinationTopic);
    messageToSend.setMessageBody(JsonHelper.convertObjectToJson(message));

    messageToSendRepository.saveAndFlush(messageToSend);
  }
}
