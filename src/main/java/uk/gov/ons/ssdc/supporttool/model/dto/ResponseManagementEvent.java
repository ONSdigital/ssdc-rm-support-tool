package uk.gov.ons.ssdc.supporttool.model.dto;

import lombok.Data;

@Data
public class ResponseManagementEvent {
  private EventDTO event;
  private PayloadDTO payload;
}
