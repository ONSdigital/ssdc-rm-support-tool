package uk.gov.ons.ssdc.supporttool.model.dto.ui;

import lombok.Data;

import java.util.Map;

@Data
public class SmsFulfilmentAction {
  private String packCode;
  private String phoneNumber;
  private Object uacMetadata;
  private Map<String, String> personalisation;
}
