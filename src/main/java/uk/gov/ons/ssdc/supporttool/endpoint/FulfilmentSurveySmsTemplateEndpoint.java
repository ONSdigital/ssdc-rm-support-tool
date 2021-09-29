package uk.gov.ons.ssdc.supporttool.endpoint;

import static uk.gov.ons.ssdc.supporttool.utility.AllowTemplateOnSurveyValidator.validate;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.ons.ssdc.common.model.entity.FulfilmentSurveySmsTemplate;
import uk.gov.ons.ssdc.common.model.entity.SmsTemplate;
import uk.gov.ons.ssdc.common.model.entity.Survey;
import uk.gov.ons.ssdc.supporttool.model.dto.messaging.AllowTemplateOnSurvey;
import uk.gov.ons.ssdc.supporttool.model.repository.FulfilmentSurveySmsTemplateRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.SmsTemplateRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.SurveyRepository;

@RestController
@RequestMapping(value = "/api/fulfilmentSurveySmsTemplates")
public class FulfilmentSurveySmsTemplateEndpoint {
  private final FulfilmentSurveySmsTemplateRepository fulfilmentSurveySmsTemplateRepository;
  private final SurveyRepository surveyRepository;
  private final SmsTemplateRepository smsTemplateRepository;

  public FulfilmentSurveySmsTemplateEndpoint(
      FulfilmentSurveySmsTemplateRepository fulfilmentSurveySmsTemplateRepository,
      SurveyRepository surveyRepository,
      SmsTemplateRepository smsTemplateRepository) {
    this.fulfilmentSurveySmsTemplateRepository = fulfilmentSurveySmsTemplateRepository;
    this.surveyRepository = surveyRepository;
    this.smsTemplateRepository = smsTemplateRepository;
  }

  @PostMapping
  public ResponseEntity<String> createFulfilmentSurveySmsTemplate(
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
                    new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Print template not found"));

    Optional<String> errorOpt = validate(survey, Set.of(smsTemplate.getTemplate()));
    if (errorOpt.isPresent()) {
      return new ResponseEntity<>(errorOpt.get(), HttpStatus.BAD_REQUEST);
    }

    FulfilmentSurveySmsTemplate fulfilmentSurveySmsTemplate = new FulfilmentSurveySmsTemplate();
    fulfilmentSurveySmsTemplate.setId(UUID.randomUUID());
    fulfilmentSurveySmsTemplate.setSurvey(survey);
    fulfilmentSurveySmsTemplate.setSmsTemplate(smsTemplate);

    fulfilmentSurveySmsTemplateRepository.save(fulfilmentSurveySmsTemplate);

    return new ResponseEntity<>("OK", HttpStatus.CREATED);
  }
}
