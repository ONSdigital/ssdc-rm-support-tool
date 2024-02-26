package uk.gov.ons.ssdc.supporttool.endpoint;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import uk.gov.ons.ssdc.common.model.entity.UserGroupAuthorisedActivityType;
import uk.gov.ons.ssdc.supporttool.testhelper.IntegrationTestHelper;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ExceptionManagerEndpointIT {

  private static final String BAD_MESSAGE_DETAILS_ENDPOINT = "api/exceptionManager/badMessage";
  private static final String BAD_MESSAGES_SUMMARY_ENDPOINT =
      "api/exceptionManager/badMessagesSummary";
  private static final String PEEK_MESSAGE_ENDPOINT = "api/exceptionManager/peekMessage";
  private static final String QUARANTINE_MESSAGE_ENDPOINT = "api/exceptionManager/skipMessage";
  private static final String BAD_MESSAGE_DETAILS_API_ENDPOINT = "/badmessage";
  private static final String BAD_MESSAGES_SUMMARY_API_ENDPOINT = "/badmessages/summary";
  private static final String BAD_MESSAGES_COUNT_API_ENDPOINT = "/badmessages/count";
  private static final String PEEK_MESSAGE_ENDPOINT_API_ENDPOINT = "/peekmessage";
  private static final String QUARANTINE_MESSAGE_ENDPOINT_API_ENDPOINT = "/skipmessage";

  private static final String TEST_MESSAGE_HASH =
      "9af5350f1e61149cd0bb7dfa5efae46f224aaaffed729b220d63e0fe5a8bf4b9";

  @Autowired private IntegrationTestHelper integrationTestHelper;

  @LocalServerPort private int port;

  private static final ObjectMapper objectMapper = new ObjectMapper();

  private WireMockServer wireMockServer;

  @BeforeEach
  @Transactional
  public void setUp() {
    this.wireMockServer = new WireMockServer(8667);
    wireMockServer.start();
    configureFor(wireMockServer.port());
  }

  @AfterEach
  public void tearDown() {
    wireMockServer.stop();
  }

  @Test
  void testGetBadMessagesSummary() throws JsonProcessingException {
    // Given
    integrationTestHelper.setUpTestUserPermission(
        UserGroupAuthorisedActivityType.EXCEPTION_MANAGER_VIEWER);

    // Stub the Exception Manager API endpoint
    String exceptionManagerApiResponse = "[{\"test\":\"test\"}]";
    wireMockServer.stubFor(
        WireMock.get(WireMock.urlEqualTo(BAD_MESSAGES_SUMMARY_API_ENDPOINT))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withBody(exceptionManagerApiResponse)
                    .withHeader("Content-Type", "application/json")));

    String exceptionManagerApiCountResponse = "10";
    wireMockServer.stubFor(
        WireMock.get(WireMock.urlEqualTo(BAD_MESSAGES_COUNT_API_ENDPOINT))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withBody(exceptionManagerApiCountResponse)
                    .withHeader("Content-Type", "application/json")));

    RestTemplate restTemplate = new RestTemplate();
    String url = String.format("http://localhost:%s/%s", port, BAD_MESSAGES_SUMMARY_ENDPOINT);

    // When
    ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    JsonNode responseJson = objectMapper.readTree(response.getBody());
    assertThat(responseJson.get(0).get("test").textValue()).isNotEmpty();

    verify(getRequestedFor(urlEqualTo(BAD_MESSAGES_SUMMARY_API_ENDPOINT)));
  }

  @Test
  void testGetBadMessageDetails() throws JsonProcessingException {
    // Given
    integrationTestHelper.setUpTestUserPermission(
        UserGroupAuthorisedActivityType.EXCEPTION_MANAGER_VIEWER);

    // Stub the Exception Manager API endpoint
    String exceptionManagerApiResponse = "[{\"test\":\"test\"}]";
    String exceptionManagerApiMessageDetailUrl =
        String.format("%s/%s", BAD_MESSAGE_DETAILS_API_ENDPOINT, TEST_MESSAGE_HASH);
    wireMockServer.stubFor(
        WireMock.get(WireMock.urlEqualTo(exceptionManagerApiMessageDetailUrl))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withBody(exceptionManagerApiResponse)
                    .withHeader("Content-Type", "application/json")));

    RestTemplate restTemplate = new RestTemplate();
    String url =
        String.format(
            "http://localhost:%s/%s/%s", port, BAD_MESSAGE_DETAILS_ENDPOINT, TEST_MESSAGE_HASH);

    // When
    ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    JsonNode responseJson = objectMapper.readTree(response.getBody());
    assertThat(responseJson.get(0).get("test").textValue()).isNotEmpty();

    verify(getRequestedFor(urlEqualTo(exceptionManagerApiMessageDetailUrl)));
  }

  @Test
  void testPeekMessage() {
    // Given
    integrationTestHelper.setUpTestUserPermission(
        UserGroupAuthorisedActivityType.EXCEPTION_MANAGER_PEEK);

    // Stub the Exception Manager API endpoint
    String exceptionManagerApiResponse = "test";
    String exceptionManagerApiMessagePeekUrl =
        String.format("%s/%s", PEEK_MESSAGE_ENDPOINT_API_ENDPOINT, TEST_MESSAGE_HASH);
    wireMockServer.stubFor(
        WireMock.get(WireMock.urlEqualTo(exceptionManagerApiMessagePeekUrl))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withBody(exceptionManagerApiResponse)
                    .withHeader("Content-Type", "application/json")));

    RestTemplate restTemplate = new RestTemplate();
    String url =
        String.format("http://localhost:%s/%s/%s", port, PEEK_MESSAGE_ENDPOINT, TEST_MESSAGE_HASH);

    // When
    ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    String responseString = response.getBody();
    assertThat(responseString).isNotEmpty();

    verify(getRequestedFor(urlEqualTo(exceptionManagerApiMessagePeekUrl)));
  }

  @Test
  void testQuarantineMessage() {
    // Given
    integrationTestHelper.setUpTestUserPermission(
        UserGroupAuthorisedActivityType.EXCEPTION_MANAGER_QUARANTINE);

    // Stub the Exception Manager API endpoint
    wireMockServer.stubFor(
        WireMock.post(WireMock.urlEqualTo(QUARANTINE_MESSAGE_ENDPOINT_API_ENDPOINT))
            .willReturn(WireMock.aResponse().withStatus(200)));

    RestTemplate restTemplate = new RestTemplate();
    String url =
        String.format(
            "http://localhost:%s/%s/%s", port, QUARANTINE_MESSAGE_ENDPOINT, TEST_MESSAGE_HASH);

    // When
    ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    verify(postRequestedFor(urlEqualTo(QUARANTINE_MESSAGE_ENDPOINT_API_ENDPOINT)));
  }
}
