package uk.gov.ons.ssdc.supporttool.model.dto.ui;

import lombok.Data;

@Data
public class PrintFulfilment {
  private String packCode;
  private Object uacMetadata;
  private Object personalisation;
}
