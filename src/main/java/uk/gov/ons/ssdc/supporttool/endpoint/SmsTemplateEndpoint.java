package uk.gov.ons.ssdc.supporttool.endpoint;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
  private static final Logger log = LoggerFactory.getLogger(SmsTemplateEndpoint.class);
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
    userIdentity.checkGlobalUserPermission(
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
      log.with("httpStatus", HttpStatus.BAD_REQUEST)
          .with("userEmail", userEmail)
          .with("template", smsTemplateDto.getTemplate())
          .warn("Failed to create sms template, template cannot have duplicate columns");
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Template cannot have duplicate columns");
    }

    smsTemplateRepository
        .findAll()
        .forEach(
            smsTemplate -> {
              if (smsTemplate.getPackCode().equalsIgnoreCase(smsTemplateDto.getPackCode())) {
                log.with("packCode", smsTemplateDto.getPackCode())
                    .with("userEmail", userEmail)
                    .with("httpStatus", HttpStatus.BAD_REQUEST)
                    .warn("Failed to create sms template, Pack code already exists");
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Pack code already exists");
              }
            });
  }
}
