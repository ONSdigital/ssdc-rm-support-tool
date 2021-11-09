package uk.gov.ons.ssdc.supporttool.rasrm.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.ons.ssdc.supporttool.rasrm.model.dto.rest.RasRmSampleSummaryDTO;
import uk.gov.ons.ssdc.supporttool.rasrm.model.dto.rest.RasRmSampleSummaryResponseDTO;

@Component
public class RasRmSampleServiceClient {

  @Value("${ras-rm-sample-service.connection.scheme}")
  private String scheme;

  @Value("${ras-rm-sample-service.connection.host}")
  private String host;

  @Value("${ras-rm-sample-service.connection.port}")
  private String port;

  public RasRmSampleSummaryResponseDTO createSampleSummary(
      int totalSampleUnits, int expectedCollectionInstruments) {
    RasRmSampleSummaryDTO sampleSummaryDTO = new RasRmSampleSummaryDTO();
    sampleSummaryDTO.setTotalSampleUnits(totalSampleUnits);
    sampleSummaryDTO.setExpectedCollectionInstruments(expectedCollectionInstruments);

    RestTemplate restTemplate = new RestTemplate();
    UriComponents uriComponents = createUriComponents("samples/samplesummary");

    return restTemplate.postForObject(
        uriComponents.toUri(), sampleSummaryDTO, RasRmSampleSummaryResponseDTO.class);
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
