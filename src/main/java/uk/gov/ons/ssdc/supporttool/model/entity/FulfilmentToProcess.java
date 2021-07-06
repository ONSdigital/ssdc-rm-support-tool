package uk.gov.ons.ssdc.supporttool.model.entity;

import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import lombok.Data;

@Entity
@Data
public class FulfilmentToProcess {

  @Id
  @Column(columnDefinition = "serial")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @Column private String fulfilmentCode;

  @ManyToOne private Case caze;

  @Column private Integer batchQuantity;

  @Column private UUID batchId;
}
