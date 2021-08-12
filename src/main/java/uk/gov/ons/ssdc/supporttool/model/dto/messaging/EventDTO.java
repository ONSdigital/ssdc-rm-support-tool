package uk.gov.ons.ssdc.supporttool.model.dto.messaging;

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
  private String originatingUser;
}
