package uk.gov.ons.ssdc.supporttool.utility;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import uk.gov.ons.ssdc.common.model.entity.Survey;

public class ColumnHelper {
  public static Set<String> getSurveyColumns(Survey survey, boolean sensitive) {
    return Arrays.stream(survey.getSampleValidationRules())
        .filter(columnValidator -> columnValidator.isSensitive() == sensitive)
        .map(columnValidator -> columnValidator.getColumnName())
        .collect(Collectors.toSet());
  }
}
