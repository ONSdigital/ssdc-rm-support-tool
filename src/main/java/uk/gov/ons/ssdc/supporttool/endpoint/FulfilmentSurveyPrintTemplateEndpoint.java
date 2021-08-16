package uk.gov.ons.ssdc.supporttool.endpoint;

import static uk.gov.ons.ssdc.supporttool.utility.AllowPrintTemplateOnSurveyValidator.validate;

import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.ons.ssdc.supporttool.model.dto.messaging.AllowPrintTemplateOnSurvey;
import uk.gov.ons.ssdc.supporttool.model.entity.FulfilmentSurveyPrintTemplate;
import uk.gov.ons.ssdc.supporttool.model.entity.PrintTemplate;
import uk.gov.ons.ssdc.supporttool.model.entity.Survey;
import uk.gov.ons.ssdc.supporttool.model.repository.FulfilmentSurveyPrintTemplateRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.PrintTemplateRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.SurveyRepository;

@Controller
@RequestMapping(value = "/api/fulfilmentSurveyPrintTemplates")
public class FulfilmentSurveyPrintTemplateEndpoint {
  private final FulfilmentSurveyPrintTemplateRepository fulfilmentSurveyPrintTemplateRepository;
  private final SurveyRepository surveyRepository;
  private final PrintTemplateRepository printTemplateRepository;

  public FulfilmentSurveyPrintTemplateEndpoint(
      FulfilmentSurveyPrintTemplateRepository fulfilmentSurveyPrintTemplateRepository,
      SurveyRepository surveyRepository,
      PrintTemplateRepository printTemplateRepository) {
    this.fulfilmentSurveyPrintTemplateRepository = fulfilmentSurveyPrintTemplateRepository;
    this.surveyRepository = surveyRepository;
    this.printTemplateRepository = printTemplateRepository;
  }

  @PostMapping
  public ResponseEntity<String> createFulfilmentSurveyPrintTemplate(
      @RequestBody AllowPrintTemplateOnSurvey allowPrintTemplateOnSurvey) {
    Survey survey =
        surveyRepository
            .findById(allowPrintTemplateOnSurvey.getSurveyId())
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Survey not found"));

    PrintTemplate printTemplate =
        printTemplateRepository
            .findById(allowPrintTemplateOnSurvey.getPackCode())
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Print template not found"));

    Optional<String> errorOpt = validate(survey, printTemplate);
    if (errorOpt.isPresent()) {
      return new ResponseEntity<>(errorOpt.get(), HttpStatus.BAD_REQUEST);
    }

    FulfilmentSurveyPrintTemplate fulfilmentSurveyPrintTemplate =
        new FulfilmentSurveyPrintTemplate();
    fulfilmentSurveyPrintTemplate.setId(UUID.randomUUID());
    fulfilmentSurveyPrintTemplate.setSurvey(survey);
    fulfilmentSurveyPrintTemplate.setPrintTemplate(printTemplate);

    fulfilmentSurveyPrintTemplateRepository.save(fulfilmentSurveyPrintTemplate);

    return new ResponseEntity<>("OK", HttpStatus.CREATED);
  }
}
