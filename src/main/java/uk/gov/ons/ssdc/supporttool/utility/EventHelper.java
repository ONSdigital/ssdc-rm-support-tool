package uk.gov.ons.ssdc.supporttool.utility;

import java.time.OffsetDateTime;
import java.util.UUID;
import uk.gov.ons.ssdc.supporttool.model.dto.messaging.EventDTO;
import uk.gov.ons.ssdc.supporttool.model.dto.messaging.EventTypeDTO;

public class EventHelper {

  private static final String EVENT_SOURCE = "SUPPORT_TOOL";
  private static final String EVENT_CHANNEL = "RM";

  private EventHelper() {
    throw new IllegalStateException("Utility class EventHelper should not be instantiated");
  }

  public static EventDTO createEventDTO(
      EventTypeDTO eventType, String eventChannel, String eventSource) {
    EventDTO eventDTO = new EventDTO();

    eventDTO.setChannel(eventChannel);
    eventDTO.setSource(eventSource);
    eventDTO.setDateTime(OffsetDateTime.now());
    eventDTO.setTransactionId(UUID.randomUUID());
    eventDTO.setType(eventType);

    return eventDTO;
  }

  public static EventDTO createEventDTO(EventTypeDTO eventType) {
    return createEventDTO(eventType, EVENT_CHANNEL, EVENT_SOURCE);
  }
}
