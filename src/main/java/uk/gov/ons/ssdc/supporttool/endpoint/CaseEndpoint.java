package uk.gov.ons.ssdc.supporttool.endpoint;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.ons.ssdc.supporttool.client.NotifyServiceClient;
import uk.gov.ons.ssdc.supporttool.model.dto.messaging.UpdateSampleSensitive;
import uk.gov.ons.ssdc.supporttool.model.dto.rest.RequestDTO;
import uk.gov.ons.ssdc.supporttool.model.dto.rest.RequestHeaderDTO;
import uk.gov.ons.ssdc.supporttool.model.dto.rest.RequestPayloadDTO;
import uk.gov.ons.ssdc.supporttool.model.dto.rest.SmsFulfilment;
import uk.gov.ons.ssdc.supporttool.model.dto.ui.InvalidCase;
import uk.gov.ons.ssdc.supporttool.model.dto.ui.PrintFulfilment;
import uk.gov.ons.ssdc.supporttool.model.dto.ui.Refusal;
import uk.gov.ons.ssdc.supporttool.model.dto.ui.SmsFulfilmentAction;
import uk.gov.ons.ssdc.supporttool.model.entity.Case;
import uk.gov.ons.ssdc.supporttool.model.entity.UserGroupAuthorisedActivityType;
import uk.gov.ons.ssdc.supporttool.security.UserIdentity;
import uk.gov.ons.ssdc.supporttool.service.CaseService;
import uk.gov.ons.ssdc.supporttool.validation.ColumnValidator;

@Controller
@RequestMapping(value = "/api/cases")
public class CaseEndpoint {

  private final NotifyServiceClient notifyServiceClient;
  private final CaseService caseService;
  private final UserIdentity userIdentity;

  @Autowired
  public CaseEndpoint(
      NotifyServiceClient notifyServiceClient, UserIdentity userIdentity, CaseService caseService) {
    this.notifyServiceClient = notifyServiceClient;
    this.userIdentity = userIdentity;
    this.caseService = caseService;
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
      String validatationErrorStr = String.join(", ", validationErrors);
      Map<String, String> body = Map.of("errors", validatationErrorStr);

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
  public void handleSmsFulfilment(
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

    smsFulfilmentRequest.setHeader(header);
    payload.setSmsFulfilment(smsFulfilment);
    smsFulfilmentRequest.setPayload(payload);

    requestSmsFulfiment(smsFulfilmentRequest);
  }

  private void requestSmsFulfiment(RequestDTO smsFulfilmentRequest) {
    try {
      notifyServiceClient.requestSmsFulfilment(smsFulfilmentRequest);
    } catch (HttpClientErrorException e) {
      throw new ResponseStatusException(e.getStatusCode(), e.getResponseBodyAsString(), e);
    }
  }
}
