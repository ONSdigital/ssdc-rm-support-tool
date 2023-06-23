package uk.gov.ons.ssdc.supporttool.model.dto.ui;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import lombok.Data;
import uk.gov.ons.ssdc.common.model.entity.RefusalType;

@Data
public class CaseDto {
  private String collectionExerciseName;

  private Long caseRef;
  private OffsetDateTime createdAt;
  private OffsetDateTime lastUpdatedAt;
  private boolean receiptReceived;
  private RefusalType refusalReceived;
  private boolean invalid;
  private boolean eqLaunched;
  private Map<String, String> sample;

  private List<UacQidLinkDto> uacQidLinks;
  private List<EventDto> events;
}
