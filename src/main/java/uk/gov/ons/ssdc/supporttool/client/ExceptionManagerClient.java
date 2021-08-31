package uk.gov.ons.ssdc.supporttool.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class ExceptionManagerClient {

  @Value("${exceptionmanager.connection.scheme}")
  private String scheme;

  @Value("${exceptionmanager.connection.host}")
  private String host;

  @Value("${exceptionmanager.connection.port}")
  private String port;

  public String getBadMessagesSummary() {

    RestTemplate restTemplate = new RestTemplate();
    UriComponents uriComponents = createUriComponents("/badmessages/summary");

    String result = restTemplate.getForObject(uriComponents.toUri(), String.class);

    return result;
  }

  public String getBadMessageDetails(String messageHash) {

    RestTemplate restTemplate = new RestTemplate();
    UriComponents uriComponents = createUriComponents(String.format("/badmessage/%s", messageHash));

    String result = restTemplate.getForObject(uriComponents.toUri(), String.class);

    return result;
  }

  public String peekMessage(String messageHash) {

    RestTemplate restTemplate = new RestTemplate();
    UriComponents uriComponents =
        createUriComponents(String.format("/peekmessage/%s", messageHash));

    String result = restTemplate.getForObject(uriComponents.toUri(), String.class);

    return result;
  }

  public void skipMessage(String messageHash) {

    RestTemplate restTemplate = new RestTemplate();
    UriComponents uriComponents =
        createUriComponents(String.format("/skipmessage/%s", messageHash));

    restTemplate.getForObject(uriComponents.toUri(), String.class);
  }

  private UriComponents createUriComponents(String path) {
    return UriComponentsBuilder.newInstance()
        .scheme(scheme)
        .host(host)
        .port(port)
        .path(path)
        .build()
        .encode();
  }
}
