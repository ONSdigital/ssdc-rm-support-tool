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
import uk.gov.ons.ssdc.common.model.entity.FulfilmentSurveySmsTemplate;
import uk.gov.ons.ssdc.common.model.entity.SmsTemplate;
import uk.gov.ons.ssdc.common.model.entity.Survey;
import uk.gov.ons.ssdc.common.model.entity.UserGroupAuthorisedActivityType;
import uk.gov.ons.ssdc.supporttool.model.dto.ui.AllowTemplateOnSurvey;
import uk.gov.ons.ssdc.supporttool.model.dto.ui.SmsTemplateDto;
import uk.gov.ons.ssdc.supporttool.model.repository.FulfilmentSurveySmsTemplateRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.SmsTemplateRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.SurveyRepository;
import uk.gov.ons.ssdc.supporttool.security.AuthUser;
import uk.gov.ons.ssdc.supporttool.service.SurveyService;

@RestController
@RequestMapping(value = "/api/fulfilmentSurveySmsTemplates")
public class FulfilmentSurveySmsTemplateEndpoint {
  private static final Logger log =
      LoggerFactory.getLogger(FulfilmentSurveySmsTemplateEndpoint.class);
  private final FulfilmentSurveySmsTemplateRepository fulfilmentSurveySmsTemplateRepository;
  private final SurveyRepository surveyRepository;
  private final SmsTemplateRepository smsTemplateRepository;
  private final AuthUser authUser;
  private final SurveyService surveyService;

  public FulfilmentSurveySmsTemplateEndpoint(
      FulfilmentSurveySmsTemplateRepository fulfilmentSurveySmsTemplateRepository,
      SurveyRepository surveyRepository,
      SmsTemplateRepository smsTemplateRepository,
      AuthUser authUser,
      SurveyService surveyService) {
    this.fulfilmentSurveySmsTemplateRepository = fulfilmentSurveySmsTemplateRepository;
    this.surveyRepository = surveyRepository;
    this.smsTemplateRepository = smsTemplateRepository;
    this.authUser = authUser;
    this.surveyService = surveyService;
  }

  @GetMapping
  public List<SmsTemplateDto> getAllowedSmsTemplatesBySurvey(
      @RequestParam(value = "surveyId") UUID surveyId,
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {
    authUser.checkUserPermission(
        userEmail,
        surveyId,
        UserGroupAuthorisedActivityType.LIST_ALLOWED_SMS_TEMPLATES_ON_FULFILMENTS);

    Survey survey =
        surveyRepository
            .findById(surveyId)
            .orElseThrow(
                () -> {
                  log.atWarn()
                      .setMessage("Failed to get allowed sms templates, Survey not found")
                      .addKeyValue("surveyId", surveyId)
                      .addKeyValue("userEmail", userEmail)
                      .addKeyValue("httpStatus", HttpStatus.BAD_REQUEST)
                      .log();
                  return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Survey not found");
                });

    return fulfilmentSurveySmsTemplateRepository.findBySurvey(survey).stream()
        .map(fsst -> new SmsTemplateDto(fsst.getSmsTemplate()))
        .toList();
  }

  @PostMapping
  public ResponseEntity<String> createFulfilmentSurveySmsTemplate(
      @RequestBody AllowTemplateOnSurvey allowTemplateOnSurvey,
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {
    authUser.checkUserPermission(
        userEmail,
        allowTemplateOnSurvey.getSurveyId(),
        UserGroupAuthorisedActivityType.ALLOW_SMS_TEMPLATE_ON_FULFILMENT);

    Survey survey =
        surveyRepository
            .findById(allowTemplateOnSurvey.getSurveyId())
            .orElseThrow(
                () -> {
                  log.atWarn()
                      .setMessage(
                          "Failed to create fulfilment survey sms templates, survey not found")
                      .addKeyValue("surveyId", allowTemplateOnSurvey.getSurveyId())
                      .addKeyValue("userEmail", userEmail)
                      .addKeyValue("httpStatus", HttpStatus.BAD_REQUEST)
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
                          "Failed to create fulfilment survey sms templates,SMS template not found")
                      .addKeyValue("packCode", allowTemplateOnSurvey.getPackCode())
                      .addKeyValue("userEmail", userEmail)
                      .addKeyValue("httpStatus", HttpStatus.BAD_REQUEST)
                      .log();
                  return new ResponseStatusException(
                      HttpStatus.BAD_REQUEST, "SMS template not found");
                });

    if (fulfilmentSurveySmsTemplateRepository
        .existsFulfilmentSurveySmsTemplateBySmsTemplateAndSurvey(smsTemplate, survey)) {
      log.atWarn()
          .setMessage(
              "Failed to create fulfilment sms template, SMS Template already exists for survey")
          .addKeyValue("httpStatus", HttpStatus.CONFLICT)
          .addKeyValue("packCode", allowTemplateOnSurvey.getPackCode())
          .addKeyValue("userEmail", userEmail)
          .log();
      return new ResponseEntity<>("SMS Template already exists for survey", HttpStatus.CONFLICT);
    }

    Optional<String> errorOpt = validate(survey, Set.of(smsTemplate.getTemplate()));
    if (errorOpt.isPresent()) {
      return new ResponseEntity<>(errorOpt.get(), HttpStatus.BAD_REQUEST);
    }

    FulfilmentSurveySmsTemplate fulfilmentSurveySmsTemplate = new FulfilmentSurveySmsTemplate();
    fulfilmentSurveySmsTemplate.setId(UUID.randomUUID());
    fulfilmentSurveySmsTemplate.setSurvey(survey);
    fulfilmentSurveySmsTemplate.setSmsTemplate(smsTemplate);

    fulfilmentSurveySmsTemplate =
        fulfilmentSurveySmsTemplateRepository.saveAndFlush(fulfilmentSurveySmsTemplate);

    surveyService.publishSurveyUpdate(fulfilmentSurveySmsTemplate.getSurvey(), userEmail);

    return new ResponseEntity<>("OK", HttpStatus.CREATED);
  }
}
