package uk.gov.ons.ssdc.supporttool.model.dto.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class RequestPayloadDTO {
  private SmsFulfilment smsFulfilment;
  private EmailFulfilment emailFulfilment;
}
