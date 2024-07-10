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
import uk.gov.ons.ssdc.common.model.entity.ActionRuleSurveySmsTemplate;
import uk.gov.ons.ssdc.common.model.entity.SmsTemplate;
import uk.gov.ons.ssdc.common.model.entity.Survey;
import uk.gov.ons.ssdc.common.model.entity.UserGroupAuthorisedActivityType;
import uk.gov.ons.ssdc.supporttool.model.dto.ui.AllowTemplateOnSurvey;
import uk.gov.ons.ssdc.supporttool.model.repository.ActionRuleSurveySmsTemplateRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.SmsTemplateRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.SurveyRepository;
import uk.gov.ons.ssdc.supporttool.security.AuthUser;

@RestController
@RequestMapping(value = "/api/actionRuleSurveySmsTemplates")
public class ActionRuleSurveySmsTemplateEndpoint {
  private static final Logger log =
      LoggerFactory.getLogger(ActionRuleSurveySmsTemplateEndpoint.class);
  private final ActionRuleSurveySmsTemplateRepository actionRuleSurveySmsTemplateRepository;
  private final SurveyRepository surveyRepository;
  private final SmsTemplateRepository smsTemplateRepository;
  private final AuthUser authUser;

  public ActionRuleSurveySmsTemplateEndpoint(
      ActionRuleSurveySmsTemplateRepository actionRuleSurveySmsTemplateRepository,
      SurveyRepository surveyRepository,
      SmsTemplateRepository smsTemplateRepository,
      AuthUser authUser) {
    this.actionRuleSurveySmsTemplateRepository = actionRuleSurveySmsTemplateRepository;
    this.surveyRepository = surveyRepository;
    this.smsTemplateRepository = smsTemplateRepository;
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
        UserGroupAuthorisedActivityType.LIST_ALLOWED_SMS_TEMPLATES_ON_ACTION_RULES);

    Survey survey =
        surveyRepository
            .findById(surveyId)
            .orElseThrow(
                () -> {
                  log.atWarn()
                      .setMessage("Failed to get allowed pack codes, survey not found")
                      .addKeyValue("httpStatus", HttpStatus.BAD_REQUEST)
                      .addKeyValue("userEmail", userEmail)
                      .addKeyValue("surveyId", surveyId)
                      .log();
                  return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Survey not found");
                });

    return actionRuleSurveySmsTemplateRepository.findBySurvey(survey).stream()
        .map(arsst -> arsst.getSmsTemplate().getPackCode())
        .collect(Collectors.toList());
  }

  @PostMapping
  public ResponseEntity<String> createActionRuleSurveySmsTemplate(
      @RequestBody AllowTemplateOnSurvey allowTemplateOnSurvey,
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {

    authUser.checkUserPermission(
        userEmail,
        allowTemplateOnSurvey.getSurveyId(),
        UserGroupAuthorisedActivityType.ALLOW_SMS_TEMPLATE_ON_ACTION_RULE);

    Survey survey =
        surveyRepository
            .findById(allowTemplateOnSurvey.getSurveyId())
            .orElseThrow(
                () -> {
                  log.atWarn()
                      .setMessage(
                          "Failed to create action rule survey sms template, survey not found")
                      .addKeyValue("httpStatus", HttpStatus.BAD_REQUEST)
                      .addKeyValue("userEmail", userEmail)
                      .addKeyValue("surveyId", allowTemplateOnSurvey.getSurveyId())
                      .log();
                  return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Survey not found");
                });

    SmsTemplate smsTemplate =
        smsTemplateRepository
            .findById(allowTemplateOnSurvey.getPackCode())
            .orElseThrow(
                () -> {
                  log.atWarn()
                      .setMessage(
                          "Failed to create action rule survey sms template, SMS template not found")
                      .addKeyValue("httpStatus", HttpStatus.BAD_REQUEST)
                      .addKeyValue("userEmail", userEmail)
                      .addKeyValue("packCode", allowTemplateOnSurvey.getPackCode())
                      .log();
                  return new ResponseStatusException(
                      HttpStatus.BAD_REQUEST, "SMS template not found");
                });

    if (actionRuleSurveySmsTemplateRepository
        .existsActionRuleSurveySmsTemplateBySmsTemplateAndSurvey(smsTemplate, survey)) {
      log.atWarn()
          .addKeyValue("httpStatus", HttpStatus.CONFLICT)
          .addKeyValue("packCode", allowTemplateOnSurvey.getPackCode())
          .addKeyValue("userEmail", userEmail)
          .setMessage(
              "Failed to create action rule sms template, SMS Template already exists for survey")
          .log();
      return new ResponseEntity<>("SMS Template already exists for survey", HttpStatus.CONFLICT);
    }

    Optional<String> errorOpt = validate(survey, Set.of(smsTemplate.getTemplate()));
    if (errorOpt.isPresent()) {
      log.atWarn()
          .addKeyValue("httpStatus", HttpStatus.BAD_REQUEST)
          .addKeyValue("userEmail", userEmail)
          .addKeyValue("validationErrors", errorOpt.get())
          .setMessage(
              "Failed to create action rule sms template, there were errors validating the sms template")
          .log();
      return new ResponseEntity<>(errorOpt.get(), HttpStatus.BAD_REQUEST);
    }

    ActionRuleSurveySmsTemplate actionRuleSurveySmsTemplate = new ActionRuleSurveySmsTemplate();
    actionRuleSurveySmsTemplate.setId(UUID.randomUUID());
    actionRuleSurveySmsTemplate.setSurvey(survey);
    actionRuleSurveySmsTemplate.setSmsTemplate(smsTemplate);

    actionRuleSurveySmsTemplateRepository.save(actionRuleSurveySmsTemplate);

    return new ResponseEntity<>("OK", HttpStatus.CREATED);
  }
}
