package uk.gov.ons.ssdc.supporttool.model.dto.ui;

import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Data;
import uk.gov.ons.ssdc.common.model.entity.CollectionInstrumentSelectionRule;

@Data
public class CollectionExerciseDto {
  private UUID id;
  private String name;
  private UUID surveyId;
  private String reference;
  private OffsetDateTime startDate;
  private OffsetDateTime endDate;
  private Object metadata;
  private CollectionInstrumentSelectionRule[] collectionInstrumentSelectionRules;
}
