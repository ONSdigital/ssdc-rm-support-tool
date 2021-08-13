package uk.gov.ons.ssdc.supporttool.utility;

import java.time.OffsetDateTime;
import java.util.UUID;
import uk.gov.ons.ssdc.supporttool.model.dto.messaging.EventHeaderDTO;

public class EventHelper {

  private static final String EVENT_SOURCE = "SUPPORT_TOOL";
  private static final String EVENT_CHANNEL = "RM";

  private EventHelper() {
    throw new IllegalStateException("Utility class EventHelper should not be instantiated");
  }

  public static EventHeaderDTO createEventDTO(
      String topic, String eventChannel, String eventSource, String userEmail) {
    EventHeaderDTO eventHeader = new EventHeaderDTO();

    eventHeader.setChannel(eventChannel);
    eventHeader.setSource(eventSource);
    eventHeader.setDateTime(OffsetDateTime.now());
    eventHeader.setMessageId(UUID.randomUUID());
    eventHeader.setCorrelationId(UUID.randomUUID());
    eventHeader.setTopic(topic);
    eventHeader.setOriginatingUser(userEmail);

    return eventHeader;
  }

  public static EventHeaderDTO createEventDTO(String topic, String userEmail) {
    return createEventDTO(topic, EVENT_CHANNEL, EVENT_SOURCE, userEmail);
  }
}
