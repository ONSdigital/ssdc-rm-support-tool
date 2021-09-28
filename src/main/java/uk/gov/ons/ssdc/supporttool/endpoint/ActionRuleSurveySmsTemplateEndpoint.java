package uk.gov.ons.ssdc.supporttool.endpoint;

import static uk.gov.ons.ssdc.supporttool.utility.AllowTemplateOnSurveyValidator.validate;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.ons.ssdc.common.model.entity.ActionRuleSurveySmsTemplate;
import uk.gov.ons.ssdc.common.model.entity.SmsTemplate;
import uk.gov.ons.ssdc.common.model.entity.Survey;
import uk.gov.ons.ssdc.supporttool.model.dto.messaging.AllowTemplateOnSurvey;
import uk.gov.ons.ssdc.supporttool.model.repository.ActionRuleSurveySmsTemplateRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.SmsTemplateRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.SurveyRepository;

@Controller
@RequestMapping(value = "/api/actionRuleSurveySmsTemplates")
public class ActionRuleSurveySmsTemplateEndpoint {
  private final ActionRuleSurveySmsTemplateRepository actionRuleSurveySmsTemplateRepository;
  private final SurveyRepository surveyRepository;
  private final SmsTemplateRepository smsTemplateRepository;

  public ActionRuleSurveySmsTemplateEndpoint(
      ActionRuleSurveySmsTemplateRepository actionRuleSurveySmsTemplateRepository,
      SurveyRepository surveyRepository,
      SmsTemplateRepository smsTemplateRepository) {
    this.actionRuleSurveySmsTemplateRepository = actionRuleSurveySmsTemplateRepository;
    this.surveyRepository = surveyRepository;
    this.smsTemplateRepository = smsTemplateRepository;
  }

  @PostMapping
  public ResponseEntity<String> createActionRuleSurveySmsTemplate(
      @RequestBody AllowTemplateOnSurvey allowTemplateOnSurvey) {
    Survey survey =
        surveyRepository
            .findById(allowTemplateOnSurvey.getSurveyId())
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Survey not found"));

    SmsTemplate smsTemplate =
        smsTemplateRepository
            .findById(allowTemplateOnSurvey.getPackCode())
            .orElseThrow(
                () ->
                    new ResponseStatusException(HttpStatus.BAD_REQUEST, "SMS template not found"));

    Optional<String> errorOpt = validate(survey, Set.of(smsTemplate.getTemplate()));
    if (errorOpt.isPresent()) {
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
