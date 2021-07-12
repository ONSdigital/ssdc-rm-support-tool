package uk.gov.ons.ssdc.supporttool.model.dto;

import java.util.UUID;
import lombok.Data;

@Data
public class InvalidAddressDTO {
  private String reason;
  private String notes;
  private UUID caseId;
}
