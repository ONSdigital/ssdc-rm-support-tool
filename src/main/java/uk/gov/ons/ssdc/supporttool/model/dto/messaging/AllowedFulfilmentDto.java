package uk.gov.ons.ssdc.supporttool.model.dto.messaging;

import lombok.Data;

@Data
public class AllowedFulfilmentDto {
  private String packCode;
  private String description;
  private Object metadata;
}
