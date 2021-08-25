package uk.gov.ons.ssdc.supporttool.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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

  private static final ObjectMapper objectMapper = new ObjectMapper();

  public void requestSmsFulfilment(RequestDTO smsFulfilmentRequest) {
    RestTemplate restTemplate = new RestTemplate();
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    String requestBody;
    try {
      requestBody = objectMapper.writeValueAsString(smsFulfilmentRequest);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Error mapping SMS fulfilment request object to JSON", e);
    }
    HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
    restTemplate.postForEntity(createUri() + "/sms-fulfilment", request, String.class);
  }

  private URI createUri() {
    return UriComponentsBuilder.newInstance()
        .scheme(scheme)
        .host(host)
        .port(port)
        .build()
        .encode()
        .toUri();
  }
}
