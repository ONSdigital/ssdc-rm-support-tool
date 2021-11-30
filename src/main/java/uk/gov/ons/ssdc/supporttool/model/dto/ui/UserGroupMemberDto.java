package uk.gov.ons.ssdc.supporttool.model.dto.ui;

import java.util.UUID;
import lombok.Data;

@Data
public class UserGroupMemberDto {
  private UUID id;
  private UUID userId;
  private String userEmail;
  private UUID groupId;
  private String groupName;
  private String groupDescription;
}
