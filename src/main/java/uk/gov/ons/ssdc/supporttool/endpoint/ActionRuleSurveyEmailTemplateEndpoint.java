package uk.gov.ons.ssdc.supporttool.endpoint;

import static uk.gov.ons.ssdc.supporttool.utility.AllowTemplateOnSurveyValidator.validate;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
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
import uk.gov.ons.ssdc.supporttool.security.UserIdentity;

@RestController
@RequestMapping(value = "/api/actionRuleSurveyEmailTemplates")
public class ActionRuleSurveyEmailTemplateEndpoint {

  private static final Logger log =
      LoggerFactory.getLogger(ActionRuleSurveyEmailTemplateEndpoint.class);
  private final ActionRuleSurveyEmailTemplateRepository actionRuleSurveyEmailTemplateRepository;
  private final SurveyRepository surveyRepository;
  private final EmailTemplateRepository emailTemplateRepository;
  private final UserIdentity userIdentity;

  public ActionRuleSurveyEmailTemplateEndpoint(
      ActionRuleSurveyEmailTemplateRepository actionRuleSurveyEmailTemplateRepository,
      SurveyRepository surveyRepository,
      EmailTemplateRepository emailTemplateRepository,
      UserIdentity userIdentity) {
    this.actionRuleSurveyEmailTemplateRepository = actionRuleSurveyEmailTemplateRepository;
    this.surveyRepository = surveyRepository;
    this.emailTemplateRepository = emailTemplateRepository;
    this.userIdentity = userIdentity;
  }

  @GetMapping
  // TODO: make this a bit more RESTful... but it does the job just fine; we don't really need a DTO
  public List<String> getAllowedPackCodesBySurvey(
      @RequestParam(value = "surveyId") UUID surveyId,
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {
    Survey survey =
        surveyRepository
            .findById(surveyId)
            .orElseThrow(
                () -> {
                  log.with("httpStatus", HttpStatus.BAD_REQUEST)
                      .with("surveyId", surveyId)
                      .with("userEmail", userEmail)
                      .warn("Failed to get allowed pack codes, survey not found");
                  return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Survey not found");
                });

    userIdentity.checkUserPermission(
        userEmail,
        survey,
        UserGroupAuthorisedActivityType.LIST_ALLOWED_EMAIL_TEMPLATES_ON_ACTION_RULES);

    return actionRuleSurveyEmailTemplateRepository.findBySurvey(survey).stream()
        .map(arsst -> arsst.getEmailTemplate().getPackCode())
        .collect(Collectors.toList());
  }

  @PostMapping
  public ResponseEntity<String> createActionRuleSurveyEmailTemplate(
      @RequestBody AllowTemplateOnSurvey allowTemplateOnSurvey,
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {
    Survey survey =
        surveyRepository
            .findById(allowTemplateOnSurvey.getSurveyId())
            .orElseThrow(
                () -> {
                  log.with("httpStatus", HttpStatus.BAD_REQUEST)
                      .with("surveyId", allowTemplateOnSurvey.getSurveyId())
                      .with("userEmail", userEmail)
                      .warn("Failed to create action rule survey email template, survey not found");
                  return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Survey not found");
                });

    userIdentity.checkUserPermission(
        userEmail, survey, UserGroupAuthorisedActivityType.ALLOW_EMAIL_TEMPLATE_ON_ACTION_RULE);

    EmailTemplate emailTemplate =
        emailTemplateRepository
            .findById(allowTemplateOnSurvey.getPackCode())
            .orElseThrow(
                () -> {
                  log.with("httpStatus", HttpStatus.BAD_REQUEST)
                      .with("packCode", allowTemplateOnSurvey.getPackCode())
                      .with("userEmail", userEmail)
                      .warn(
                          "Failed to create action rule survey email template, email template not found");
                  return new ResponseStatusException(
                      HttpStatus.BAD_REQUEST, "Email template not found");
                });

    if (actionRuleSurveyEmailTemplateRepository
            .countActionRuleSurveyEmailTemplateByEmailTemplateAndAndSurvey(emailTemplate, survey)
        != 0) {
      log.with("httpStatus", HttpStatus.CONFLICT)
          .with("packCode", allowTemplateOnSurvey.getPackCode())
          .with("userEmail", userEmail)
          .warn(
              "Failed to create action rule survey email template, Email Template already exists for survey");
      return new ResponseEntity<>("Email already exists for survey", HttpStatus.CONFLICT);
    }

    Optional<String> errorOpt = validate(survey, Set.of(emailTemplate.getTemplate()));
    if (errorOpt.isPresent()) {
      log.with("httpStatus", HttpStatus.BAD_REQUEST)
          .with("userEmail", userEmail)
          .with("validationErrors", errorOpt.get())
          .warn(
              "Failed to create action rule survey email template, there were errors validating the email template");
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
