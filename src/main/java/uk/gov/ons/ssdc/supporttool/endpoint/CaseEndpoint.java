package uk.gov.ons.ssdc.supporttool.endpoint;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.ons.ssdc.supporttool.model.dto.messaging.EventDTO;
import uk.gov.ons.ssdc.supporttool.model.dto.messaging.EventTypeDTO;
import uk.gov.ons.ssdc.supporttool.model.dto.messaging.ResponseManagementEvent;
import uk.gov.ons.ssdc.supporttool.model.dto.messaging.UpdateSampleSensitive;
import uk.gov.ons.ssdc.supporttool.model.dto.ui.Fulfilment;
import uk.gov.ons.ssdc.supporttool.model.dto.ui.InvalidAddress;
import uk.gov.ons.ssdc.supporttool.model.dto.ui.Refusal;
import uk.gov.ons.ssdc.supporttool.model.entity.Case;
import uk.gov.ons.ssdc.supporttool.model.entity.Event;
import uk.gov.ons.ssdc.supporttool.model.entity.UserGroupAuthorisedActivityType;
import uk.gov.ons.ssdc.supporttool.security.UserIdentity;
import uk.gov.ons.ssdc.supporttool.service.CaseService;

@Controller
@RequestMapping(value = "/cases")
public class CaseEndpoint {

  private final CaseService caseService;
  private final UserIdentity userIdentity;

  @Autowired
  public CaseEndpoint(UserIdentity userIdentity, CaseService caseService) {
    this.userIdentity = userIdentity;
    this.caseService = caseService;
  }

  @PostMapping("{caseId}/updateSensitiveField/")
  public ResponseEntity<?> updateSensitiveField(@PathVariable(value = "caseId") UUID caseId,
                                               @RequestParam(value = "fieldToUpdate") String fieldToUpdate,
                                               @RequestParam(value = "newValue") String newValue ) {

    UpdateSampleSensitive updateSampleSensitive = new UpdateSampleSensitive();
    updateSampleSensitive.setCaseId(caseId);
    updateSampleSensitive.setSampleSensitive(Collections.singletonMap(fieldToUpdate, newValue));

    caseService.buildAndSendUpdateSensitiveSampleEvent(updateSampleSensitive);

    return new ResponseEntity<>(HttpStatus.OK);
  }



  @PostMapping(value = "/{caseId}/action/refusal")
  public ResponseEntity<?> handleRefusal(
      @PathVariable("caseId") UUID caseId,
      @RequestBody Refusal refusal,
      @RequestHeader(required = false, value = "x-goog-iap-jwt-assertion") String jwtToken) {

    Case caze = caseService.getCaseByCaseId(caseId);

    // Check user is authorised to refuse a case for this survey
    userIdentity.checkUserPermission(
        jwtToken,
        caze.getCollectionExercise().getSurvey(),
        UserGroupAuthorisedActivityType.CREATE_CASE_REFUSAL);

    caseService.buildAndSendRefusalEvent(refusal, caze);

    return new ResponseEntity<>(HttpStatus.OK);
  }

  @PostMapping(value = "/{caseId}/action/fulfilment")
  public ResponseEntity<?> handleFulfilment(
      @PathVariable("caseId") UUID caseId,
      @RequestBody Fulfilment fulfilment,
      @RequestHeader(required = false, value = "x-goog-iap-jwt-assertion") String jwtToken) {

    Case caze = caseService.getCaseByCaseId(caseId);

    // Check user is authorised to request a fulfilment on a case for this survey
    userIdentity.checkUserPermission(
        jwtToken,
        caze.getCollectionExercise().getSurvey(),
        UserGroupAuthorisedActivityType.CREATE_CASE_FULFILMENT);

    caseService.buildAndSendFulfilmentCaseEvent(fulfilment, caze);

    return new ResponseEntity<>(HttpStatus.OK);
  }

  @PostMapping(value = "/{caseId}/action/invalid-address")
  public ResponseEntity<?> handleInvalidAddress(
      @PathVariable("caseId") UUID caseId,
      @RequestBody InvalidAddress invalidAddress,
      @RequestHeader(required = false, value = "x-goog-iap-jwt-assertion") String jwtToken) {

    Case caze = caseService.getCaseByCaseId(caseId);

    // Check user is authorised to invalidate a case address for this survey
    userIdentity.checkUserPermission(
        jwtToken,
        caze.getCollectionExercise().getSurvey(),
        UserGroupAuthorisedActivityType.CREATE_CASE_INVALID_ADDRESS);

    caseService.buildAndSendInvalidAddressCaseEvent(invalidAddress, caze);

    return new ResponseEntity<>(HttpStatus.OK);
  }
}
