package uk.gov.ons.ssdc.supporttool.endpoint;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.ons.ssdc.common.model.entity.Case;
import uk.gov.ons.ssdc.common.model.entity.Event;
import uk.gov.ons.ssdc.common.model.entity.UacQidLink;
import uk.gov.ons.ssdc.common.model.entity.UserGroupAuthorisedActivityType;
import uk.gov.ons.ssdc.common.validation.ColumnValidator;
import uk.gov.ons.ssdc.supporttool.client.NotifyServiceClient;
import uk.gov.ons.ssdc.supporttool.model.dto.messaging.UpdateSampleSensitive;
import uk.gov.ons.ssdc.supporttool.model.dto.rest.RequestDTO;
import uk.gov.ons.ssdc.supporttool.model.dto.rest.RequestHeaderDTO;
import uk.gov.ons.ssdc.supporttool.model.dto.rest.RequestPayloadDTO;
import uk.gov.ons.ssdc.supporttool.model.dto.rest.SmsFulfilment;
import uk.gov.ons.ssdc.supporttool.model.dto.ui.CaseDto;
import uk.gov.ons.ssdc.supporttool.model.dto.ui.EventDto;
import uk.gov.ons.ssdc.supporttool.model.dto.ui.InvalidCase;
import uk.gov.ons.ssdc.supporttool.model.dto.ui.PrintFulfilment;
import uk.gov.ons.ssdc.supporttool.model.dto.ui.Refusal;
import uk.gov.ons.ssdc.supporttool.model.dto.ui.SmsFulfilmentAction;
import uk.gov.ons.ssdc.supporttool.model.dto.ui.UacQidLinkDto;
import uk.gov.ons.ssdc.supporttool.security.UserIdentity;
import uk.gov.ons.ssdc.supporttool.service.CaseService;

@RestController
@RequestMapping(value = "/api/cases")
public class CaseEndpoint {

  private final NotifyServiceClient notifyServiceClient;
  private final CaseService caseService;
  private final UserIdentity userIdentity;

  public CaseEndpoint(
      NotifyServiceClient notifyServiceClient, UserIdentity userIdentity, CaseService caseService) {
    this.notifyServiceClient = notifyServiceClient;
    this.userIdentity = userIdentity;
    this.caseService = caseService;
  }

  @GetMapping("/{caseId}")
  public CaseDto getCase(
      @PathVariable(value = "caseId") UUID caseId,
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {
    Case caze = caseService.getCaseByCaseId(caseId);

    userIdentity.checkUserPermission(
        userEmail,
        caze.getCollectionExercise().getSurvey(),
        UserGroupAuthorisedActivityType.VIEW_CASE_DETAILS);

    CaseDto caseDto = new CaseDto();
    caseDto.setCollectionExerciseName(caze.getCollectionExercise().getName());
    caseDto.setCaseRef(caze.getCaseRef());
    caseDto.setRefusalReceived(caze.getRefusalReceived());
    caseDto.setInvalid(caze.isInvalid());
    caseDto.setCreatedAt(caze.getCreatedAt());
    caseDto.setLastUpdatedAt(caze.getLastUpdatedAt());

    List<EventDto> events = new LinkedList<>();
    for (Event event : caze.getEvents()) {
      EventDto eventDto = mapEventDto(event);
      events.add(eventDto);
    }

    List<UacQidLinkDto> uacQidLinks = new LinkedList<>();
    for (UacQidLink uacQidLink : caze.getUacQidLinks()) {
      UacQidLinkDto uacQidLinkDto = new UacQidLinkDto();
      uacQidLinkDto.setQid(uacQidLink.getQid());
      uacQidLinkDto.setActive(uacQidLink.isActive());
      uacQidLinkDto.setMetadata(uacQidLink.getMetadata());
      uacQidLinkDto.setCreatedAt(uacQidLink.getCreatedAt());
      uacQidLinkDto.setLastUpdatedAt(uacQidLink.getLastUpdatedAt());
      uacQidLinkDto.setReceiptReceived(uacQidLink.isReceiptReceived());
      uacQidLinkDto.setEqLaunched(uacQidLink.isEqLaunched());
      uacQidLinks.add(uacQidLinkDto);

      for (Event event : uacQidLink.getEvents()) {
        EventDto eventDto = mapEventDto(event);
        events.add(eventDto);
      }
    }

    caseDto.setUacQidLinks(uacQidLinks);
    caseDto.setEvents(events);

    return caseDto;
  }

  private EventDto mapEventDto(Event event) {
    EventDto eventDto = new EventDto();
    eventDto.setDescription(event.getDescription());
    eventDto.setDateTime(event.getDateTime());
    eventDto.setType(event.getType());
    eventDto.setChannel(event.getChannel());
    eventDto.setSource(event.getSource());
    eventDto.setMessageId(event.getMessageId());
    eventDto.setPayload(event.getPayload());
    eventDto.setCorrelationId(event.getCorrelationId());
    return eventDto;
  }

  @PostMapping("{caseId}/action/updateSensitiveField")
  public ResponseEntity<?> updateSensitiveField(
      @PathVariable(value = "caseId") UUID caseId,
      @RequestBody UpdateSampleSensitive updateSampleSensitive,
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {

    Case caze = caseService.getCaseByCaseId(caseId);

    userIdentity.checkUserPermission(
        userEmail,
        caze.getCollectionExercise().getSurvey(),
        UserGroupAuthorisedActivityType.UPDATE_SAMPLE_SENSITIVE);

    List<String> validationErrors =
        validateFieldToUpdate(caze, updateSampleSensitive.getSampleSensitive());

    if (validationErrors.size() > 0) {
      String validationErrorStr = String.join(", ", validationErrors);
      Map<String, String> body = Map.of("errors", validationErrorStr);

      return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    caseService.buildAndSendUpdateSensitiveSampleEvent(updateSampleSensitive, userEmail);

    return new ResponseEntity<>(HttpStatus.OK);
  }

  private List<String> validateFieldToUpdate(
      Case caze, Map<String, String> fieldAndValueToValidate) {
    ColumnValidator[] columnValidators =
        caze.getCollectionExercise().getSurvey().getSampleValidationRules();
    List<String> allValidationErrors = new LinkedList<>();

    for (var dataToValidate : fieldAndValueToValidate.entrySet()) {

      for (ColumnValidator columnValidator : columnValidators) {
        if (columnValidator.getColumnName().equals(dataToValidate.getKey())) {
          Map<String, String> validateThis =
              Map.of(dataToValidate.getKey(), dataToValidate.getValue());

          Optional<String> validationErrors = columnValidator.validateRow(validateThis);
          validationErrors.ifPresent(validationError -> allValidationErrors.add(validationError));
        }
      }
    }

    return allValidationErrors;
  }

  @PostMapping(value = "/{caseId}/action/refusal")
  public ResponseEntity<?> handleRefusal(
      @PathVariable("caseId") UUID caseId,
      @RequestBody Refusal refusal,
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {

    Case caze = caseService.getCaseByCaseId(caseId);

    // Check user is authorised to refuse a case for this survey
    userIdentity.checkUserPermission(
        userEmail,
        caze.getCollectionExercise().getSurvey(),
        UserGroupAuthorisedActivityType.CREATE_CASE_REFUSAL);

    caseService.buildAndSendRefusalEvent(refusal, caze, userEmail);

    return new ResponseEntity<>(HttpStatus.OK);
  }

  @PostMapping(value = "/{caseId}/action/print-fulfilment")
  public ResponseEntity<?> handlePrintFulfilment(
      @PathVariable("caseId") UUID caseId,
      @RequestBody PrintFulfilment printFulfilment,
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {

    Case caze = caseService.getCaseByCaseId(caseId);

    // Check user is authorised to request a fulfilment on a case for this survey
    userIdentity.checkUserPermission(
        userEmail,
        caze.getCollectionExercise().getSurvey(),
        UserGroupAuthorisedActivityType.CREATE_CASE_PRINT_FULFILMENT);

    caseService.buildAndSendPrintFulfilmentCaseEvent(printFulfilment, caze, userEmail);

    return new ResponseEntity<>(HttpStatus.OK);
  }

  @PostMapping(value = "/{caseId}/action/invalid-case")
  public ResponseEntity<?> handleInvalidCase(
      @PathVariable("caseId") UUID caseId,
      @RequestBody InvalidCase invalidCase,
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {

    Case caze = caseService.getCaseByCaseId(caseId);

    // Check user is authorised to invalidate a case address for this survey
    userIdentity.checkUserPermission(
        userEmail,
        caze.getCollectionExercise().getSurvey(),
        UserGroupAuthorisedActivityType.CREATE_CASE_INVALID_CASE);

    caseService.buildAndSendInvalidAddressCaseEvent(invalidCase, caze, userEmail);

    return new ResponseEntity<>(HttpStatus.OK);
  }

  @PostMapping(value = "/{caseId}/action/sms-fulfilment")
  public ResponseEntity<?> handleSmsFulfilment(
      @PathVariable("caseId") UUID caseId,
      @RequestBody SmsFulfilmentAction smsFulfilmentAction,
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {

    Case caze = caseService.getCaseByCaseId(caseId);

    // Check user is authorised to request a fulfilment on a case for this survey
    userIdentity.checkUserPermission(
        userEmail,
        caze.getCollectionExercise().getSurvey(),
        UserGroupAuthorisedActivityType.CREATE_CASE_SMS_FULFILMENT);

    RequestDTO smsFulfilmentRequest = new RequestDTO();
    RequestHeaderDTO header = new RequestHeaderDTO();
    header.setSource("SUPPORT_TOOL");
    header.setChannel("RM");
    header.setCorrelationId(UUID.randomUUID());
    header.setOriginatingUser(userEmail);

    RequestPayloadDTO payload = new RequestPayloadDTO();
    SmsFulfilment smsFulfilment = new SmsFulfilment();
    smsFulfilment.setCaseId(caze.getId());
    smsFulfilment.setPackCode(smsFulfilmentAction.getPackCode());
    smsFulfilment.setPhoneNumber(smsFulfilmentAction.getPhoneNumber());
    smsFulfilment.setUacMetadata(smsFulfilmentAction.getUacMetadata());

    smsFulfilmentRequest.setHeader(header);
    payload.setSmsFulfilment(smsFulfilment);
    smsFulfilmentRequest.setPayload(payload);

    Optional<String> errorOpt = requestSmsFulfilment(smsFulfilmentRequest);
    if (errorOpt.isPresent()) {
      return new ResponseEntity<>(errorOpt.get(), HttpStatus.BAD_REQUEST);
    }
    return new ResponseEntity<>(HttpStatus.OK);
  }

  private Optional<String> requestSmsFulfilment(RequestDTO smsFulfilmentRequest) {
    try {
      notifyServiceClient.requestSmsFulfilment(smsFulfilmentRequest);
    } catch (HttpClientErrorException e) {
      return Optional.of(e.getResponseBodyAsString());
    }
    return Optional.empty();
  }
}
