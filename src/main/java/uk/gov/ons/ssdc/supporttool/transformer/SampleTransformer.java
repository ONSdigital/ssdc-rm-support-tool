package uk.gov.ons.ssdc.supporttool.transformer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import uk.gov.ons.ssdc.common.model.entity.Job;
import uk.gov.ons.ssdc.common.validation.ColumnValidator;
import uk.gov.ons.ssdc.supporttool.model.dto.messaging.NewCase;

public class SampleTransformer implements Transformer {
  @Override
  public Object transformRow(
      Map<String, String> rowData, Job job, ColumnValidator[] columnValidators) {
    NewCase newCase = new NewCase();
    newCase.setCaseId(UUID.randomUUID());
    newCase.setCollectionExerciseId(job.getCollectionExercise().getId());

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

    newCase.setSample(nonSensitiveSampleData);
    newCase.setSampleSensitive(sensitiveSampleData);

    newCase.setJobId(job.getId());
    newCase.setOriginatingUser(job.getCreatedBy());
    return newCase;
  }
}
