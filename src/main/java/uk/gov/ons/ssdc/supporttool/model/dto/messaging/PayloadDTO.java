package uk.gov.ons.ssdc.supporttool.model.dto.messaging;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class PayloadDTO {
  private DeactivateUacDTO deactivateUac;
  private RefusalDTO refusal;
  private InvalidCaseDTO invalidCase;
  private PrintFulfilmentDTO printFulfilment;
  private UpdateSample updateSample;
  private UpdateSampleSensitive updateSampleSensitive;
  private SurveyUpdateDto surveyUpdate;
  private CollectionExerciseUpdateDTO collectionExerciseUpdate;
}
