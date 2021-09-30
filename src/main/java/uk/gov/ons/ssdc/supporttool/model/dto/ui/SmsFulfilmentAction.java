package uk.gov.ons.ssdc.supporttool.model.dto.ui;

import java.util.Map;
import lombok.Data;

@Data
public class SmsFulfilmentAction {
  private String packCode;
  private String phoneNumber;
  private Map<String, String> uacMetadata;
}
