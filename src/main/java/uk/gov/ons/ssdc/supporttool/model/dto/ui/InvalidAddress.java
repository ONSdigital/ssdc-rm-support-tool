package uk.gov.ons.ssdc.supporttool.model.dto.ui;

import lombok.Data;

@Data
public class InvalidAddress {
  private String reason;
  private String notes;
}
