package uk.gov.ons.ssdc.supporttool.model.dto.ui;

import java.util.UUID;
import lombok.Data;

@Data
public class UserGroupAdminDto {
  private UUID id;
  private UUID userId;
  private String userEmail;
  private UUID groupId;
}
