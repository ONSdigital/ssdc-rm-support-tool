package uk.gov.ons.ssdc.supporttool.model.entity;

import uk.gov.ons.ssdc.supporttool.transformer.SampleTransformer;
import uk.gov.ons.ssdc.supporttool.transformer.Transformer;
import uk.gov.ons.ssdc.supporttool.validation.AlphanumericRule;
import uk.gov.ons.ssdc.supporttool.validation.ColumnValidator;
import uk.gov.ons.ssdc.supporttool.validation.LengthRule;
import uk.gov.ons.ssdc.supporttool.validation.MandatoryRule;
import uk.gov.ons.ssdc.supporttool.validation.Rule;

public enum BulkProcess {
  SAMPLE(
      "Sample",
      new String[] {"ADDRESS_LINE1", "ADDRESS_LINE2", "ADDRESS_LINE3", "TOWN_NAME", "POSTCODE"},
      new ColumnValidator[] {
        new ColumnValidator("ADDRESS_LINE1", new Rule[] {new MandatoryRule(), new LengthRule(60)}),
        new ColumnValidator("ADDRESS_LINE2", new Rule[] {new MandatoryRule()}),
        new ColumnValidator("ADDRESS_LINE3", new Rule[] {new MandatoryRule()}),
        new ColumnValidator("POSTCODE", new Rule[] {new MandatoryRule(), new AlphanumericRule()}),
        new ColumnValidator("TOWN_NAME", new Rule[] {new MandatoryRule()})
      },
      new SampleTransformer(),
      "",
      "case.sample.inbound");

  BulkProcess(
      String title,
      String[] expectedColumns,
      ColumnValidator[] columnValidators,
      Transformer transformer,
      String targetExchange,
      String targetRoutingKey) {
    this.title = title;

    this.expectedColumns = expectedColumns;
    this.columnValidators = columnValidators;
    this.transformer = transformer;
    this.targetExchange = targetExchange;
    this.targetRoutingKey = targetRoutingKey;
  }

  private final String title;
  private final String[] expectedColumns;
  private final ColumnValidator[] columnValidators;
  private final Transformer transformer;
  private final String targetExchange;
  private final String targetRoutingKey;

  public String[] getExpectedColumns() {
    return expectedColumns;
  }

  public ColumnValidator[] getColumnValidators() {
    return columnValidators;
  }

  public Transformer getTransformer() {
    return transformer;
  }

  public String getTargetExchange() {
    return targetExchange;
  }

  public String getTargetRoutingKey() {
    return targetRoutingKey;
  }

  public String getTitle() {
    return title;
  }
}
