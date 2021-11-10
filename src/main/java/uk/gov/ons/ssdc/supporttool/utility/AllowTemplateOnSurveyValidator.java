package uk.gov.ons.ssdc.supporttool.utility;

import static uk.gov.ons.ssdc.supporttool.utility.ColumnHelper.getSurveyColumns;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import uk.gov.ons.ssdc.common.model.entity.Survey;

public class AllowTemplateOnSurveyValidator {
  private static final Set<String> OTHER_ALLOWABLE_COLUMNS =
      Set.of("__uac__", "__qid__", "__caseref__", "__ras_rm_iac__");

  public static Optional<String> validate(Survey survey, Set<String> templateColumns) {
    Set<String> surveyColumns = getSurveyColumns(survey, false);
    Set<String> sensitiveSurveyColumns =
        getSurveyColumns(survey, true).stream()
            .map(column -> "__sensitive__." + column)
            .collect(Collectors.toSet());
    Set<String> surveyColumnsPlusOtherAllowableColumns = new HashSet<>();

    surveyColumnsPlusOtherAllowableColumns.addAll(surveyColumns);
    surveyColumnsPlusOtherAllowableColumns.addAll(sensitiveSurveyColumns);
    surveyColumnsPlusOtherAllowableColumns.addAll(OTHER_ALLOWABLE_COLUMNS);

    if (!surveyColumnsPlusOtherAllowableColumns.containsAll(templateColumns)) {
      Set<String> printTemplateColumnsNotAllowed = new HashSet<>();
      printTemplateColumnsNotAllowed.addAll(templateColumns);
      printTemplateColumnsNotAllowed.removeAll(surveyColumnsPlusOtherAllowableColumns);
      String errorMessage =
          "Survey is missing columns: " + String.join(", ", printTemplateColumnsNotAllowed);
      return Optional.of(errorMessage);
    }

    return Optional.empty();
  }
}
