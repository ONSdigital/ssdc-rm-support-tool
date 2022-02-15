package uk.gov.ons.ssdc.supporttool.model.dto.messaging;

import java.util.UUID;
import lombok.Data;

@Data
public class PrintFulfilmentDTO {
  private UUID caseId;
  private String packCode;
  private Object uacMetadata;
  private Object personalisation;
}
