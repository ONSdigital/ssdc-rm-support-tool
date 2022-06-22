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
import uk.gov.ons.ssdc.common.model.entity.ActionRuleSurveySmsTemplate;
import uk.gov.ons.ssdc.common.model.entity.SmsTemplate;
import uk.gov.ons.ssdc.common.model.entity.Survey;
import uk.gov.ons.ssdc.common.model.entity.UserGroupAuthorisedActivityType;
import uk.gov.ons.ssdc.supporttool.model.dto.ui.AllowTemplateOnSurvey;
import uk.gov.ons.ssdc.supporttool.model.repository.ActionRuleSurveySmsTemplateRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.SmsTemplateRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.SurveyRepository;
import uk.gov.ons.ssdc.supporttool.security.UserIdentity;

@RestController
@RequestMapping(value = "/api/actionRuleSurveySmsTemplates")
public class ActionRuleSurveySmsTemplateEndpoint {
  private static final Logger log =
      LoggerFactory.getLogger(ActionRuleSurveySmsTemplateEndpoint.class);
  private final ActionRuleSurveySmsTemplateRepository actionRuleSurveySmsTemplateRepository;
  private final SurveyRepository surveyRepository;
  private final SmsTemplateRepository smsTemplateRepository;
  private final UserIdentity userIdentity;

  public ActionRuleSurveySmsTemplateEndpoint(
      ActionRuleSurveySmsTemplateRepository actionRuleSurveySmsTemplateRepository,
      SurveyRepository surveyRepository,
      SmsTemplateRepository smsTemplateRepository,
      UserIdentity userIdentity) {
    this.actionRuleSurveySmsTemplateRepository = actionRuleSurveySmsTemplateRepository;
    this.surveyRepository = surveyRepository;
    this.smsTemplateRepository = smsTemplateRepository;
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
                      .with("userEmail", userEmail)
                      .with("surveyId", surveyId)
                      .warn("Survey not found");
                  return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Survey not found");
                });
    userIdentity.checkUserPermission(
        userEmail,
        survey,
        UserGroupAuthorisedActivityType.LIST_ALLOWED_SMS_TEMPLATES_ON_ACTION_RULES);

    return actionRuleSurveySmsTemplateRepository.findBySurvey(survey).stream()
        .map(arsst -> arsst.getSmsTemplate().getPackCode())
        .collect(Collectors.toList());
  }

  @PostMapping
  public ResponseEntity<String> createActionRuleSurveySmsTemplate(
      @RequestBody AllowTemplateOnSurvey allowTemplateOnSurvey,
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {
    Survey survey =
        surveyRepository
            .findById(allowTemplateOnSurvey.getSurveyId())
            .orElseThrow(
                () -> {
                  log.with("httpStatus", HttpStatus.BAD_REQUEST)
                      .with("userEmail", userEmail)
                      .with("surveyId", allowTemplateOnSurvey.getSurveyId())
                      .warn("Survey not found");
                  return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Survey not found");
                });

    userIdentity.checkUserPermission(
        userEmail, survey, UserGroupAuthorisedActivityType.ALLOW_SMS_TEMPLATE_ON_ACTION_RULE);

    SmsTemplate smsTemplate =
        smsTemplateRepository
            .findById(allowTemplateOnSurvey.getPackCode())
            .orElseThrow(
                () -> {
                  log.with("httpStatus", HttpStatus.BAD_REQUEST)
                      .with("userEmail", userEmail)
                      .with("packCode", allowTemplateOnSurvey.getPackCode())
                      .warn("SMS template not found");
                  return new ResponseStatusException(
                      HttpStatus.BAD_REQUEST, "SMS template not found");
                });

    Optional<String> errorOpt = validate(survey, Set.of(smsTemplate.getTemplate()));
    if (errorOpt.isPresent()) {
      log.with("httpStatus", HttpStatus.BAD_REQUEST)
          .with("userEmail", userEmail)
          .with("validationErrors", errorOpt.get())
          .warn("There were errors validating the sms template");
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
