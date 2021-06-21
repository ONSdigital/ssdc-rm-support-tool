package uk.gov.ons.ssdc.supporttool.model.dto;

import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Data;

@Data
public class JobDto {
  private UUID id;

  private OffsetDateTime createdAt;
  private String createdBy;
  private OffsetDateTime lastUpdatedAt;

  private String fileName;
  private int fileRowCount;
  private int stagedRowCount;
  private int processedRowCount;
  private int rowErrorCount;

  private JobStatusDto jobStatus;

  private String fatalErrorDescription;
}
