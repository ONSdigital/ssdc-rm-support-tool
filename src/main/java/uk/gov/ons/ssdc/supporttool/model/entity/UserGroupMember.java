package uk.gov.ons.ssdc.supporttool.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import lombok.Data;
import lombok.ToString;

@ToString(onlyExplicitlyIncluded = true) // Bidirectional relationship causes IDE stackoverflow
@Entity
@Data
public class UserGroupMember {
  @Id private UUID id;

  @ManyToOne private User user;

  @ManyToOne private UserGroup group;
}
