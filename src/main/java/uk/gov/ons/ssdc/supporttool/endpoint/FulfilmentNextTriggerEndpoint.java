package uk.gov.ons.ssdc.supporttool.endpoint;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.ons.ssdc.common.model.entity.FulfilmentNextTrigger;
import uk.gov.ons.ssdc.common.model.entity.UserGroupAuthorisedActivityType;
import uk.gov.ons.ssdc.supporttool.model.repository.FulfilmentNextTriggerRepository;
import uk.gov.ons.ssdc.supporttool.security.AuthUser;

@RestController
@RequestMapping(value = "/api/fulfilmentNextTriggers")
public class FulfilmentNextTriggerEndpoint {
  private static final Logger log = LoggerFactory.getLogger(FulfilmentNextTriggerEndpoint.class);
  private final FulfilmentNextTriggerRepository fulfilmentNextTriggerRepository;
  private final AuthUser authUser;

  public FulfilmentNextTriggerEndpoint(
      FulfilmentNextTriggerRepository fulfilmentNextTriggerRepository, AuthUser authUser) {
    this.fulfilmentNextTriggerRepository = fulfilmentNextTriggerRepository;
    this.authUser = authUser;
  }

  @GetMapping
  public OffsetDateTime getTriggerDateTime(
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {
    authUser.checkGlobalUserPermission(
        userEmail, UserGroupAuthorisedActivityType.CONFIGURE_FULFILMENT_TRIGGER);

    List<FulfilmentNextTrigger> fulfilmentNextTriggers = fulfilmentNextTriggerRepository.findAll();
    if (fulfilmentNextTriggers.size() > 1) {
      log.atWarn()
          .setMessage(
              "Failed to get fulfilment trigger time, multiple triggers not currently supported")
          .addKeyValue("userEmail", userEmail)
          .addKeyValue("httpStatus", HttpStatus.INTERNAL_SERVER_ERROR)
          .log();
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Multiple triggers not currently supported");
    }

    if (fulfilmentNextTriggers.isEmpty()) {
      log.atWarn()
          .setMessage("Failed to get fulfilment trigger time, no fulfilment trigger found")
          .addKeyValue("userEmail", userEmail)
          .addKeyValue("httpStatus", HttpStatus.NOT_FOUND)
          .log();
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    return fulfilmentNextTriggers.get(0).getTriggerDateTime();
  }

  @PostMapping
  public void setTriggerDateTime(
      @RequestParam(value = "triggerDateTime") String triggerDateTime,
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {
    authUser.checkGlobalUserPermission(
        userEmail, UserGroupAuthorisedActivityType.CONFIGURE_FULFILMENT_TRIGGER);

    List<FulfilmentNextTrigger> fulfilmentNextTriggers = fulfilmentNextTriggerRepository.findAll();

    if (fulfilmentNextTriggers.size() > 1) {
      log.atWarn()
          .setMessage(
              "Failed to set fulfilment trigger time, multiple triggers not currently supported")
          .addKeyValue("userEmail", userEmail)
          .addKeyValue("httpStatus", HttpStatus.INTERNAL_SERVER_ERROR)
          .log();
      throw new HttpClientErrorException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Multiple triggers not currently supported");
    }

    FulfilmentNextTrigger fulfilmentNextTrigger;

    if (fulfilmentNextTriggers.isEmpty()) {
      fulfilmentNextTrigger = new FulfilmentNextTrigger();
      fulfilmentNextTrigger.setId(UUID.randomUUID());
    } else {
      fulfilmentNextTrigger = fulfilmentNextTriggers.get(0);
    }

    OffsetDateTime triggerDateTimeParsed =
        OffsetDateTime.parse(triggerDateTime, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    fulfilmentNextTrigger.setTriggerDateTime(triggerDateTimeParsed);
    fulfilmentNextTriggerRepository.saveAndFlush(fulfilmentNextTrigger);
  }
}
