package uk.gov.ons.ssdc.supporttool.endpoint;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.ons.ssdc.supporttool.model.dto.FulfilmentDTO;
import uk.gov.ons.ssdc.supporttool.model.entity.Case;
import uk.gov.ons.ssdc.supporttool.model.entity.UserGroupAuthorisedActivityType;
import uk.gov.ons.ssdc.supporttool.security.UserIdentity;
import uk.gov.ons.ssdc.supporttool.service.CaseService;

@Controller
public class Fulfilment {

  private final CaseService caseService;
  private final UserIdentity userIdentity;

  @Autowired
  public Fulfilment(UserIdentity userIdentity, CaseService caseService) {
    this.userIdentity = userIdentity;
    this.caseService = caseService;
  }

  @PutMapping(value = "/fulfilment")
  public ResponseEntity<?> handleFulfilment(
      @RequestBody FulfilmentDTO fulfilmentDTO,
      @RequestHeader(required = false, value = "x-goog-iap-jwt-assertion") String jwtToken) {

    Case caze = caseService.getCaseByCaseId(fulfilmentDTO.getCaseId());

    // Check user is authorised to request a fulfilment on a case for this survey
    userIdentity.checkUserPermission(
        jwtToken, caze.getCollectionExercise().getSurvey(),
        UserGroupAuthorisedActivityType.CREATE_CASE_FULFILMENT);

    caseService.buildAndSendFulfilmentCaseEvent(fulfilmentDTO);

    return new ResponseEntity<>(HttpStatus.OK);
  }
}
