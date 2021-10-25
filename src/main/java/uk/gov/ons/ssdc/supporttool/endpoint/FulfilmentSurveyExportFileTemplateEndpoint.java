package uk.gov.ons.ssdc.supporttool.endpoint;

import static uk.gov.ons.ssdc.supporttool.utility.AllowTemplateOnSurveyValidator.validate;

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
import uk.gov.ons.ssdc.common.model.entity.ExportFileTemplate;
import uk.gov.ons.ssdc.common.model.entity.FulfilmentSurveyExportFileTemplate;
import uk.gov.ons.ssdc.common.model.entity.Survey;
import uk.gov.ons.ssdc.common.model.entity.UserGroupAuthorisedActivityType;
import uk.gov.ons.ssdc.supporttool.model.dto.ui.AllowTemplateOnSurvey;
import uk.gov.ons.ssdc.supporttool.model.repository.ExportFileTemplateRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.FulfilmentSurveyExportFileTemplateRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.SurveyRepository;
import uk.gov.ons.ssdc.supporttool.security.UserIdentity;

@RestController
@RequestMapping(value = "/api/fulfilmentSurveyExportFileTemplates")
public class FulfilmentSurveyExportFileTemplateEndpoint {
  private final FulfilmentSurveyExportFileTemplateRepository
      fulfilmentSurveyExportFileTemplateRepository;
  private final SurveyRepository surveyRepository;
  private final ExportFileTemplateRepository exportFileTemplateRepository;
  private final UserIdentity userIdentity;

  public FulfilmentSurveyExportFileTemplateEndpoint(
      FulfilmentSurveyExportFileTemplateRepository fulfilmentSurveyExportFileTemplateRepository,
      SurveyRepository surveyRepository,
      ExportFileTemplateRepository exportFileTemplateRepository,
      UserIdentity userIdentity) {
    this.fulfilmentSurveyExportFileTemplateRepository =
        fulfilmentSurveyExportFileTemplateRepository;
    this.surveyRepository = surveyRepository;
    this.exportFileTemplateRepository = exportFileTemplateRepository;
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
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Survey not found"));

    userIdentity.checkUserPermission(
        userEmail,
        survey,
        UserGroupAuthorisedActivityType.LIST_ALLOWED_EXPORT_FILE_TEMPLATES_ON_FULFILMENTS);

    return fulfilmentSurveyExportFileTemplateRepository.findBySurvey(survey).stream()
        .map(fspt -> fspt.getExportFileTemplate().getPackCode())
        .collect(Collectors.toList());
  }

  @PostMapping
  public ResponseEntity<String> createFulfilmentSurveyPrintTemplate(
      @RequestBody AllowTemplateOnSurvey allowTemplateOnSurvey,
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {
    Survey survey =
        surveyRepository
            .findById(allowTemplateOnSurvey.getSurveyId())
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Survey not found"));

    userIdentity.checkUserPermission(
        userEmail,
        survey,
        UserGroupAuthorisedActivityType.ALLOW_EXPORT_FILE_TEMPLATE_ON_FULFILMENT);

    ExportFileTemplate exportFileTemplate =
        exportFileTemplateRepository
            .findById(allowTemplateOnSurvey.getPackCode())
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Export file template not found"));

    Optional<String> errorOpt = validate(survey, Set.of(exportFileTemplate.getTemplate()));
    if (errorOpt.isPresent()) {
      return new ResponseEntity<>(errorOpt.get(), HttpStatus.BAD_REQUEST);
    }

    FulfilmentSurveyExportFileTemplate fulfilmentSurveyExportFileTemplate =
        new FulfilmentSurveyExportFileTemplate();
    fulfilmentSurveyExportFileTemplate.setId(UUID.randomUUID());
    fulfilmentSurveyExportFileTemplate.setSurvey(survey);
    fulfilmentSurveyExportFileTemplate.setExportFileTemplate(exportFileTemplate);

    fulfilmentSurveyExportFileTemplateRepository.save(fulfilmentSurveyExportFileTemplate);

    return new ResponseEntity<>("OK", HttpStatus.CREATED);
  }
}
