package uk.gov.ons.ssdc.supporttool.model.dto.messaging;

import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Data;

@Data
public class CollectionExerciseUpdateDTO {
  private UUID collectionExerciseId;
  private String name;
  private UUID surveyId;
  private String reference;
  private OffsetDateTime startDate;
  private OffsetDateTime endDate;
  private Object metadata;
}
