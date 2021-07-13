package uk.gov.ons.ssdc.supporttool.model.ui.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.ons.ssdc.supporttool.model.messaging.dto.CollectionCase;
import uk.gov.ons.ssdc.supporttool.model.messaging.dto.RefusalTypeDTO;

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class Refusal {
  private RefusalTypeDTO type;
  private String agentId;
  private String callId;
}
