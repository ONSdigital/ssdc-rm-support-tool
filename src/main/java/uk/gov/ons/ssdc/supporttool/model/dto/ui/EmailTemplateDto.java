package uk.gov.ons.ssdc.supporttool.model.dto.ui;

import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.ons.ssdc.common.model.entity.EmailTemplate;
import uk.gov.ons.ssdc.common.model.entity.SmsTemplate;

@Data
@NoArgsConstructor
public class EmailTemplateDto {
  private String packCode;
  private String[] template;
  private UUID notifyTemplateId;
  private String description;
  private Object metadata;

  public EmailTemplateDto(EmailTemplate emailTemplate) {
    packCode = emailTemplate.getPackCode();
    template = emailTemplate.getTemplate();
    notifyTemplateId = emailTemplate.getNotifyTemplateId();
    description = emailTemplate.getDescription();
    metadata = emailTemplate.getMetadata();
  }
}
