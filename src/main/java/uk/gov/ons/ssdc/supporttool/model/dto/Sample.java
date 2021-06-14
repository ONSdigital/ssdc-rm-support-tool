package uk.gov.ons.ssdc.supporttool.model.dto;

import java.util.Map;
import java.util.UUID;
import lombok.Data;

@Data
public class Sample {
  private UUID caseId;

  private UUID collectionExerciseId;

  private Map<String, String> sample;
}
