package uk.gov.ons.ssdc.supporttool.endpoint;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.ons.ssdc.supporttool.model.dto.InvalidAddressDTO;
import uk.gov.ons.ssdc.supporttool.model.entity.Case;
import uk.gov.ons.ssdc.supporttool.model.entity.UserGroupAuthorisedActivityType;
import uk.gov.ons.ssdc.supporttool.security.UserIdentity;
import uk.gov.ons.ssdc.supporttool.service.CaseService;

@Controller
public class InvalidAddress {

  private final CaseService caseService;
  private final UserIdentity userIdentity;

  @Autowired
  public InvalidAddress(UserIdentity userIdentity, CaseService caseService) {
    this.userIdentity = userIdentity;
    this.caseService = caseService;
  }

  @PutMapping(value = "/invalid-address")
  public ResponseEntity<?> handleInvalidAddress(
      @RequestBody InvalidAddressDTO invalidAddressDto,
      @RequestHeader(required = false, value = "x-goog-iap-jwt-assertion") String jwtToken) {

    Case caze = caseService.getCaseByCaseId(invalidAddressDto.getCaseId());

    // Check user is authorised to invalidate a case address for this survey
    userIdentity.checkUserPermission(
        jwtToken, caze.getCollectionExercise().getSurvey(),
        UserGroupAuthorisedActivityType.CREATE_CASE_INVALID_ADDRESS);

    caseService.buildAndSendInvalidAddressCaseEvent(invalidAddressDto);

    return new ResponseEntity<>(HttpStatus.OK);
  }
}
