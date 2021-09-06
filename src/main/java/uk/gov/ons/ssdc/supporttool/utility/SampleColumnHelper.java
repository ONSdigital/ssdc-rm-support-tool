package uk.gov.ons.ssdc.supporttool.utility;

import uk.gov.ons.ssdc.common.model.entity.Job;
import uk.gov.ons.ssdc.common.model.entity.Survey;
import uk.gov.ons.ssdc.common.validation.ColumnValidator;

public class SampleColumnHelper {
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
}
