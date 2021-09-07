package uk.gov.ons.ssdc.supporttool.utility;

import static uk.gov.ons.ssdc.supporttool.utility.SampleColumnHelper.getExpectedColumns;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import uk.gov.ons.ssdc.common.model.entity.PrintTemplate;
import uk.gov.ons.ssdc.common.model.entity.Survey;

public class AllowPrintTemplateOnSurveyValidator {
  private static final Set<String> OTHER_ALLOWABLE_COLUMNS =
      Set.of("__uac__", "__qid__", "__caseref__");

  public static Optional<String> validate(Survey survey, PrintTemplate printTemplate) {
    Set<String> surveyColumns = Set.of(getExpectedColumns(survey));
    Set<String> printTemplateColumns = Set.of(printTemplate.getTemplate());

    Set<String> surveyColumnsPlusOtherAllowableColumns = new HashSet<>();
    surveyColumnsPlusOtherAllowableColumns.addAll(surveyColumns);
    surveyColumnsPlusOtherAllowableColumns.addAll(OTHER_ALLOWABLE_COLUMNS);

    if (!surveyColumnsPlusOtherAllowableColumns.containsAll(printTemplateColumns)) {
      Set<String> printTemplateColumnsNotAllowed = new HashSet<>();
      printTemplateColumnsNotAllowed.addAll(printTemplateColumns);
      printTemplateColumnsNotAllowed.removeAll(surveyColumnsPlusOtherAllowableColumns);
      String errorMessage =
          "Survey is missing columns: " + String.join(", ", printTemplateColumnsNotAllowed);
      return Optional.of(errorMessage);
    }

    return Optional.empty();
  }
}
