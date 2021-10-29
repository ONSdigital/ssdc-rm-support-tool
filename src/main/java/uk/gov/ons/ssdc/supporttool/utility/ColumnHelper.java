package uk.gov.ons.ssdc.supporttool.utility;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import uk.gov.ons.ssdc.common.model.entity.Survey;
import uk.gov.ons.ssdc.common.validation.ColumnValidator;

public class ColumnHelper {
  public static String[] getExpectedColumns(ColumnValidator[] validationRules) {
    String[] expectedColumns = new String[validationRules.length];

    for (int i = 0; i < expectedColumns.length; i++) {
      expectedColumns[i] = validationRules[i].getColumnName();
    }

    return expectedColumns;
  }

  public static Set<String> getSurveyColumns(Survey survey, boolean sensitive) {
    return Arrays.stream(survey.getSampleValidationRules())
        .filter(columnValidator -> columnValidator.isSensitive() == sensitive)
        .map(columnValidator -> columnValidator.getColumnName())
        .collect(Collectors.toSet());
  }
}
