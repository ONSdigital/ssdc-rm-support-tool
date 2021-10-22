package uk.gov.ons.ssdc.supporttool.endpoint;

import java.util.List;
import java.util.stream.Collectors;
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
import uk.gov.ons.ssdc.supporttool.security.UserIdentity;

@RestController
@RequestMapping(value = "/api/smsTemplates")
public class SmsTemplateEndpoint {
  private final SmsTemplateRepository smsTemplateRepository;
  private final UserIdentity userIdentity;

  public SmsTemplateEndpoint(
      SmsTemplateRepository smsTemplateRepository, UserIdentity userIdentity) {
    this.smsTemplateRepository = smsTemplateRepository;
    this.userIdentity = userIdentity;
  }

  @GetMapping
  public List<SmsTemplateDto> getTemplates(
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {
    userIdentity.checkGlobalUserPermission(
        userEmail, UserGroupAuthorisedActivityType.LIST_SMS_TEMPLATES);

    return smsTemplateRepository.findAll().stream()
        .map(
            smsTemplate -> {
              SmsTemplateDto smsTemplateDto = new SmsTemplateDto();
              smsTemplateDto.setTemplate(smsTemplate.getTemplate());
              smsTemplateDto.setPackCode(smsTemplate.getPackCode());
              smsTemplateDto.setNotifyTemplateId(smsTemplate.getNotifyTemplateId());
              return smsTemplateDto;
            })
        .collect(Collectors.toList());
  }

  @PostMapping
  public ResponseEntity<Void> createTemplate(
      @RequestBody SmsTemplateDto smsTemplateDto,
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {
    userIdentity.checkGlobalUserPermission(
        userEmail, UserGroupAuthorisedActivityType.CREATE_SMS_TEMPLATE);

    smsTemplateRepository
        .findById(smsTemplateDto.getPackCode())
        .ifPresent(
            smsTemplate -> {
              throw new ResponseStatusException(
                  HttpStatus.CONFLICT,
                  String.format("Packcode '%s' already exists", smsTemplate.getPackCode()));
            });

    SmsTemplate smsTemplate = new SmsTemplate();
    smsTemplate.setTemplate(smsTemplateDto.getTemplate());
    smsTemplate.setPackCode(smsTemplateDto.getPackCode());
    smsTemplate.setNotifyTemplateId(smsTemplateDto.getNotifyTemplateId());

    smsTemplateRepository.saveAndFlush(smsTemplate);

    return new ResponseEntity<>(HttpStatus.CREATED);
  }
}
