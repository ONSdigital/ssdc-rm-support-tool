package uk.gov.ons.ssdc.supporttool.endpoint;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import uk.gov.ons.ssdc.common.model.entity.UserGroupAuthorisedActivityType;
import uk.gov.ons.ssdc.supporttool.client.ExceptionManagerClient;
import uk.gov.ons.ssdc.supporttool.model.dto.rest.SkipMessageRequest;
import uk.gov.ons.ssdc.supporttool.security.UserIdentity;

@Controller
@RequestMapping(value = "/api/exceptionManager")
public class ExceptionManagerEndpoint {
  private final ExceptionManagerClient exceptionManagerClient;
  private final UserIdentity userIdentity;

  public ExceptionManagerEndpoint(
      ExceptionManagerClient exceptionManagerClient, UserIdentity userIdentity) {
    this.exceptionManagerClient = exceptionManagerClient;
    this.userIdentity = userIdentity;
  }

  @GetMapping(value = "/badMessagesSummary", produces = "application/json")
  public ResponseEntity<String> getBadMessagesSummary(
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {
    userIdentity.checkGlobalUserPermission(
        userEmail, UserGroupAuthorisedActivityType.EXCEPTION_MANAGER_VIEWER);

    return new ResponseEntity<>(exceptionManagerClient.getBadMessagesSummary(), HttpStatus.OK);
  }

  @GetMapping(value = "/badMessage/{messageHash}", produces = "application/json")
  public ResponseEntity<String> getBadMessageDetails(
      @PathVariable("messageHash") String messageHash,
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {
    userIdentity.checkGlobalUserPermission(
        userEmail, UserGroupAuthorisedActivityType.EXCEPTION_MANAGER_VIEWER);

    return new ResponseEntity<>(
        exceptionManagerClient.getBadMessageDetails(messageHash), HttpStatus.OK);
  }

  @GetMapping(value = "/peekMessage/{messageHash}", produces = "application/json")
  public ResponseEntity<String> peekMessage(
      @PathVariable("messageHash") String messageHash,
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {
    userIdentity.checkGlobalUserPermission(
        userEmail, UserGroupAuthorisedActivityType.EXCEPTION_MANAGER_PEEK);

    return new ResponseEntity<>(exceptionManagerClient.peekMessage(messageHash), HttpStatus.OK);
  }

  @GetMapping(value = "/skipMessage/{messageHash}", produces = "application/json")
  public ResponseEntity<Void> skipMessage(
      @PathVariable("messageHash") String messageHash,
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {
    userIdentity.checkGlobalUserPermission(
        userEmail, UserGroupAuthorisedActivityType.EXCEPTION_MANAGER_QUARANTINE);

    SkipMessageRequest skipMessageRequest = new SkipMessageRequest();
    skipMessageRequest.setMessageHash(messageHash);
    skipMessageRequest.setSkippingUser(userEmail);
    exceptionManagerClient.skipMessage(skipMessageRequest);
    return new ResponseEntity<>(HttpStatus.OK);
  }
}
