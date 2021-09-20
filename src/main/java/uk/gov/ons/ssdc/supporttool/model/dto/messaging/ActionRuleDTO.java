package uk.gov.ons.ssdc.supporttool.model.dto.messaging;

import java.time.OffsetDateTime;
import lombok.Data;
import uk.gov.ons.ssdc.common.model.entity.ActionRuleType;

@Data
public class ActionRuleDTO {

  private String collectionExerciseId;

  private String packCode;

  private String classifiers;

  private ActionRuleType type;

  private OffsetDateTime triggerDateTime;
}
