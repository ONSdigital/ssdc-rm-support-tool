package uk.gov.ons.ssdc.supporttool.utility;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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
  private String topic;
  private UserGroupAuthorisedActivityType fileLoadPermission;
  private UserGroupAuthorisedActivityType fileViewProgressPersmission;

  private Map<String, ColumnValidator[]> sampleOrSensitiveValidationsMap;


  public ColumnValidator[] getColumnValidators() {
    return columnValidators;
  }

  public void setSampleAndSensitiveDataColumnMaps(ColumnValidator[] columnValidators, boolean jobSensitive) {
    sampleOrSensitiveValidationsMap = new HashMap<>();
    String[] allValidateColumns = Arrays.stream(columnValidators)
            .filter(columnValidator -> columnValidator.isSensitive() == jobSensitive)
            .map(ColumnValidator::getColumnName)
            .toArray(String[]::new);

    for (ColumnValidator columnValidator : columnValidators) {
      if(jobSensitive == columnValidator.isSensitive()) {
        sampleOrSensitiveValidationsMap.put(columnValidator.getColumnName(),
                createColumnValidation(allValidateColumns, columnValidator.getRules()));
      }
    }
  }

  private ColumnValidator[] createColumnValidation(String[] allowedColumns, Rule[] newValueRules) {
    Rule[] caseExistsRules = {new CaseExistsRule()};
    ColumnValidator caseExistsValidator = new ColumnValidator("caseId", false, caseExistsRules);

    Rule[] fieldToUpdateRule = {new InSetRule(allowedColumns)};
    ColumnValidator fieldToUpdateValidator =
            new ColumnValidator("fieldToUpdate", false, fieldToUpdateRule);

    ColumnValidator newValueValidator =
            new ColumnValidator("newValue", false, newValueRules);

    return new ColumnValidator[] {caseExistsValidator, fieldToUpdateValidator, newValueValidator};
  }

  public ColumnValidator[] getColumnValidatorForSampleOrSensitiveDataRows(String columnName) {
     return sampleOrSensitiveValidationsMap.get(columnName);
  }
}
