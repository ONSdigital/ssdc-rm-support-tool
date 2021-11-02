package uk.gov.ons.ssdc.supporttool.utility;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import uk.gov.ons.ssdc.common.model.entity.CollectionExercise;
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
  private String[] allowedSensitiveColumns;
  private String topic;
  private UserGroupAuthorisedActivityType fileLoadPermission;
  private UserGroupAuthorisedActivityType fileViewProgressPersmission;

  public ColumnValidator[] getColumnValidators() {
    return columnValidators;
  }

  //  TODO: This can be split into  one for each.  Any Job will only need one or the other.
  // Or we just an 'allowedColumns single validator based on sensitive param.
  // Other option is to set up a nice pre populated Map of all the columns and their rules
  // here? Then just access that, rather than configure on the fly, potentially 1000s of times
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

    allowedSensitiveColumns =
        sensitiveColumnValidators.keySet().toArray(new String[sensitiveColumnValidators.size()]);
  }

  public ColumnValidator[] getColumnValidatorForSampleOrSensitive(
      String fieldToUpdate, boolean sensitive, CollectionExercise collectionExercise) {

    if (sensitive) {
      return getColumnValidatorForColumnNameAndItsValidators(
          allowedSensitiveColumns, sensitiveColumnValidators, fieldToUpdate, collectionExercise);
    } else {
      return getColumnValidatorForColumnNameAndItsValidators(
          allowedSampleColumnNames, sampleColumnValidators, fieldToUpdate, collectionExercise);
    }
  }

  private ColumnValidator[] getColumnValidatorForColumnNameAndItsValidators(
      String[] allowedColumns,
      Map<String, ColumnValidator[]> allValidators,
      String fieldToUpdate,
      CollectionExercise collectionExercise) {
    Rule[] caseExistsRules = {new CaseExistsRule()};
    ColumnValidator caseExistsValidator = new ColumnValidator("caseId", false, caseExistsRules);

    Rule[] fieldToUpdateRule = {new InSetRule(allowedColumns)};
    ColumnValidator fieldToUpdateValidator =
        new ColumnValidator("fieldToUpdate", false, fieldToUpdateRule);

    ColumnValidator[] validatorsForField = allValidators.get(fieldToUpdate);
    ColumnValidator newValueValidator =
        new ColumnValidator("newValue", false, validatorsForField[0].getRules());

    return new ColumnValidator[] {caseExistsValidator, fieldToUpdateValidator, newValueValidator};
  }
}
