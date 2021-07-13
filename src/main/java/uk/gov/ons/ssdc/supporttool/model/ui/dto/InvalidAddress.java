package uk.gov.ons.ssdc.supporttool.model.ui.dto;

import lombok.Data;

@Data
public class InvalidAddress {
  private String reason;
  private String notes;
}
