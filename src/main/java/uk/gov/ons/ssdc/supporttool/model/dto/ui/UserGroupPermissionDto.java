package uk.gov.ons.ssdc.supporttool.model.dto.ui;

import java.util.UUID;
import lombok.Data;
import uk.gov.ons.ssdc.common.model.entity.UserGroupAuthorisedActivityType;

@Data
public class UserGroupPermissionDto {
  private UUID id;
  private UUID groupId;
  private UUID surveyId;
  private String surveyName;
  private UserGroupAuthorisedActivityType authorisedActivity;
}
