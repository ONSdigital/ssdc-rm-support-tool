package uk.gov.ons.ssdc.supporttool.utility;

import uk.gov.ons.ssdc.supporttool.model.entity.Job;
import uk.gov.ons.ssdc.supporttool.validation.ColumnValidator;

public class SampleColumnHelper {
  public static String[] getExpectedColumns(Job job) {
    ColumnValidator[] sampleValidationRules =
        job.getCollectionExercise().getSurvey().getSampleValidationRules();

    String[] expectedColumns = new String[sampleValidationRules.length];

    for (int i = 0; i < expectedColumns.length; i++) {
      expectedColumns[i] = sampleValidationRules[i].getColumnName();
    }

    return expectedColumns;
  }
}
