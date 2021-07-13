package uk.gov.ons.ssdc.supporttool.model.messaging.dto;

import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Data;

@Data
public class EventDTO {
  private EventTypeDTO type;
  private String source;
  private String channel;
  private OffsetDateTime dateTime;
  private UUID transactionId;
}
