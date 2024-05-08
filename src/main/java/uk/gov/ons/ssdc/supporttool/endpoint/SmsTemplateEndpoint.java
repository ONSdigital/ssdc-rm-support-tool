package uk.gov.ons.ssdc.supporttool.endpoint;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.ons.ssdc.common.model.entity.SmsTemplate;
import uk.gov.ons.ssdc.common.model.entity.UserGroupAuthorisedActivityType;
import uk.gov.ons.ssdc.supporttool.model.dto.ui.SmsTemplateDto;
import uk.gov.ons.ssdc.supporttool.model.repository.SmsTemplateRepository;
import uk.gov.ons.ssdc.supporttool.security.AuthUser;

@RestController
@RequestMapping(value = "/api/smsTemplates")
public class SmsTemplateEndpoint {
  private static final Logger log = LoggerFactory.getLogger(SmsTemplateEndpoint.class);
  private final SmsTemplateRepository smsTemplateRepository;
  private final AuthUser authUser;

  public SmsTemplateEndpoint(SmsTemplateRepository smsTemplateRepository, AuthUser authUser) {
    this.smsTemplateRepository = smsTemplateRepository;
    this.authUser = authUser;
  }

  @GetMapping
  public List<SmsTemplateDto> getTemplates(
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {
    authUser.checkGlobalUserPermission(
        userEmail, UserGroupAuthorisedActivityType.LIST_SMS_TEMPLATES);

    return smsTemplateRepository.findAll().stream()
        .map(
            smsTemplate -> {
              SmsTemplateDto smsTemplateDto = new SmsTemplateDto();
              smsTemplateDto.setTemplate(smsTemplate.getTemplate());
              smsTemplateDto.setPackCode(smsTemplate.getPackCode());
              smsTemplateDto.setNotifyTemplateId(smsTemplate.getNotifyTemplateId());
              smsTemplateDto.setDescription(smsTemplate.getDescription());
              smsTemplateDto.setMetadata(smsTemplate.getMetadata());
              smsTemplateDto.setNotifyServiceRef(smsTemplate.getNotifyServiceRef());
              return smsTemplateDto;
            })
        .collect(Collectors.toList());
  }

  @PostMapping
  public ResponseEntity<Void> createTemplate(
      @RequestBody SmsTemplateDto smsTemplateDto,
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {
    authUser.checkGlobalUserPermission(
        userEmail, UserGroupAuthorisedActivityType.CREATE_SMS_TEMPLATE);

    validateTemplate(smsTemplateDto, userEmail);

    SmsTemplate smsTemplate = new SmsTemplate();
    smsTemplate.setTemplate(smsTemplateDto.getTemplate());
    smsTemplate.setPackCode(smsTemplateDto.getPackCode());
    smsTemplate.setNotifyTemplateId(smsTemplateDto.getNotifyTemplateId());
    smsTemplate.setDescription(smsTemplateDto.getDescription());
    smsTemplate.setMetadata(smsTemplateDto.getMetadata());
    smsTemplate.setNotifyServiceRef(smsTemplateDto.getNotifyServiceRef());

    smsTemplateRepository.saveAndFlush(smsTemplate);

    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  private void validateTemplate(SmsTemplateDto smsTemplateDto, String userEmail) {
    Set<String> templateSet = new HashSet<>(Arrays.asList(smsTemplateDto.getTemplate()));
    if (templateSet.size() != smsTemplateDto.getTemplate().length) {
      log.atWarn()
          .setMessage("Failed to create sms template, template cannot have duplicate columns")
          .addKeyValue("httpStatus", HttpStatus.BAD_REQUEST)
          .addKeyValue("userEmail", userEmail)
          .addKeyValue("template", smsTemplateDto.getTemplate())
          .log();
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Template cannot have duplicate columns");
    }

    smsTemplateRepository
        .findAll()
        .forEach(
            smsTemplate -> {
              if (smsTemplate.getPackCode().equalsIgnoreCase(smsTemplateDto.getPackCode())) {
                log.atWarn()
                    .setMessage("Failed to create sms template, Pack code already exists")
                    .addKeyValue("packCode", smsTemplateDto.getPackCode())
                    .addKeyValue("userEmail", userEmail)
                    .addKeyValue("httpStatus", HttpStatus.BAD_REQUEST)
                    .log();
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Pack code already exists");
              }
            });
  }
}
