package uk.gov.ons.ssdc.supporttool.utility;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Data;
import uk.gov.ons.ssdc.common.model.entity.UserGroupAuthorisedActivityType;
import uk.gov.ons.ssdc.common.validation.ColumnValidator;
import uk.gov.ons.ssdc.common.validation.InSetRule;
import uk.gov.ons.ssdc.common.validation.Rule;
import uk.gov.ons.ssdc.supporttool.transformer.Transformer;
import uk.gov.ons.ssdc.supporttool.validators.CaseExistsRule;

@Data
public class JobTypeSettings {
  private Transformer transformer;
  private ColumnValidator[] columnValidators;
  private Map<String, ColumnValidator[]> sampleColumnValidators;
  private Map<String, ColumnValidator[]> sensitiveColumnValidators;
  private String[] allowedSampleColumnNames;
  private String topic;
  private UserGroupAuthorisedActivityType fileLoadPermission;
  private UserGroupAuthorisedActivityType fileViewProgressPersmission;

  public ColumnValidator[] getColumnValidators() {
    return columnValidators;
  }

  public void setSampleAndSensitiveDataColumnMaps(ColumnValidator[] columnValidators) {
    sampleColumnValidators = new HashMap<>();
    sensitiveColumnValidators = new HashMap<>();

    for (ColumnValidator columnValidator : columnValidators) {
      if (columnValidator.isSensitive()) {
        sensitiveColumnValidators.put(
            columnValidator.getColumnName(), new ColumnValidator[] {columnValidator});
      } else {
        sampleColumnValidators.put(
            columnValidator.getColumnName(), new ColumnValidator[] {columnValidator});
      }
    }

    allowedSampleColumnNames =
        sampleColumnValidators.keySet().toArray(new String[sampleColumnValidators.size()]);
  }

  public ColumnValidator[] getColumnValidatorForSampleOrSensitive(
      String fieldToUpdate, boolean sensitive) {
    // There's 2 stages of validation here.  We'll return the actual validation rules.
    // 1st we must validate whether or not the column actually exists. will return null if column
    // not where expected
    if (sensitive) {
      return sensitiveColumnValidators.get(fieldToUpdate);
    } else {
      Rule[] caseExistsRules = {new CaseExistsRule()};
      ColumnValidator caseExistsValidator = new ColumnValidator("caseId", false, caseExistsRules);

      Rule[] fieldToUpdateRule = {new InSetRule(allowedSampleColumnNames)};
      ColumnValidator fieldToUpdateValidator =
          new ColumnValidator("fieldToUpdate", false, fieldToUpdateRule);

      ColumnValidator[] validatorsForField = sampleColumnValidators.get(fieldToUpdate);
      ColumnValidator newValueValidator =
          new ColumnValidator("newValue", false, validatorsForField[0].getRules());

      return new ColumnValidator[] {caseExistsValidator, fieldToUpdateValidator, newValueValidator};
    }
  }
}
