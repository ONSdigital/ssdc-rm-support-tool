package uk.gov.ons.ssdc.supporttool.model.dto.ui;

import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.ons.ssdc.common.model.entity.SmsTemplate;

@Data
@NoArgsConstructor
public class SmsTemplateDto {
  private String packCode;
  private String[] template;
  private UUID notifyTemplateId;
  private String description;
  private Object metadata;
  private String notifyServiceRef;

  public SmsTemplateDto(SmsTemplate smsTemplate) {
    packCode = smsTemplate.getPackCode();
    template = smsTemplate.getTemplate();
    notifyTemplateId = smsTemplate.getNotifyTemplateId();
    description = smsTemplate.getDescription();
    metadata = smsTemplate.getMetadata();
    notifyServiceRef = smsTemplate.getNotifyServiceRef();
  }
}
