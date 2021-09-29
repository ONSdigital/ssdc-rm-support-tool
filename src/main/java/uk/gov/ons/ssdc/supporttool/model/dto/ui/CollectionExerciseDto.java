package uk.gov.ons.ssdc.supporttool.model.dto.ui;

import java.util.UUID;
import lombok.Data;

@Data
public class CollectionExerciseDto {
  private UUID id;
  private String name;
  private UUID surveyId;
}
