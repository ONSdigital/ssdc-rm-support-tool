package uk.gov.ons.ssdc.supporttool.utility;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.Test;
import uk.gov.ons.ssdc.supporttool.model.dto.messaging.EventDTO;
import uk.gov.ons.ssdc.supporttool.model.dto.messaging.EventTypeDTO;

public class EventHelperTest {

  @Test
  public void testCreateEventDTOWithEventType() {
    EventDTO eventDTO = EventHelper.createEventDTO(EventTypeDTO.DEACTIVATE_UAC);

    assertThat(eventDTO.getChannel()).isEqualTo("RM");
    assertThat(eventDTO.getSource()).isEqualTo("SUPPORT_TOOL");
    assertThat(eventDTO.getDateTime()).isInstanceOf(OffsetDateTime.class);
    assertThat(eventDTO.getTransactionId()).isInstanceOf(UUID.class);
    assertThat(eventDTO.getType()).isEqualTo(EventTypeDTO.DEACTIVATE_UAC);
  }

  @Test
  public void testCreateEventDTOWithEventTypeChannelAndSource() {
    EventDTO eventDTO =
        EventHelper.createEventDTO(EventTypeDTO.DEACTIVATE_UAC, "CHANNEL", "SOURCE");

    assertThat(eventDTO.getChannel()).isEqualTo("CHANNEL");
    assertThat(eventDTO.getSource()).isEqualTo("SOURCE");
    assertThat(eventDTO.getDateTime()).isInstanceOf(OffsetDateTime.class);
    assertThat(eventDTO.getTransactionId()).isInstanceOf(UUID.class);
    assertThat(eventDTO.getType()).isEqualTo(EventTypeDTO.DEACTIVATE_UAC);
  }
}
