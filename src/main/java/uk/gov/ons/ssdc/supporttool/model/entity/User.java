package uk.gov.ons.ssdc.supporttool.model.entity;

import java.util.Collection;
import java.util.UUID;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "users")
public class User {
  @Id private UUID id;

  @Column private String email;

  @ElementCollection(targetClass = Survey.class)
  @CollectionTable(name = "users_survey")
  @Enumerated(EnumType.STRING)
  private Collection<Survey> surveys;
}
