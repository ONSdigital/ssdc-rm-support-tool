package uk.gov.ons.ssdc.supporttool.utility;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import uk.gov.ons.ssdc.common.model.entity.Job;
import uk.gov.ons.ssdc.common.model.entity.Survey;
import uk.gov.ons.ssdc.common.validation.ColumnValidator;

public class SampleColumnHelper {
  private final JobTypeHelper jobTypeHelper;

  public SampleColumnHelper(JobTypeHelper jobTypeHelper) {
    this.jobTypeHelper = jobTypeHelper;
  }

  public static String[] getExpectedColumns(Job job) {
    return getExpectedColumns(job.getCollectionExercise().getSurvey());
  }

  public static String[] getExpectedColumns(Survey survey) {
    ColumnValidator[] sampleValidationRules = survey.getSampleValidationRules();

    String[] expectedColumns = new String[sampleValidationRules.length];

    for (int i = 0; i < expectedColumns.length; i++) {
      expectedColumns[i] = sampleValidationRules[i].getColumnName();
    }

    return expectedColumns;
  }

  public static Set<String> getColumns(Survey survey, boolean sensitive) {
    return Arrays.stream(survey.getSampleValidationRules())
        .filter(columnValidator -> columnValidator.isSensitive() == sensitive)
        .map(columnValidator -> columnValidator.getColumnName())
        .collect(Collectors.toSet());
  }
}
