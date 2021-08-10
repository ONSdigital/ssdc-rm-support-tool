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
import uk.gov.ons.ssdc.supporttool.model.dto.messaging.UpdateSampleSensitive;
import uk.gov.ons.ssdc.supporttool.model.dto.ui.Fulfilment;
import uk.gov.ons.ssdc.supporttool.model.dto.ui.InvalidAddress;
import uk.gov.ons.ssdc.supporttool.model.dto.ui.Refusal;
import uk.gov.ons.ssdc.supporttool.model.entity.Case;
import uk.gov.ons.ssdc.supporttool.model.entity.UserGroupAuthorisedActivityType;
import uk.gov.ons.ssdc.supporttool.security.UserIdentity;
import uk.gov.ons.ssdc.supporttool.service.CaseService;
import uk.gov.ons.ssdc.supporttool.validation.ColumnValidator;

@Controller
@RequestMapping(value = "/api/cases")
public class CaseEndpoint {

  private final CaseService caseService;
  private final UserIdentity userIdentity;

  @Autowired
  public CaseEndpoint(UserIdentity userIdentity, CaseService caseService) {
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

  @PostMapping(value = "/{caseId}/action/fulfilment")
  public ResponseEntity<?> handleFulfilment(
      @PathVariable("caseId") UUID caseId,
      @RequestBody Fulfilment fulfilment,
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {

    Case caze = caseService.getCaseByCaseId(caseId);

    // Check user is authorised to request a fulfilment on a case for this survey
    userIdentity.checkUserPermission(
        userEmail,
        caze.getCollectionExercise().getSurvey(),
        UserGroupAuthorisedActivityType.CREATE_CASE_FULFILMENT);

    caseService.buildAndSendFulfilmentCaseEvent(fulfilment, caze, userEmail);

    return new ResponseEntity<>(HttpStatus.OK);
  }

  @PostMapping(value = "/{caseId}/action/invalid-address")
  public ResponseEntity<?> handleInvalidAddress(
      @PathVariable("caseId") UUID caseId,
      @RequestBody InvalidAddress invalidAddress,
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {

    Case caze = caseService.getCaseByCaseId(caseId);

    // Check user is authorised to invalidate a case address for this survey
    userIdentity.checkUserPermission(
        userEmail,
        caze.getCollectionExercise().getSurvey(),
        UserGroupAuthorisedActivityType.CREATE_CASE_INVALID_ADDRESS);

    caseService.buildAndSendInvalidAddressCaseEvent(invalidAddress, caze, userEmail);

    return new ResponseEntity<>(HttpStatus.OK);
  }
}
