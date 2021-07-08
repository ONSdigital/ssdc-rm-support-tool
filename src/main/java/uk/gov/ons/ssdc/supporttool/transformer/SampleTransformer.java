package uk.gov.ons.ssdc.supporttool.transformer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import uk.gov.ons.ssdc.supporttool.model.dto.Sample;
import uk.gov.ons.ssdc.supporttool.model.entity.Job;
import uk.gov.ons.ssdc.supporttool.validation.ColumnValidator;

public class SampleTransformer implements Transformer {
  @Override
  public Object transformRow(Map<String, String> rowData, Job job,
      ColumnValidator[] columnValidators) {
    Sample sample = new Sample();
    sample.setCaseId(UUID.randomUUID());
    sample.setCollectionExerciseId(job.getCollectionExercise().getId());

    Map<String, String> nonSensitiveSampleData = new HashMap<>();
    Map<String, String> sensitiveSampleData = new HashMap<>();

    for (ColumnValidator columnValidator : columnValidators) {
      String columnName = columnValidator.getColumnName();
      String sampleValue = rowData.get(columnName);

      if (columnValidator.isSensitive()) {
        sensitiveSampleData.put(columnName, sampleValue);
      } else {
        nonSensitiveSampleData.put(columnName, sampleValue);
      }
    }

    sample.setSample(nonSensitiveSampleData);
    sample.setSampleSensitive(sensitiveSampleData);
    return sample;
  }
}
