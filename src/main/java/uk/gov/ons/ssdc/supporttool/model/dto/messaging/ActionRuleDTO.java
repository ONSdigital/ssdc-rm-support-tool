package uk.gov.ons.ssdc.supporttool.model.dto.messaging;

import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Data;
import uk.gov.ons.ssdc.common.model.entity.ActionRuleType;

@Data
public class ActionRuleDTO {

  private UUID collectionExerciseId;

  private String packCode;

  private String classifiers;

  private ActionRuleType type;

  private OffsetDateTime triggerDateTime;
}
