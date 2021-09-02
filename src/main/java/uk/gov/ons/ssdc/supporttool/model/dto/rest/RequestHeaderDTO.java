package uk.gov.ons.ssdc.supporttool.model.dto.rest;

import java.util.UUID;
import lombok.Data;

@Data
public class RequestHeaderDTO {
  private String source;
  private String channel;
  private UUID correlationId;
  private String originatingUser;
}
