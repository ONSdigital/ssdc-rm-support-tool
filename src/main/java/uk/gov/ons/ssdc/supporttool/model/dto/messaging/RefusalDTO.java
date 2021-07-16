package uk.gov.ons.ssdc.supporttool.model.dto.messaging;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class RefusalDTO {
  private RefusalTypeDTO type;
  private String agentId;
  private String callId;
  private CollectionCase collectionCase;
}