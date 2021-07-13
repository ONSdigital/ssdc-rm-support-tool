package uk.gov.ons.ssdc.supporttool.model.messaging.dto;

import java.util.UUID;
import lombok.Data;

@Data
public class FulfilmentDTO {
  private UUID caseId;
  private String packCode;
}
