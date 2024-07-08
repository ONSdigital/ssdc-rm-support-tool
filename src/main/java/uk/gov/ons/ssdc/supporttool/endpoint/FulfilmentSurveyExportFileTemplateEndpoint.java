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
import uk.gov.ons.ssdc.common.model.entity.ExportFileTemplate;
import uk.gov.ons.ssdc.common.model.entity.FulfilmentSurveyExportFileTemplate;
import uk.gov.ons.ssdc.common.model.entity.Survey;
import uk.gov.ons.ssdc.common.model.entity.UserGroupAuthorisedActivityType;
import uk.gov.ons.ssdc.supporttool.model.dto.ui.AllowTemplateOnSurvey;
import uk.gov.ons.ssdc.supporttool.model.dto.ui.ExportFileTemplateDto;
import uk.gov.ons.ssdc.supporttool.model.repository.ExportFileTemplateRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.FulfilmentSurveyExportFileTemplateRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.SurveyRepository;
import uk.gov.ons.ssdc.supporttool.security.AuthUser;
import uk.gov.ons.ssdc.supporttool.service.SurveyService;

@RestController
@RequestMapping(value = "/api/fulfilmentSurveyExportFileTemplates")
public class FulfilmentSurveyExportFileTemplateEndpoint {
  private static final Logger log =
      LoggerFactory.getLogger(FulfilmentSurveyExportFileTemplateEndpoint.class);
  private final FulfilmentSurveyExportFileTemplateRepository
      fulfilmentSurveyExportFileTemplateRepository;
  private final SurveyRepository surveyRepository;
  private final ExportFileTemplateRepository exportFileTemplateRepository;
  private final AuthUser authUser;
  private final SurveyService surveyService;

  public FulfilmentSurveyExportFileTemplateEndpoint(
      FulfilmentSurveyExportFileTemplateRepository fulfilmentSurveyExportFileTemplateRepository,
      SurveyRepository surveyRepository,
      ExportFileTemplateRepository exportFileTemplateRepository,
      AuthUser authUser,
      SurveyService surveyService) {
    this.fulfilmentSurveyExportFileTemplateRepository =
        fulfilmentSurveyExportFileTemplateRepository;
    this.surveyRepository = surveyRepository;
    this.exportFileTemplateRepository = exportFileTemplateRepository;
    this.authUser = authUser;
    this.surveyService = surveyService;
  }

  @GetMapping
  public List<ExportFileTemplateDto> getAllowedExportFileTemplatesBySurvey(
      @RequestParam(value = "surveyId") UUID surveyId,
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {

    authUser.checkUserPermission(
        userEmail,
        surveyId,
        UserGroupAuthorisedActivityType.LIST_ALLOWED_EXPORT_FILE_TEMPLATES_ON_FULFILMENTS);

    Survey survey =
        surveyRepository
            .findById(surveyId)
            .orElseThrow(
                () -> {
                  log.atWarn()
                      .setMessage(
                          "Failed to get allowed fulfilment export file templates, survey not found")
                      .addKeyValue("surveyId", surveyId)
                      .addKeyValue("userEmail", userEmail)
                      .addKeyValue("httpStatus", HttpStatus.BAD_REQUEST)
                      .log();
                  return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Survey not found");
                });

    return fulfilmentSurveyExportFileTemplateRepository.findBySurvey(survey).stream()
        .map(fspt -> new ExportFileTemplateDto(fspt.getExportFileTemplate()))
        .toList();
  }

  @PostMapping
  public ResponseEntity<String> createFulfilmentSurveyPrintTemplate(
      @RequestBody AllowTemplateOnSurvey allowTemplateOnSurvey,
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {

    authUser.checkUserPermission(
        userEmail,
        allowTemplateOnSurvey.getSurveyId(),
        UserGroupAuthorisedActivityType.ALLOW_EXPORT_FILE_TEMPLATE_ON_FULFILMENT);

    Survey survey =
        surveyRepository
            .findById(allowTemplateOnSurvey.getSurveyId())
            .orElseThrow(
                () -> {
                  log.atWarn()
                      .setMessage(
                          "Failed to create fulfilment survey export file templates, survey not found")
                      .addKeyValue("surveyId", allowTemplateOnSurvey.getSurveyId())
                      .addKeyValue("userEmail", userEmail)
                      .addKeyValue("httpStatus", HttpStatus.BAD_REQUEST)
                      .log();
                  return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Survey not found");
                });

    ExportFileTemplate exportFileTemplate =
        exportFileTemplateRepository
            .findById(allowTemplateOnSurvey.getPackCode())
            .orElseThrow(
                () -> {
                  log.atWarn()
                      .setMessage(
                          "Failed to create fulfilment survey export file templates, export file template not found")
                      .addKeyValue("packCode", allowTemplateOnSurvey.getPackCode())
                      .addKeyValue("userEmail", userEmail)
                      .addKeyValue("httpStatus", HttpStatus.BAD_REQUEST)
                      .log();
                  return new ResponseStatusException(
                      HttpStatus.BAD_REQUEST, "Export file template not found");
                });

    if (fulfilmentSurveyExportFileTemplateRepository
        .existsFulfilmentSurveyExportFileTemplateByExportFileTemplateAndSurvey(
            exportFileTemplate, survey)) {
      log.atWarn()
          .setMessage(
              "Failed to create fulfilment export file template, Export File Template already exists for survey")
          .addKeyValue("httpStatus", HttpStatus.CONFLICT)
          .addKeyValue("packCode", allowTemplateOnSurvey.getPackCode())
          .addKeyValue("userEmail", userEmail)
          .log();
      return new ResponseEntity<>(
          "Export File Template already exists for survey", HttpStatus.CONFLICT);
    }

    Optional<String> errorOpt = validate(survey, Set.of(exportFileTemplate.getTemplate()));
    if (errorOpt.isPresent()) {
      return new ResponseEntity<>(errorOpt.get(), HttpStatus.BAD_REQUEST);
    }

    FulfilmentSurveyExportFileTemplate fulfilmentSurveyExportFileTemplate =
        new FulfilmentSurveyExportFileTemplate();
    fulfilmentSurveyExportFileTemplate.setId(UUID.randomUUID());
    fulfilmentSurveyExportFileTemplate.setSurvey(survey);
    fulfilmentSurveyExportFileTemplate.setExportFileTemplate(exportFileTemplate);

    fulfilmentSurveyExportFileTemplate =
        fulfilmentSurveyExportFileTemplateRepository.saveAndFlush(
            fulfilmentSurveyExportFileTemplate);

    surveyService.publishSurveyUpdate(fulfilmentSurveyExportFileTemplate.getSurvey(), userEmail);

    return new ResponseEntity<>("OK", HttpStatus.CREATED);
  }
}
