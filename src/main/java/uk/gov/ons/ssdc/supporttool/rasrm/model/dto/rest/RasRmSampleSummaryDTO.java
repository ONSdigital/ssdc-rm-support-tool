package uk.gov.ons.ssdc.supporttool.rasrm.model.dto.rest;

import lombok.Data;

@Data
public class RasRmSampleSummaryDTO {
  private int totalSampleUnits;
  private int expectedCollectionInstruments;
}
