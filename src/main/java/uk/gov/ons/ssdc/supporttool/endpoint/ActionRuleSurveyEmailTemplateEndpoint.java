package uk.gov.ons.ssdc.supporttool.endpoint;

import static uk.gov.ons.ssdc.supporttool.utility.AllowTemplateOnSurveyValidator.validate;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.ons.ssdc.common.model.entity.ActionRuleSurveyEmailTemplate;
import uk.gov.ons.ssdc.common.model.entity.EmailTemplate;
import uk.gov.ons.ssdc.common.model.entity.Survey;
import uk.gov.ons.ssdc.common.model.entity.UserGroupAuthorisedActivityType;
import uk.gov.ons.ssdc.supporttool.model.dto.ui.AllowTemplateOnSurvey;
import uk.gov.ons.ssdc.supporttool.model.repository.ActionRuleSurveyEmailTemplateRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.EmailTemplateRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.SurveyRepository;
import uk.gov.ons.ssdc.supporttool.security.AuthUser;

@RestController
@RequestMapping(value = "/api/actionRuleSurveyEmailTemplates")
public class ActionRuleSurveyEmailTemplateEndpoint {

  private static final Logger log =
      LoggerFactory.getLogger(ActionRuleSurveyEmailTemplateEndpoint.class);
  private final ActionRuleSurveyEmailTemplateRepository actionRuleSurveyEmailTemplateRepository;
  private final SurveyRepository surveyRepository;
  private final EmailTemplateRepository emailTemplateRepository;
  private final AuthUser authUser;

  public ActionRuleSurveyEmailTemplateEndpoint(
      ActionRuleSurveyEmailTemplateRepository actionRuleSurveyEmailTemplateRepository,
      SurveyRepository surveyRepository,
      EmailTemplateRepository emailTemplateRepository,
      AuthUser authUser) {
    this.actionRuleSurveyEmailTemplateRepository = actionRuleSurveyEmailTemplateRepository;
    this.surveyRepository = surveyRepository;
    this.emailTemplateRepository = emailTemplateRepository;
    this.authUser = authUser;
  }

  @GetMapping
  // TODO: make this a bit more RESTful... but it does the job just fine; we don't really need a DTO
  public List<String> getAllowedPackCodesBySurvey(
      @RequestParam(value = "surveyId") UUID surveyId,
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {

    authUser.checkUserPermission(
        userEmail,
        surveyId,
        UserGroupAuthorisedActivityType.LIST_ALLOWED_EMAIL_TEMPLATES_ON_ACTION_RULES);

    Survey survey =
        surveyRepository
            .findById(surveyId)
            .orElseThrow(
                () -> {
                  log.atWarn()
                      .setMessage("Failed to get allowed pack codes, survey not found")
                      .addKeyValue("httpStatus", HttpStatus.BAD_REQUEST)
                      .addKeyValue("surveyId", surveyId)
                      .addKeyValue("userEmail", userEmail)
                      .log();
                  return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Survey not found");
                });

    return actionRuleSurveyEmailTemplateRepository.findBySurvey(survey).stream()
        .map(arsst -> arsst.getEmailTemplate().getPackCode())
        .collect(Collectors.toList());
  }

  @PostMapping
  public ResponseEntity<String> createActionRuleSurveyEmailTemplate(
      @RequestBody AllowTemplateOnSurvey allowTemplateOnSurvey,
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {

    authUser.checkUserPermission(
        userEmail,
        allowTemplateOnSurvey.getSurveyId(),
        UserGroupAuthorisedActivityType.ALLOW_EMAIL_TEMPLATE_ON_ACTION_RULE);

    Survey survey =
        surveyRepository
            .findById(allowTemplateOnSurvey.getSurveyId())
            .orElseThrow(
                () -> {
                  log.atWarn()
                      .setMessage(
                          "Failed to create action rule survey email template, survey not found")
                      .addKeyValue("httpStatus", HttpStatus.BAD_REQUEST)
                      .addKeyValue("surveyId", allowTemplateOnSurvey.getSurveyId())
                      .addKeyValue("userEmail", userEmail)
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
                          "Failed to create action rule survey email template, email template not found")
                      .addKeyValue("httpStatus", HttpStatus.BAD_REQUEST)
                      .addKeyValue("packCode", allowTemplateOnSurvey.getPackCode())
                      .addKeyValue("userEmail", userEmail)
                      .log();
                  return new ResponseStatusException(
                      HttpStatus.BAD_REQUEST, "Email template not found");
                });

    if (actionRuleSurveyEmailTemplateRepository
        .existsActionRuleSurveyEmailTemplateByEmailTemplateAndSurvey(emailTemplate, survey)) {
      log.atWarn()
          .setMessage(
              "Failed to create action rule email template, Email Template already exists for survey")
          .addKeyValue("httpStatus", HttpStatus.CONFLICT)
          .addKeyValue("packCode", allowTemplateOnSurvey.getPackCode())
          .addKeyValue("userEmail", userEmail)
          .log();
      return new ResponseEntity<>("Email Template already exists for survey", HttpStatus.CONFLICT);
    }

    Optional<String> errorOpt = validate(survey, Set.of(emailTemplate.getTemplate()));
    if (errorOpt.isPresent()) {
      log.atWarn()
          .setMessage(
              "Failed to create action rule survey email template, there were errors validating the email template")
          .addKeyValue("httpStatus", HttpStatus.BAD_REQUEST)
          .addKeyValue("userEmail", userEmail)
          .addKeyValue("validationErrors", errorOpt.get())
          .log();
      return new ResponseEntity<>(errorOpt.get(), HttpStatus.BAD_REQUEST);
    }

    ActionRuleSurveyEmailTemplate actionRuleSurveyEmailTemplate =
        new ActionRuleSurveyEmailTemplate();
    actionRuleSurveyEmailTemplate.setId(UUID.randomUUID());
    actionRuleSurveyEmailTemplate.setSurvey(survey);
    actionRuleSurveyEmailTemplate.setEmailTemplate(emailTemplate);

    actionRuleSurveyEmailTemplateRepository.save(actionRuleSurveyEmailTemplate);

    return new ResponseEntity<>("OK", HttpStatus.CREATED);
  }
}
