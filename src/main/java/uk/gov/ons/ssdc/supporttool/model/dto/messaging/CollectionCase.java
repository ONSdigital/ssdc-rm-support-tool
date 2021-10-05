package uk.gov.ons.ssdc.supporttool.model.dto.messaging;

import java.util.Map;
import java.util.UUID;
import lombok.Data;

@Data
public class CollectionCase {
  private UUID caseId;
  private boolean invalidAddress;
  private RefusalTypeDTO refusalReceived;
  private Map<String, String> sample;
}
