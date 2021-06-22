package uk.gov.ons.ssdc.supporttool.utility;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.ons.ssdc.supporttool.model.entity.Job;
import uk.gov.ons.ssdc.supporttool.validation.ColumnValidator;

public class SampleColumnHelper {
  private static final ObjectMapper OBJECT_MAPPER = ObjectMapperFactory.objectMapper();

  public static String[] getExpectedColumns(Job job) {
    ColumnValidator[] sampleValidationRules;
    try {
      sampleValidationRules =
          OBJECT_MAPPER.readValue(
              job.getCollectionExercise().getSurvey().getSampleValidationRules(),
              ColumnValidator[].class);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Validation JSON could not be unmarshalled", e);
    }

    String[] expectedColumns = new String[sampleValidationRules.length];

    for (int i = 0; i < expectedColumns.length; i++) {
      expectedColumns[i] = sampleValidationRules[i].getColumnName();
    }

    return expectedColumns;
  }
}
