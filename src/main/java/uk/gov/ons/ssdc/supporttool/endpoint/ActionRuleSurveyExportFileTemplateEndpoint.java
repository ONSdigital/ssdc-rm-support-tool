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
import uk.gov.ons.ssdc.common.model.entity.ActionRuleSurveyExportFileTemplate;
import uk.gov.ons.ssdc.common.model.entity.ExportFileTemplate;
import uk.gov.ons.ssdc.common.model.entity.Survey;
import uk.gov.ons.ssdc.common.model.entity.UserGroupAuthorisedActivityType;
import uk.gov.ons.ssdc.supporttool.model.dto.ui.AllowTemplateOnSurvey;
import uk.gov.ons.ssdc.supporttool.model.repository.ActionRuleSurveyExportFileTemplateRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.ExportFileTemplateRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.SurveyRepository;
import uk.gov.ons.ssdc.supporttool.security.AuthUser;

@RestController
@RequestMapping(value = "/api/actionRuleSurveyExportFileTemplates")
public class ActionRuleSurveyExportFileTemplateEndpoint {
  private static final Logger log =
      LoggerFactory.getLogger(ActionRuleSurveyExportFileTemplateEndpoint.class);
  private final ActionRuleSurveyExportFileTemplateRepository
      actionRuleSurveyExportFileTemplateRepository;
  private final SurveyRepository surveyRepository;
  private final ExportFileTemplateRepository exportFileTemplateRepository;
  private final AuthUser authUser;

  public ActionRuleSurveyExportFileTemplateEndpoint(
      ActionRuleSurveyExportFileTemplateRepository actionRuleSurveyExportFileTemplateRepository,
      SurveyRepository surveyRepository,
      ExportFileTemplateRepository exportFileTemplateRepository,
      AuthUser authUser) {
    this.actionRuleSurveyExportFileTemplateRepository =
        actionRuleSurveyExportFileTemplateRepository;
    this.surveyRepository = surveyRepository;
    this.exportFileTemplateRepository = exportFileTemplateRepository;
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
        UserGroupAuthorisedActivityType.LIST_ALLOWED_EXPORT_FILE_TEMPLATES_ON_ACTION_RULES);

    Survey survey =
        surveyRepository
            .findById(surveyId)
            .orElseThrow(
                () -> {
                  log.atWarn()
                      .setMessage("Failed to get allowed pack codes, Survey not found")
                      .addKeyValue("httpStatus", HttpStatus.BAD_REQUEST)
                      .addKeyValue("userEmail", userEmail)
                      .addKeyValue("surveyId", surveyId)
                      .log();
                  return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Survey not found");
                });

    return actionRuleSurveyExportFileTemplateRepository.findBySurvey(survey).stream()
        .map(arspt -> arspt.getExportFileTemplate().getPackCode())
        .collect(Collectors.toList());
  }

  @PostMapping
  public ResponseEntity<String> createActionRuleSurveyExportFileTemplate(
      @RequestBody AllowTemplateOnSurvey allowTemplateOnSurvey,
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {

    authUser.checkUserPermission(
        userEmail,
        allowTemplateOnSurvey.getSurveyId(),
        UserGroupAuthorisedActivityType.ALLOW_EXPORT_FILE_TEMPLATE_ON_ACTION_RULE);

    Survey survey =
        surveyRepository
            .findById(allowTemplateOnSurvey.getSurveyId())
            .orElseThrow(
                () -> {
                  log.atWarn()
                      .setMessage(
                          "Failed to create action rule survey export file template, survey not found")
                      .addKeyValue("httpStatus", HttpStatus.BAD_REQUEST)
                      .addKeyValue("userEmail", userEmail)
                      .addKeyValue("surveyId", allowTemplateOnSurvey.getSurveyId())
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
                          "Failed to create action rule survey export file template, export File template not found")
                      .addKeyValue("httpStatus", HttpStatus.BAD_REQUEST)
                      .addKeyValue("userEmail", userEmail)
                      .addKeyValue("packCode", allowTemplateOnSurvey.getPackCode())
                      .log();
                  return new ResponseStatusException(
                      HttpStatus.BAD_REQUEST, "Export File template not found");
                });

    if (actionRuleSurveyExportFileTemplateRepository
        .existsActionRuleSurveyExportFileTemplateByExportFileTemplateAndSurvey(
            exportFileTemplate, survey)) {
      log.atWarn()
          .setMessage(
              "Failed to create action rule Export File template, Export File Template already exists for survey")
          .addKeyValue("httpStatus", HttpStatus.CONFLICT)
          .addKeyValue("packCode", allowTemplateOnSurvey.getPackCode())
          .addKeyValue("userEmail", userEmail)
          .log();
      return new ResponseEntity<>(
          "Export File Template already exists for survey", HttpStatus.CONFLICT);
    }

    Optional<String> errorOpt = validate(survey, Set.of(exportFileTemplate.getTemplate()));
    if (errorOpt.isPresent()) {
      log.atWarn()
          .setMessage(
              "Failed to create action rule survey export file template, there were errors validating the export file template")
          .addKeyValue("httpStatus", HttpStatus.BAD_REQUEST)
          .addKeyValue("userEmail", userEmail)
          .addKeyValue("validationErrors", errorOpt.get())
          .log();
      return new ResponseEntity<>(errorOpt.get(), HttpStatus.BAD_REQUEST);
    }

    ActionRuleSurveyExportFileTemplate actionRuleSurveyExportFileTemplate =
        new ActionRuleSurveyExportFileTemplate();
    actionRuleSurveyExportFileTemplate.setId(UUID.randomUUID());
    actionRuleSurveyExportFileTemplate.setSurvey(survey);
    actionRuleSurveyExportFileTemplate.setExportFileTemplate(exportFileTemplate);

    actionRuleSurveyExportFileTemplateRepository.save(actionRuleSurveyExportFileTemplate);

    return new ResponseEntity<>("OK", HttpStatus.CREATED);
  }
}
