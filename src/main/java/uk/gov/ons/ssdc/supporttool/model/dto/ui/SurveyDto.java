package uk.gov.ons.ssdc.supporttool.model.dto.ui;

import java.util.UUID;
import lombok.Data;
import src.main.java.uk.gov.ons.ssdc.common.model.entity.ScheduleTemplate;
import uk.gov.ons.ssdc.common.validation.ColumnValidator;

@Data
public class SurveyDto {
  private UUID id;
  private String name;
  private ColumnValidator[] sampleValidationRules;
  private boolean sampleWithHeaderRow;
  private char sampleSeparator;
  private String sampleDefinitionUrl;
  private Object metadata;
  private ScheduleTemplate scheduleTemplate;
}
