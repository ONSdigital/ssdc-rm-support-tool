package uk.gov.ons.ssdc.supporttool.endpoint;

import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import uk.gov.ons.ssdc.supporttool.client.ExceptionManagerClient;

@Controller
@RequestMapping(value = "/api/exceptionManager")
public class ExceptionManager {
  private final ExceptionManagerClient exceptionManagerClient;

  public ExceptionManager(
      ExceptionManagerClient exceptionManagerClient) {
    this.exceptionManagerClient = exceptionManagerClient;
  }


  @GetMapping(value = "/badMessagesSummary", produces="application/json")
  public ResponseEntity<String> getBadMessagesSummary() {
    return new ResponseEntity<>(exceptionManagerClient.getBadMessagesSummary(), HttpStatus.OK);
  }

  @GetMapping(value = "/badMessage/{messageHash}", produces="application/json")
  public ResponseEntity<String> getBadMessageDetails(@PathVariable("messageHash") String messageHash) {
    return new ResponseEntity<>(exceptionManagerClient.getBadMessageDetails(messageHash), HttpStatus.OK);
  }
}
