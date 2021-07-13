package uk.gov.ons.ssdc.supporttool.transformer;

import java.util.Map;
import java.util.UUID;
import uk.gov.ons.ssdc.supporttool.model.messaging.dto.Sample;
import uk.gov.ons.ssdc.supporttool.model.entity.Job;

public class SampleTransformer implements Transformer {
  @Override
  public Object transformRow(Map<String, String> rowData, Job job) {
    Sample sample = new Sample();
    sample.setCaseId(UUID.randomUUID());
    sample.setCollectionExerciseId(job.getCollectionExercise().getId());
    sample.setSample(rowData);
    return sample;
  }
}
