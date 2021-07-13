package uk.gov.ons.ssdc.supporttool.transformer;

import java.util.Map;
import uk.gov.ons.ssdc.supporttool.model.entity.Job;
import uk.gov.ons.ssdc.supporttool.validation.ColumnValidator;

public interface Transformer {
  Object transformRow(Map<String, String> rowData, Job job, ColumnValidator[] columnValidators);
}
