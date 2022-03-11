package uk.gov.ons.ssdc.supporttool.model.dto.ui;

import lombok.Data;

import java.util.Map;

@Data
public class EmailFulfilmentAction {
  private String packCode;
  private String email;
  private Object uacMetadata;
  private Map<String, String> personalisation;
}
