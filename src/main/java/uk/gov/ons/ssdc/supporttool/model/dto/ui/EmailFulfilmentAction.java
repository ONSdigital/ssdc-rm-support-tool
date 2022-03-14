package uk.gov.ons.ssdc.supporttool.model.dto.ui;

import java.util.Map;
import lombok.Data;

@Data
public class EmailFulfilmentAction {
  private String packCode;
  private String email;
  private Object uacMetadata;
  private Map<String, String> personalisation;
}
