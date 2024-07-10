package uk.gov.ons.ssdc.supporttool.endpoint;

import static uk.gov.ons.ssdc.supporttool.utility.AllowTemplateOnSurveyValidator.validate;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.ons.ssdc.common.model.entity.EmailTemplate;
import uk.gov.ons.ssdc.common.model.entity.FulfilmentSurveyEmailTemplate;
import uk.gov.ons.ssdc.common.model.entity.Survey;
import uk.gov.ons.ssdc.common.model.entity.UserGroupAuthorisedActivityType;
import uk.gov.ons.ssdc.supporttool.model.dto.ui.AllowTemplateOnSurvey;
import uk.gov.ons.ssdc.supporttool.model.dto.ui.EmailTemplateDto;
import uk.gov.ons.ssdc.supporttool.model.repository.EmailTemplateRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.FulfilmentSurveyEmailTemplateRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.SurveyRepository;
import uk.gov.ons.ssdc.supporttool.security.AuthUser;
import uk.gov.ons.ssdc.supporttool.service.SurveyService;

@RestController
@RequestMapping(value = "/api/fulfilmentSurveyEmailTemplates")
public class FulfilmentSurveyEmailTemplateEndpoint {
  private static final Logger log =
      LoggerFactory.getLogger(FulfilmentSurveyEmailTemplateEndpoint.class);

  private final FulfilmentSurveyEmailTemplateRepository fulfilmentSurveyEmailTemplateRepository;
  private final SurveyRepository surveyRepository;
  private final EmailTemplateRepository emailTemplateRepository;
  private final AuthUser authUser;
  private final SurveyService surveyService;

  public FulfilmentSurveyEmailTemplateEndpoint(
      FulfilmentSurveyEmailTemplateRepository fulfilmentSurveyEmailTemplateRepository,
      SurveyRepository surveyRepository,
      EmailTemplateRepository emailTemplateRepository,
      AuthUser authUser,
      SurveyService surveyService) {
    this.fulfilmentSurveyEmailTemplateRepository = fulfilmentSurveyEmailTemplateRepository;
    this.surveyRepository = surveyRepository;
    this.emailTemplateRepository = emailTemplateRepository;
    this.authUser = authUser;
    this.surveyService = surveyService;
  }

  @GetMapping
  public List<EmailTemplateDto> getAllowedEmailTemplatesBySurvey(
      @RequestParam(value = "surveyId") UUID surveyId,
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {

    authUser.checkUserPermission(
        userEmail,
        surveyId,
        UserGroupAuthorisedActivityType.LIST_ALLOWED_EMAIL_TEMPLATES_ON_FULFILMENTS);

    Survey survey =
        surveyRepository
            .findById(surveyId)
            .orElseThrow(
                () -> {
                  log.atWarn()
                      .setMessage("Failed to get allowed email templates, survey not found")
                      .addKeyValue("surveyId", surveyId)
                      .addKeyValue("userEmail", userEmail)
                      .addKeyValue("httpStatus", HttpStatus.BAD_REQUEST)
                      .log();
                  return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Survey not found");
                });

    return fulfilmentSurveyEmailTemplateRepository.findBySurvey(survey).stream()
        .map(fset -> new EmailTemplateDto(fset.getEmailTemplate()))
        .toList();
  }

  @PostMapping
  public ResponseEntity<String> createFulfilmentSurveyEmailTemplate(
      @RequestBody AllowTemplateOnSurvey allowTemplateOnSurvey,
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {
    authUser.checkUserPermission(
        userEmail,
        allowTemplateOnSurvey.getSurveyId(),
        UserGroupAuthorisedActivityType.ALLOW_EMAIL_TEMPLATE_ON_FULFILMENT);

    Survey survey =
        surveyRepository
            .findById(allowTemplateOnSurvey.getSurveyId())
            .orElseThrow(
                () -> {
                  log.atWarn()
                      .setMessage(
                          "Failed to create fulfilment survey email templates, survey not found")
                      .addKeyValue("surveyId", allowTemplateOnSurvey.getSurveyId())
                      .addKeyValue("userEmail", userEmail)
                      .addKeyValue("httpStatus", HttpStatus.BAD_REQUEST)
                      .log();
                  return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Survey not found");
                });

    EmailTemplate emailTemplate =
        emailTemplateRepository
            .findById(allowTemplateOnSurvey.getPackCode())
            .orElseThrow(
                () -> {
                  log.atWarn()
                      .setMessage(
                          "Failed to create fulfilment survey email templates, email template not found")
                      .addKeyValue("packCode", allowTemplateOnSurvey.getPackCode())
                      .addKeyValue("userEmail", userEmail)
                      .addKeyValue("httpStatus", HttpStatus.BAD_REQUEST)
                      .log();
                  return new ResponseStatusException(
                      HttpStatus.BAD_REQUEST, "Email template not found");
                });

    if (fulfilmentSurveyEmailTemplateRepository
        .existsFulfilmentSurveyEmailTemplateByEmailTemplateAndSurvey(emailTemplate, survey)) {
      log.atWarn()
          .setMessage(
              "Failed to create fulfilment email template, Email Template already exists for survey")
          .addKeyValue("httpStatus", HttpStatus.CONFLICT)
          .addKeyValue("packCode", allowTemplateOnSurvey.getPackCode())
          .addKeyValue("userEmail", userEmail)
          .log();
      return new ResponseEntity<>("Email Template already exists for survey", HttpStatus.CONFLICT);
    }

    Optional<String> errorOpt = validate(survey, Set.of(emailTemplate.getTemplate()));
    if (errorOpt.isPresent()) {
      return new ResponseEntity<>(errorOpt.get(), HttpStatus.BAD_REQUEST);
    }

    FulfilmentSurveyEmailTemplate fulfilmentSurveyEmailTemplate =
        new FulfilmentSurveyEmailTemplate();
    fulfilmentSurveyEmailTemplate.setId(UUID.randomUUID());
    fulfilmentSurveyEmailTemplate.setSurvey(survey);
    fulfilmentSurveyEmailTemplate.setEmailTemplate(emailTemplate);

    fulfilmentSurveyEmailTemplate =
        fulfilmentSurveyEmailTemplateRepository.saveAndFlush(fulfilmentSurveyEmailTemplate);

    surveyService.publishSurveyUpdate(fulfilmentSurveyEmailTemplate.getSurvey(), userEmail);

    return new ResponseEntity<>("OK", HttpStatus.CREATED);
  }
}
