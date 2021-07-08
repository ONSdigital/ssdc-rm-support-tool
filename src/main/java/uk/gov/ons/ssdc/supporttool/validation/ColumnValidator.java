package uk.gov.ons.ssdc.supporttool.validation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ColumnValidator implements Serializable {

  private final String columnName;

  private final boolean sensitive;

  @JsonTypeInfo(
      use = JsonTypeInfo.Id.CLASS,
      include = JsonTypeInfo.As.PROPERTY,
      property = "className")
  private Rule[] rules;

  @JsonCreator
  public ColumnValidator(
      @JsonProperty("columnName") String columnName,
      @JsonProperty("sensitive") boolean sensitive,
      @JsonProperty("rules") Rule[] rules) {
    this.columnName = columnName;
    this.sensitive = sensitive;
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

  public String getColumnName() {
    return columnName;
  }

  public boolean isSensitive() {
    return sensitive;
  }
}
