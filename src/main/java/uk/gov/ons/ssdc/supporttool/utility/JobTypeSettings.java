package uk.gov.ons.ssdc.supporttool.utility;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import uk.gov.ons.ssdc.common.model.entity.UserGroupAuthorisedActivityType;
import uk.gov.ons.ssdc.common.validation.ColumnValidator;
import uk.gov.ons.ssdc.supporttool.transformer.Transformer;

@Data
public class JobTypeSettings {
  private Transformer transformer;
  private ColumnValidator[] columnValidators;
  private Map<String, ColumnValidator[]> sampleColumnValidators;
  private Map<String, ColumnValidator[]> sensitiveeColumnValidators;
  private String topic;
  private UserGroupAuthorisedActivityType fileLoadPermission;
  private UserGroupAuthorisedActivityType fileViewProgressPersmission;

  public ColumnValidator[] getColumnValidators() {
    return columnValidators;
  }

  public void setSampleAndSensitiveDataColumnMaps(ColumnValidator[] columnValidators) {
    sampleColumnValidators = new HashMap<>();
    sensitiveeColumnValidators = new HashMap<>();

    for (ColumnValidator columnValidator : columnValidators) {
      if (columnValidator.isSensitive()) {
        sensitiveeColumnValidators.put(
            columnValidator.getColumnName(), new ColumnValidator[] {columnValidator});
      } else {
        sampleColumnValidators.put(
            columnValidator.getColumnName(), new ColumnValidator[] {columnValidator});
      }
    }
  }

  public ColumnValidator[] getColumnValidatorForSampleOrSensitive(
      String fieldToUpdate, boolean sensitive) {
    // There's 2 stages of validation here.  We'll return the actual validation rules.
    // 1st we must validate whether or not the column actually exists. will return null if column
    // not where expected
    if (sensitive) {
      return sensitiveeColumnValidators.get(fieldToUpdate);
    } else {
      return sampleColumnValidators.get(fieldToUpdate);
    }
  }
}
