package uk.gov.ons.ssdc.supporttool.model.dto.ui;

import lombok.Data;

@Data
public class PrintTemplateDto {
  private String packCode;
  private String[] template;
  private String printSupplier;
}
