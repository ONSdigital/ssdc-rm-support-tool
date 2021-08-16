package uk.gov.ons.ssdc.supporttool.model.dto.messaging;

import java.util.UUID;
import lombok.Data;

@Data
public class AllowPrintTemplateOnSurvey {
  private UUID surveyId;
  private String packCode;
}
