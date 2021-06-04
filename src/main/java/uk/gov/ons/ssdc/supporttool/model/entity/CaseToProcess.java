package uk.gov.ons.ssdc.supporttool.model.entity;

import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import lombok.Data;

@Data
@Entity
public class CaseToProcess {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(columnDefinition = "serial")
  private long id;

  @ManyToOne private Case caze;

  @ManyToOne private WaveOfContact waveOfContact;

  @Column private UUID batchId;

  @Column(nullable = false)
  private int batchQuantity;
}
