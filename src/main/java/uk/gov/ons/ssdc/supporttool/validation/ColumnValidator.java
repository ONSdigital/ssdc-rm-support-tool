package uk.gov.ons.ssdc.supporttool.validation;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ColumnValidator {

  private final String columnName;
  private final Rule[] rules;

  public ColumnValidator(String columnName, Rule[] rules) {
    this.columnName = columnName;
    this.rules = rules;
  }

  public Optional<String> validateRow(Map<String, String> rowData) {
    List<String> validationErrors = new LinkedList<>();

    for (Rule rule : rules) {
      String dataToValidate = rowData.get(columnName);

      Optional<String> validationError = rule.checkValidity(dataToValidate);
      if (validationError.isPresent()) {
        validationErrors.add(
            "Column '"
                + columnName
                + "' value '"
                + dataToValidate
                + "' validation error: "
                + validationError.get());
      }
    }

    if (validationErrors.size() > 0) {
      return Optional.of(String.join(", ", validationErrors));
    }

    return Optional.empty();
  }
}
