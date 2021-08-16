package uk.gov.ons.ssdc.supporttool.utility;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import uk.gov.ons.ssdc.supporttool.model.dto.messaging.EventHeaderDTO;

public class EventHelperTest {

  @Test
  public void testCreateEventDTOWithEventType() {
    EventHeaderDTO eventHeader = EventHelper.createEventDTO("Test topic", "Test_user");

    assertThat(eventHeader.getChannel()).isEqualTo("RM");
    assertThat(eventHeader.getSource()).isEqualTo("SUPPORT_TOOL");
    assertThat(eventHeader.getDateTime()).isInstanceOf(OffsetDateTime.class);
    assertThat(eventHeader.getMessageId()).isInstanceOf(UUID.class);
    assertThat(eventHeader.getCorrelationId()).isInstanceOf(UUID.class);
    assertThat(eventHeader.getTopic()).isEqualTo("Test topic");
    assertThat(eventHeader.getOriginatingUser()).isEqualTo("Test_user");
  }

  @Test
  public void testCreateEventDTOWithEventTypeChannelAndSource() {
    EventHeaderDTO eventHeader =
        EventHelper.createEventDTO("Test topic", "CHANNEL", "SOURCE", "Test_user");

    assertThat(eventHeader.getChannel()).isEqualTo("CHANNEL");
    assertThat(eventHeader.getSource()).isEqualTo("SOURCE");
    assertThat(eventHeader.getDateTime()).isInstanceOf(OffsetDateTime.class);
    assertThat(eventHeader.getMessageId()).isInstanceOf(UUID.class);
    assertThat(eventHeader.getCorrelationId()).isInstanceOf(UUID.class);
    assertThat(eventHeader.getTopic()).isEqualTo("Test topic");
    assertThat(eventHeader.getOriginatingUser()).isEqualTo("Test_user");
  }
}
