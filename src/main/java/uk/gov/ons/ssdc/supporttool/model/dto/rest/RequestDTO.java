package uk.gov.ons.ssdc.supporttool.model.dto.rest;

import lombok.Data;

@Data
public class RequestDTO {
  private RequestHeaderDTO header;
  private RequestPayloadDTO payload;
}
