package uk.gov.ons.ssdc.supporttool.model.dto.ui;

import java.util.UUID;
import lombok.Data;

@Data
public class EmailTemplateDto {
  private String packCode;
  private String[] template;
  private UUID notifyTemplateId;
  private String description;
  private Object metadata;
}
