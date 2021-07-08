package uk.gov.ons.ssdc.supporttool.model.dto;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

public class CaseContainerDto {
  public UUID id;
  public String caseRef;
  public Map<String, String> sample;
  public OffsetDateTime createdAt;
  public OffsetDateTime lastUpdatedAt;
  public String refusalReceived;
  public boolean addressInvalid;
  public boolean receiptReceived;
  public boolean surveyLaunched;
 }
