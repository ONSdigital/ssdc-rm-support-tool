package uk.gov.ons.ssdc.supporttool.model.dto.messaging;

import java.util.List;
import java.util.UUID;
import lombok.Data;
import uk.gov.ons.ssdc.common.validation.ColumnValidator;

@Data
public class SurveyUpdateDto {
  private UUID surveyId;
  private String name;
  private ColumnValidator[] sampleDefinition;
  private String sampleDefinitionUrl;
  private Object metadata;
  private List<AllowedFulfilmentDto> allowedPrintFulfilments;
  private List<AllowedFulfilmentDto> allowedSmsFulfilments;
  private List<AllowedFulfilmentDto> allowedEmailFulfilments;
}
