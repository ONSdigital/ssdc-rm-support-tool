package uk.gov.ons.ssdc.supporttool.model.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import lombok.Data;
import org.hibernate.annotations.Type;

@Data
@Entity
public class PrintTemplate {
  @Id private String packCode;

  @Type(type = "jsonb")
  @Column(nullable = false, columnDefinition = "jsonb")
  private String[] template;

  @Column(nullable = false)
  private String printSupplier;
}
