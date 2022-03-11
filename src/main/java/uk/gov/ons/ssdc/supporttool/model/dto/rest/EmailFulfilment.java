package uk.gov.ons.ssdc.supporttool.model.dto.rest;

import java.util.Map;
import java.util.UUID;
import lombok.Data;

@Data
public class EmailFulfilment {
  private UUID caseId;
  private String email;
  private String packCode;
  private Object uacMetadata;
  private Map<String, String> personalisation;
}
