package uk.gov.ons.ssdc.supporttool.transformer;

import java.util.Map;
import uk.gov.ons.ssdc.common.model.entity.Job;
import uk.gov.ons.ssdc.common.validation.ColumnValidator;

public interface Transformer {
  Object transformRow(
      Map<String, String> rowData, Job job, ColumnValidator[] columnValidators, String topic);
}
