package uk.gov.ons.ssdc.supporttool.endpoint;

import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import java.util.HashMap;
import java.util.Map;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.ons.ssdc.supporttool.model.messaging.dto.CollectionCase;
import uk.gov.ons.ssdc.supporttool.model.messaging.dto.RefusalDTO;
import uk.gov.ons.ssdc.supporttool.model.messaging.dto.RefusalTypeDTO;
import uk.gov.ons.ssdc.supporttool.model.entity.Case;
import uk.gov.ons.ssdc.supporttool.model.repository.CaseRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.EventRepository;
import uk.gov.ons.ssdc.supporttool.testutils.RabbitQueueHelper;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class CaseEndpointIT {

  private static final String TEST_UPRN_EXISTS = "123456789012345";
  private static final String UPRN_FIELD_NAME = "uprn";

  private static final String TEST_CASE_ID_1_EXISTS = "c0d4f87d-9d19-4393-80c9-9eb94f69c460";
  private static final String TEST_CASE_ID_2_EXISTS = "3e948f6a-00bb-466d-88a7-b0990a827b53";

  private static final String TEST_CASE_ID_DOES_NOT_EXIST = "590179eb-f8ce-4e2d-8cb6-ca4013a2ccf1";
  private static final String TEST_INVALID_CASE_ID = "anything";

  private static final String TEST_REFERENCE_DOES_NOT_EXIST = "9999999999";
  private static final String ADDRESS_TYPE_TEST = "addressTypeTest";
  private static final String TEST_TOWN = "Tenby";
  private static final String TEST_HOUSEHOLD_ENGLAND_TREATMENT_CODE = "HH_XXXXXE";

  private static final String TEST_POSTCODE_NO_SPACE = "AB12BC";
  private static final String TEST_POSTCODE_WITH_SPACE = "AB1 2BC";

  @LocalServerPort private int port;

  @Autowired private CaseRepository caseRepository;
  @Autowired private EventRepository eventRepository;
  @Autowired private RabbitQueueHelper rabbitQueueHelper;

  private EasyRandom easyRandom;

  private static final String refusalQueueName = "events.caseProcessor.refusal";
  private static final String invalidAddressQueueName = "events.caseProcessor.invalidAddress";
  private static final String printFulfilmentQueueName = "events.caseProcessor.fulfilment";

  @Before
  @Transactional
  public void setUp() {
    try {
      clearDown();
    } catch (Exception e) {
      // this is expected behaviour, where the event rows are deleted, then the case-processor image
      // puts a new
      // event row on and the case table clear down fails.  2nd run should clear it down
      clearDown();
    }

    rabbitQueueHelper.purgeQueue(refusalQueueName);
    rabbitQueueHelper.purgeQueue(invalidAddressQueueName);
    rabbitQueueHelper.purgeQueue(printFulfilmentQueueName);

    easyRandom = new EasyRandom(new EasyRandomParameters().randomizationDepth(1));
  }

  public void clearDown() {
    eventRepository.deleteAll();
    caseRepository.deleteAll();
  }

  @Test
  public void testCaseRefusal() throws JsonProcessingException, UnirestException {
    // Given
    Case caze = getACase();
    caze.setEvents(null);
    caze.setRefusalReceived(null);

    caseRepository.saveAndFlush(caze);

    // When
    //    HttpResponse<JsonNode> jsonResponse =
    //        Unirest.put(String.format("http://localhost:%d/qids/link", port))
    //            .header("content-type", "application/json")
    //            .body(DataUtils.mapper.writeValueAsString(newQidLink))
    //            .asJson();
    //
    //    // Then
    //    assertThat(jsonResponse.getStatus()).isEqualTo(HttpStatus.SC_OK);

    RefusalDTO refusalDTO = new RefusalDTO();
    refusalDTO.setType(RefusalTypeDTO.HARD_REFUSAL);
    refusalDTO.setAgentId("123");
    refusalDTO.setCallId("456");

    CollectionCase collectionCase = new CollectionCase();
    collectionCase.setCaseId(caze.getId());
    collectionCase.setRefusalReceived(null);

    refusalDTO.setCollectionCase(collectionCase);

    ObjectWriter ow = new ObjectMapper().writer();
    String jsonBody = ow.writeValueAsString(refusalDTO);

//    RestTemplate restTemplate = new RestTemplate();
//    String url = "http://localhost:" + port + "/cases/refusal/" + caze.getId();

    HttpResponse<JsonNode> jsonResponse =
        Unirest.put(String.format("http://localhost:%d/cases/refusal/%s", port, caze.getId()))
            .header("content-type", "application/json")
            .body(jsonBody)
            .asJson();

    System.out.println();

//        HttpHeaders headers = new HttpHeaders();
//    headers.setContentType(MediaType.APPLICATION_JSON);
//
//    HttpEntity<RefusalDTO> requestUpdate = new HttpEntity<>(refusalDTO, headers);
//    template.exchange(resourceUrl, HttpMethod.PUT, requestUpdate, Void.class);


//        HttpEntity<RefusalDTO> requestUpdate = new HttpEntity<>(refusalDTO, headers);
//    ResponseEntity<String> response = restTemplate.postForEntity(
//        fooResourceUrl+"/form", request , String.class);
//    template.exchange(resourceUrl, HttpMethod.PUT, requestUpdate, Void.class);

    //    RestTemplate restTemplate = new RestTemplate();
    //    String url =
    //        "http://localhost:" + port + "/cases/refusal/" + caze.getId();
    //    ResponseEntity<Case[]> foundCasesResponse = restTemplate.p(url, Case[].class);
    //
    //    Case[] actualCases = foundCasesResponse.getBody();
    //    assertThat(actualCases.length).isEqualTo(2);
  }

  //  @Test
  //  public void shouldRetrieveMultipleCasesWithEventsWhenSearchingByUPRN() {
  //    createTwoTestCasesWithEvents();
  //
  //    RestTemplate restTemplate = new RestTemplate();
  //    String url =
  //        "http://localhost:"
  //            + port
  //            + "/cases/searchByField?caseEvents=true&fieldName=uprn&filterValue="
  //            + TEST_UPRN_EXISTS;
  //    ResponseEntity<CaseContainerDTO[]> foundCasesResponse =
  //        restTemplate.getForEntity(url, CaseContainerDTO[].class);
  //
  //    CaseContainerDTO[] actualCases = foundCasesResponse.getBody();
  //    assertThat(actualCases.length).isEqualTo(2);
  //
  //    assertThat(actualCases[0].getSample().get(UPRN_FIELD_NAME).equals(TEST_UPRN_EXISTS));
  //    assertThat(actualCases[0].getCaseEvents().size()).isEqualTo(1);
  //
  //    assertThat(actualCases[1].getSample().get(UPRN_FIELD_NAME).equals(TEST_UPRN_EXISTS));
  //    assertThat(actualCases[1].getCaseEvents().size()).isEqualTo(1);
  //  }
  //
  //  @Test
  //  public void searchCasesByPostCode() {
  //    createTwoTestCasesWithEvents();
  //
  //    RestTemplate restTemplate = new RestTemplate();
  //    String url =
  //        "http://localhost:"
  //            + port
  //            + "/cases/searchByField?ignoreCaseAndSpaces=true&fieldName=PostCode&filterValue="
  //            + TEST_POSTCODE_WITH_SPACE;
  //    ResponseEntity<CaseContainerDTO[]> foundCasesResponse =
  //        restTemplate.getForEntity(url, CaseContainerDTO[].class);
  //
  //    CaseContainerDTO[] actualCases = foundCasesResponse.getBody();
  //    assertThat(actualCases.length).isEqualTo(2);
  //
  //    assertThat(actualCases[0].getSample().get("PostCode").equals(TEST_POSTCODE_WITH_SPACE));
  //    assertThat(actualCases[1].getSample().get("PostCode").equals(TEST_POSTCODE_WITH_SPACE));
  //  }
  //
  //  @Test
  //  public void getCaseByIdMinusEvents() {
  //    createOneTestCaseWithEvent();
  //
  //    RestTemplate restTemplate = new RestTemplate();
  //    String url = "http://localhost:" + port + "/cases/" + TEST_CASE_ID_1_EXISTS;
  //    ResponseEntity<Case> foundCaseResponse = restTemplate.getForEntity(url, Case.class);
  //
  //    Case actualCase = foundCaseResponse.getBody();
  //    assertThat(actualCase.getId()).isEqualTo(UUID.fromString(TEST_CASE_ID_1_EXISTS));
  //  }
  //
  //  @Test
  //  public void shouldRetrieveACaseWithEventsWhenSearchingByCaseId() throws Exception {
  //    createOneTestCaseWithEvent();
  //
  //    HttpResponse<JsonNode> response =
  //        Unirest.get(createUrl("http://localhost:%d/cases/%s", port, TEST_CASE_ID_1_EXISTS))
  //            .header("accept", "application/json")
  //            .queryString("caseEvents", "true")
  //            .asJson();
  //
  //    assertThat(response.getStatus()).isEqualTo(OK.value());
  //
  //    CaseContainerDTO actualData = extractCaseContainerDTOFromResponse(response);
  //
  //    assertThat(actualData.getId()).isEqualTo(UUID.fromString(TEST_CASE_ID_1_EXISTS));
  //    assertThat(actualData.getCaseEvents().size()).isEqualTo(1);
  //  }
  //
  //  @Test
  //  public void shouldRetrieveACaseWithoutEventsWhenSearchingByCaseId() throws Exception {
  //    createOneTestCaseWithoutEvents();
  //
  //    HttpResponse<JsonNode> response =
  //        Unirest.get(createUrl("http://localhost:%d/cases/%s", port, TEST_CASE_ID_1_EXISTS))
  //            .header("accept", "application/json")
  //            .queryString("caseEvents", "false")
  //            .asJson();
  //
  //    assertThat(response.getStatus()).isEqualTo(OK.value());
  //
  //    CaseContainerDTO actualData = extractCaseContainerDTOFromResponse(response);
  //
  //    assertThat(actualData.getId()).isEqualTo(UUID.fromString(TEST_CASE_ID_1_EXISTS));
  //    assertThat(actualData.getCaseEvents().size()).isEqualTo(0);
  //  }
  //
  //  @Test
  //  public void shouldRetrieveACaseWithoutEventsByDefaultWhenSearchingByCaseId() throws Exception
  // {
  //    createOneTestCaseWithoutEvents();
  //
  //    HttpResponse<JsonNode> response =
  //        Unirest.get(createUrl("http://localhost:%d/cases/%s", port, TEST_CASE_ID_1_EXISTS))
  //            .header("accept", "application/json")
  //            .asJson();
  //
  //    assertThat(response.getStatus()).isEqualTo(OK.value());
  //
  //    CaseContainerDTO actualData = extractCaseContainerDTOFromResponse(response);
  //
  //    assertThat(actualData.getId()).isEqualTo(UUID.fromString(TEST_CASE_ID_1_EXISTS));
  //    assertThat(actualData.getCaseEvents().size()).isEqualTo(0);
  //  }
  //
  //  @Test
  //  public void shouldReturn404WhenCaseIdNotFound() throws UnirestException {
  //    HttpResponse<JsonNode> jsonResponse =
  //        Unirest.get(createUrl("http://localhost:%d/cases/%s", port,
  // TEST_CASE_ID_DOES_NOT_EXIST))
  //            .header("accept", "application/json")
  //            .asJson();
  //
  //    assertThat(jsonResponse.getStatus()).isEqualTo(NOT_FOUND.value());
  //  }
  //
  //  @Test
  //  public void shouldReturn400WhenInvalidCaseId() throws UnirestException {
  //    HttpResponse<JsonNode> jsonResponse =
  //        Unirest.get(createUrl("http://localhost:%d/cases/%s", port, TEST_INVALID_CASE_ID))
  //            .header("accept", "application/json")
  //            .asJson();
  //
  //    assertThat(jsonResponse.getStatus()).isEqualTo(BAD_REQUEST.value());
  //  }
  //
  //  @Test
  //  public void shouldRetrieveACaseWithEventsWhenSearchingByCaseReference() throws Exception {
  //    Case expectedCase = createOneTestCaseWithEvent();
  //    String expectedCaseRef = Long.toString(expectedCase.getCaseRef());
  //
  //    HttpResponse<JsonNode> response =
  //        Unirest.get(createUrl("http://localhost:%d/cases/ref/%s", port, expectedCaseRef))
  //            .header("accept", "application/json")
  //            .queryString("caseEvents", "true")
  //            .asJson();
  //
  //    assertThat(response.getStatus()).isEqualTo(OK.value());
  //
  //    CaseContainerDTO actualData = extractCaseContainerDTOFromResponse(response);
  //
  //    assertThat(actualData.getCaseRef()).isEqualTo(expectedCaseRef);
  //    assertThat(actualData.getCaseEvents().size()).isEqualTo(1);
  //  }
  //
  //  @Test
  //  public void shouldRetrieveACaseWithoutEventsWhenSearchingByCaseReference() throws Exception {
  //    Case expectedCase = createOneTestCaseWithoutEvents();
  //    String expectedCaseRef = Long.toString(expectedCase.getCaseRef());
  //
  //    HttpResponse<JsonNode> response =
  //        Unirest.get(createUrl("http://localhost:%d/cases/ref/%s", port, expectedCaseRef))
  //            .header("accept", "application/json")
  //            .queryString("caseEvents", "false")
  //            .asJson();
  //
  //    assertThat(response.getStatus()).isEqualTo(OK.value());
  //
  //    CaseContainerDTO actualData = extractCaseContainerDTOFromResponse(response);
  //
  //    assertThat(actualData.getCaseRef()).isEqualTo(expectedCaseRef);
  //    assertThat(actualData.getCaseEvents().size()).isEqualTo(0);
  //  }
  //
  //  @Test
  //  public void shouldRetrieveACaseWithoutEventsByDefaultWhenSearchingByCaseReference()
  //      throws Exception {
  //    Case expectedCase = createOneTestCaseWithoutEvents();
  //    String expectedCaseRef = Long.toString(expectedCase.getCaseRef());
  //
  //    HttpResponse<JsonNode> response =
  //        Unirest.get(createUrl("http://localhost:%d/cases/ref/%s", port, expectedCaseRef))
  //            .header("accept", "application/json")
  //            .asJson();
  //
  //    assertThat(response.getStatus()).isEqualTo(OK.value());
  //
  //    CaseContainerDTO actualData = extractCaseContainerDTOFromResponse(response);
  //
  //    assertThat(actualData.getCaseRef()).isEqualTo(expectedCaseRef);
  //    assertThat(actualData.getCaseEvents().size()).isEqualTo(0);
  //  }
  //
  //  @Test
  //  public void shouldReturn404WhenCaseReferenceNotFound() throws Exception {
  //    HttpResponse<JsonNode> jsonResponse =
  //        Unirest.get(
  //                createUrl("http://localhost:%d/cases/ref/%s", port,
  // TEST_REFERENCE_DOES_NOT_EXIST))
  //            .header("accept", "application/json")
  //            .asJson();
  //
  //    assertThat(jsonResponse.getStatus()).isEqualTo(NOT_FOUND.value());
  //  }
  //
  //  @Test
  //  public void testGetNewUacForTelephoneCaptureDoesNotReturnTheSameQidUacTwice()
  //      throws UnirestException, IOException {
  //    // Given
  //    setupUnitTestCaseWithTreatmentCode(
  //        TEST_CASE_ID_1_EXISTS, TEST_HOUSEHOLD_ENGLAND_TREATMENT_CODE);
  //
  //    // When
  //    HttpResponse<JsonNode> firstJsonResponse =
  //        Unirest.get(
  //                createUrl(
  //                    "http://localhost:%d/cases/%s/telephone-capture", port,
  // TEST_CASE_ID_1_EXISTS))
  //            .header("accept", "application/json")
  //            .asJson();
  //    TelephoneCaptureDTO firstTelephoneCaptureDTO =
  //        DataUtils.mapper.readValue(
  //            firstJsonResponse.getBody().getObject().toString(), TelephoneCaptureDTO.class);
  //
  //    HttpResponse<JsonNode> secondJsonResponse =
  //        Unirest.get(
  //                createUrl(
  //                    "http://localhost:%d/cases/%s/telephone-capture", port,
  // TEST_CASE_ID_1_EXISTS))
  //            .header("accept", "application/json")
  //            .asJson();
  //    TelephoneCaptureDTO secondTelephoneCaptureDTO =
  //        DataUtils.mapper.readValue(
  //            secondJsonResponse.getBody().getObject().toString(), TelephoneCaptureDTO.class);
  //
  //    // Then
  //
  // assertThat(firstTelephoneCaptureDTO.getQid()).isNotEqualTo(secondTelephoneCaptureDTO.getQid());
  //
  // assertThat(firstTelephoneCaptureDTO.getUac()).isNotEqualTo(secondTelephoneCaptureDTO.getUac());
  //  }
  //
  //  @Test
  //  public void testGetNewUacForTelephoneCapture() throws Exception {
  //    try (QueueSpy telephoneCaptureQueueSpy =
  // rabbitQueueHelper.listen(telephoneCaptureQueueName)) {
  //
  //      // Given
  //      Case caze =
  //          setupUnitTestCaseWithTreatmentCode(
  //              TEST_CASE_ID_1_EXISTS, TEST_HOUSEHOLD_ENGLAND_TREATMENT_CODE);
  //
  //      // When
  //      HttpResponse<JsonNode> jsonResponse =
  //          Unirest.get(
  //                  createUrl(
  //                      "http://localhost:%d/cases/%s/telephone-capture",
  //                      port, TEST_CASE_ID_1_EXISTS))
  //              .header("accept", "application/json")
  //              .asJson();
  //
  //      // Then
  //      TelephoneCaptureDTO actualTelephoneCaptureDTO =
  //          DataUtils.mapper.readValue(
  //              jsonResponse.getBody().getObject().toString(), TelephoneCaptureDTO.class);
  //      assertThat(actualTelephoneCaptureDTO.getQid()).startsWith("01");
  //      assertThat(actualTelephoneCaptureDTO.getUac()).isNotNull();
  //
  // assertThat(actualTelephoneCaptureDTO.getCaseId().toString()).isEqualTo(TEST_CASE_ID_1_EXISTS);
  //
  //      String message = telephoneCaptureQueueSpy.checkExpectedMessageReceived();
  //      ResponseManagementEvent responseManagementEvent =
  //          DataUtils.mapper.readValue(message, ResponseManagementEvent.class);
  //
  //      assertThat(responseManagementEvent.getPayload().getTelephoneCapture().getCaseId())
  //          .isEqualTo(caze.getId());
  //
  //      assertThat(
  //          responseManagementEvent.getPayload().getTelephoneCapture().getQid().startsWith("01"));
  //
  // assertThat(responseManagementEvent.getPayload().getTelephoneCapture().getUac()).isNotNull();
  //
  //
  // assertThat(responseManagementEvent.getEvent().getSource()).isEqualTo("RESPONSE_MANAGEMENT");
  //      assertThat(responseManagementEvent.getEvent().getChannel()).isEqualTo(ChannelTypeDTO.RM);
  //      assertThat(responseManagementEvent.getEvent().getType())
  //          .isEqualTo(EventTypeDTO.TELEPHONE_CAPTURE_REQUESTED);
  //      assertThat(responseManagementEvent.getEvent().getTransactionId()).isNotNull();
  //      assertThat(responseManagementEvent.getEvent().getDateTime()).isNotNull();
  //    }
  //  }
  //
  //  private Case createOneTestCaseWithEvent() {
  //    return setupTestCaseWithEvent(TEST_CASE_ID_1_EXISTS);
  //  }
  //
  //  private Case createOneTestCaseWithoutEvents() {
  //    return setupTestCaseWithoutEvents(TEST_CASE_ID_1_EXISTS);
  //  }
  //
  //  private void createTwoTestCasesWithEvents() {
  //    setupTestCaseWithEvent(TEST_CASE_ID_1_EXISTS);
  //    setupTestCaseWithEvent(TEST_CASE_ID_2_EXISTS);
  //  }
  //
  //  private Case setupTestCaseWithEvent(String id) {
  //    Case caze = easyRandom.nextObject(Case.class);
  //    caze.setId(UUID.fromString(id));
  //    caze.setCollectionExercise(null);
  //
  //    Map<String, String> sample = new HashMap<>();
  //    sample.put(UPRN_FIELD_NAME, TEST_UPRN_EXISTS);
  //    sample.put("PostCode", TEST_POSTCODE_NO_SPACE);
  //    sample.put("Town", TEST_TOWN);
  //    caze.setReceiptReceived(false);
  //    caze.setSample(sample);
  //
  //    caseRepository.saveAndFlush(caze);
  //
  //    UacQidLink uacQidLink = new UacQidLink();
  //    uacQidLink.setId(UUID.randomUUID());
  //    uacQidLink.setActive(true);
  //    uacQidLink.setCaze(caze);
  //
  //    Event event = new Event();
  //    event.setId(UUID.randomUUID());
  //    event.setCaze(caze);
  //    event.setEventType(EventType.CASE_CREATED);
  //    event.setEventPayload("{}");
  //
  //    eventRepository.saveAndFlush(event);
  //
  //    return caseRepository
  //        .findById(UUID.fromString(id))
  //        .orElseThrow(() -> new RuntimeException("Case not found!"));
  //  }
  //
  //  private Case setupTestCaseWithoutEvents(String id) {
  //    Case caze = getACase(id);
  //
  //    return saveAndRetrieveCase(caze);
  //  }
  //
  //  private Case setupUnitTestCaseWithTreatmentCode(String id, String treatmentCode) {
  //    Case caze = getACase(id);
  //
  //    Map<String, String> sample = new HashMap<>();
  //    sample.put("CaseType", "HH");
  //    sample.put("Region", "E1000");
  //    sample.put("AddressLevel", "U");
  //    sample.put(treatmentCode, TEST_HOUSEHOLD_ENGLAND_TREATMENT_CODE);
  //
  //    sample.put("AddressType", ADDRESS_TYPE_TEST);
  //    caze.setReceiptReceived(false);
  //    caze.setSample(sample);
  //
  //    return saveAndRetrieveCase(caze);
  //  }
  //
  //  private Case saveAndRetrieveCase(Case caze) {
  //    caseRepository.saveAndFlush(caze);
  //
  //    return caseRepository
  //        .findById(caze.getId())
  //        .orElseThrow(() -> new RuntimeException("Case not found!"));
  //  }
  //
  //  private String createUrl(String urlFormat, int port, String param1) {
  //    return String.format(urlFormat, port, param1);
  //  }
  //
  private Case getACase() {
    Case caze = easyRandom.nextObject(Case.class);
    caze.setEvents(null);
    caze.setCollectionExercise(null);

    Map<String, String> sample = new HashMap<>();
    sample.put(UPRN_FIELD_NAME, TEST_UPRN_EXISTS);
    sample.put("AddressType", ADDRESS_TYPE_TEST);
    caze.setReceiptReceived(false);
    caze.setSample(sample);

    caze.setUacQidLinks(null);
    return caze;
  }
}
