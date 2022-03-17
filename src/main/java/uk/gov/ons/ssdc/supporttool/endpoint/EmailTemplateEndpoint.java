package uk.gov.ons.ssdc.supporttool.endpoint;

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
import uk.gov.ons.ssdc.common.model.entity.EmailTemplate;
import uk.gov.ons.ssdc.common.model.entity.UserGroupAuthorisedActivityType;
import uk.gov.ons.ssdc.supporttool.model.dto.ui.EmailTemplateDto;
import uk.gov.ons.ssdc.supporttool.model.repository.EmailTemplateRepository;
import uk.gov.ons.ssdc.supporttool.security.UserIdentity;

@RestController
@RequestMapping(value = "/api/emailTemplates")
public class EmailTemplateEndpoint {
  private final EmailTemplateRepository emailTemplateRepository;
  private final UserIdentity userIdentity;

  public EmailTemplateEndpoint(
      EmailTemplateRepository emailTemplateRepository, UserIdentity userIdentity) {
    this.emailTemplateRepository = emailTemplateRepository;
    this.userIdentity = userIdentity;
  }

  @GetMapping
  public List<EmailTemplateDto> getTemplates(
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {
    userIdentity.checkGlobalUserPermission(
        userEmail, UserGroupAuthorisedActivityType.LIST_EMAIL_TEMPLATES);

    return emailTemplateRepository.findAll().stream()
        .map(
            emailTemplate -> {
              EmailTemplateDto emailTemplateDto = new EmailTemplateDto();
              emailTemplateDto.setTemplate(emailTemplate.getTemplate());
              emailTemplateDto.setPackCode(emailTemplate.getPackCode());
              emailTemplateDto.setNotifyTemplateId(emailTemplate.getNotifyTemplateId());
              emailTemplateDto.setDescription(emailTemplate.getDescription());
              emailTemplateDto.setMetadata(emailTemplate.getMetadata());
              return emailTemplateDto;
            })
        .collect(Collectors.toList());
  }

  @PostMapping
  public ResponseEntity<Void> createTemplate(
      @RequestBody EmailTemplateDto emailTemplateDto,
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {
    userIdentity.checkGlobalUserPermission(
        userEmail, UserGroupAuthorisedActivityType.CREATE_EMAIL_TEMPLATE);

    validateEmailTemplate(emailTemplateDto);

    EmailTemplate emailTemplate = new EmailTemplate();
    emailTemplate.setTemplate(emailTemplateDto.getTemplate());
    emailTemplate.setPackCode(emailTemplateDto.getPackCode());
    emailTemplate.setNotifyTemplateId(emailTemplateDto.getNotifyTemplateId());
    emailTemplate.setDescription(emailTemplateDto.getDescription());
    emailTemplate.setMetadata(emailTemplateDto.getMetadata());

    emailTemplateRepository.saveAndFlush(emailTemplate);

    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  private void validateEmailTemplate(EmailTemplateDto emailTemplateDto) {

    emailTemplateRepository
        .findAll()
        .forEach(
            emailTemplate -> {
              if (emailTemplate.getPackCode().equalsIgnoreCase(emailTemplateDto.getPackCode())) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Pack code already exists");
              }
            });

    Set<String> templateSet = new HashSet<>(Arrays.asList(emailTemplateDto.getTemplate()));
    if (templateSet.size() != emailTemplateDto.getTemplate().length) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Template cannot have duplicate columns");
    }
  }
}
