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
  private UpdateSampleSensitive updateSampleSensitive;
  private NewCase newCase;
  private SurveyUpdateDto surveyUpdate;
  private CollectionExerciseUpdateDTO collectionExerciseUpdate;
  private UpdateSample updateSample;
}
