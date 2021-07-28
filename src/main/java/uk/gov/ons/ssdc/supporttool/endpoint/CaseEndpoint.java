package uk.gov.ons.ssdc.supporttool.endpoint;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.ons.ssdc.supporttool.model.dto.messaging.EventDTO;
import uk.gov.ons.ssdc.supporttool.model.dto.messaging.EventTypeDTO;
import uk.gov.ons.ssdc.supporttool.model.dto.messaging.ResponseManagementEvent;
import uk.gov.ons.ssdc.supporttool.model.dto.messaging.UpdateSampleSensitive;
import uk.gov.ons.ssdc.supporttool.model.dto.ui.Fulfilment;
import uk.gov.ons.ssdc.supporttool.model.dto.ui.InvalidAddress;
import uk.gov.ons.ssdc.supporttool.model.dto.ui.Refusal;
import uk.gov.ons.ssdc.supporttool.model.entity.Case;
import uk.gov.ons.ssdc.supporttool.model.entity.Event;
import uk.gov.ons.ssdc.supporttool.model.entity.Job;
import uk.gov.ons.ssdc.supporttool.model.entity.JobRow;
import uk.gov.ons.ssdc.supporttool.model.entity.JobRowStatus;
import uk.gov.ons.ssdc.supporttool.model.entity.UserGroupAuthorisedActivityType;
import uk.gov.ons.ssdc.supporttool.security.UserIdentity;
import uk.gov.ons.ssdc.supporttool.service.CaseService;
import uk.gov.ons.ssdc.supporttool.validation.ColumnValidator;
import uk.gov.ons.ssdc.supporttool.validation.Rule;

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

  @PostMapping("{caseId}/updateSensitiveField/")
  public ResponseEntity<?> updateSensitiveField(
      @PathVariable(value = "caseId") UUID caseId,
      @RequestBody UpdateSampleSensitive updateSampleSensitive,
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {


    Case caze = caseService.getCaseByCaseId(caseId);

    userIdentity.checkUserPermission(
        userEmail,
        caze.getCollectionExercise().getSurvey(),
        UserGroupAuthorisedActivityType.UPDATE_SENSITIVE_SAMPLE);

    Optional<List<String>> validationErrorsOpt = validateFieldToUpdate(caze, updateSampleSensitive.getSampleSensitive());


    if(validationErrorsOpt.isPresent()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.join("|", validationErrorsOpt.get()));
    }

    caseService.buildAndSendUpdateSensitiveSampleEvent(updateSampleSensitive);

    return new ResponseEntity<>(HttpStatus.OK);
  }

  private Optional<List<String>> validateFieldToUpdate(Case caze, Map<String, String> fieldAndValueToValidate) {
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

    if(allValidationErrors.size() == 0 ) {
      return Optional.empty();
    }

    return Optional.of(allValidationErrors);
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

    caseService.buildAndSendRefusalEvent(refusal, caze);

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

    caseService.buildAndSendFulfilmentCaseEvent(fulfilment, caze);

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

    caseService.buildAndSendInvalidAddressCaseEvent(invalidAddress, caze);

    return new ResponseEntity<>(HttpStatus.OK);
  }
}
