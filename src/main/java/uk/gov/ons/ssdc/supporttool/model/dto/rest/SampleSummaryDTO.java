package uk.gov.ons.ssdc.supporttool.model.dto.rest;

import lombok.Data;

@Data
public class SampleSummaryDTO {
  private int totalSampleUnits;
  private int expectedCollectionInstruments;
}
