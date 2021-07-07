package uk.gov.ons.ssdc.supporttool.model.entity;

import java.util.List;
import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import lombok.Data;
import lombok.ToString;

@ToString(onlyExplicitlyIncluded = true) // Bidirectional relationship causes IDE stackoverflow
@Entity
@Data
public class UserGroup {
  @Id private UUID id;

  @OneToMany(mappedBy = "group")
  private List<UserGroupMember> members;

  @OneToMany(mappedBy = "group")
  private List<UserGroupAdmin> admins;

  @OneToMany(mappedBy = "group")
  private List<UserGroupPermission> permissions;
}
