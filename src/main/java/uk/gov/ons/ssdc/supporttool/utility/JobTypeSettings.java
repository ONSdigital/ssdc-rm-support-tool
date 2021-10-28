package uk.gov.ons.ssdc.supporttool.utility;

import lombok.Data;
import uk.gov.ons.ssdc.common.model.entity.UserGroupAuthorisedActivityType;
import uk.gov.ons.ssdc.common.validation.ColumnValidator;
import uk.gov.ons.ssdc.supporttool.transformer.Transformer;

@Data
public class JobTypeSettings {
  private Transformer transformer;
  private ColumnValidator[] columnValidators;
  private String topic;
  private UserGroupAuthorisedActivityType fileLoadPermission;
  private UserGroupAuthorisedActivityType fileViewProgressPersmission;
}
