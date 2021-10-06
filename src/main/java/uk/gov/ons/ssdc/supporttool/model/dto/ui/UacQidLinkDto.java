package uk.gov.ons.ssdc.supporttool.model.dto.ui;

import java.time.OffsetDateTime;
import lombok.Data;

@Data
public class UacQidLinkDto {
  private String qid;
  private boolean active = true;
  private OffsetDateTime createdAt;
  private OffsetDateTime lastUpdatedAt;
  private Object metadata;
  private boolean receiptReceived = false;
  private boolean eqLaunched = false;
}
