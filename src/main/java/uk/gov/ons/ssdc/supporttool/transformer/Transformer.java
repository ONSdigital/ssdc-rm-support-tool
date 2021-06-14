package uk.gov.ons.ssdc.supporttool.transformer;

import java.util.Map;

public interface Transformer {
  Object transformRow(Map<String, String> rowData);
}
