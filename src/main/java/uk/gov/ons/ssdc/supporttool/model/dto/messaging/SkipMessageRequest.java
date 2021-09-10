package uk.gov.ons.ssdc.supporttool.model.dto.messaging;

import lombok.Data;

@Data
public class SkipMessageRequest {
  private String messageHash;
  private String skippingUser;
}
