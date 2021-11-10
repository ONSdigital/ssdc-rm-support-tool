package uk.gov.ons.ssdc.supporttool.client;

import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.ons.ssdc.supporttool.model.dto.rest.RequestDTO;

@Component
public class NotifyServiceClient {

  @Value("${notifyservice.connection.scheme}")
  private String scheme;

  @Value("${notifyservice.connection.host}")
  private String host;

  @Value("${notifyservice.connection.port}")
  private String port;

  public void requestSmsFulfilment(RequestDTO smsFulfilmentRequest) {
    RestTemplate restTemplate = new RestTemplate();
    restTemplate.postForObject(
        createUri("/sms-fulfilment"), smsFulfilmentRequest, RequestDTO.class);
  }

  public void requestEmailFulfilment(RequestDTO emailFulfilmentRequest) {
    RestTemplate restTemplate = new RestTemplate();
    restTemplate.postForObject(
        createUri("/email-fulfilment"), emailFulfilmentRequest, RequestDTO.class);
  }

  private URI createUri(String path) {
    return UriComponentsBuilder.newInstance()
        .scheme(scheme)
        .host(host)
        .port(port)
        .path(path)
        .build()
        .encode()
        .toUri();
  }
}
