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
import uk.gov.ons.ssdc.common.model.entity.ActionRuleSurveyPrintTemplate;
import uk.gov.ons.ssdc.common.model.entity.PrintTemplate;
import uk.gov.ons.ssdc.common.model.entity.Survey;
import uk.gov.ons.ssdc.common.model.entity.UserGroupAuthorisedActivityType;
import uk.gov.ons.ssdc.supporttool.model.dto.ui.AllowTemplateOnSurvey;
import uk.gov.ons.ssdc.supporttool.model.repository.ActionRuleSurveyPrintTemplateRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.PrintTemplateRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.SurveyRepository;
import uk.gov.ons.ssdc.supporttool.security.UserIdentity;

@RestController
@RequestMapping(value = "/api/actionRuleSurveyPrintTemplates")
public class ActionRuleSurveyPrintTemplateEndpoint {
  private final ActionRuleSurveyPrintTemplateRepository actionRuleSurveyPrintTemplateRepository;
  private final SurveyRepository surveyRepository;
  private final PrintTemplateRepository printTemplateRepository;
  private final UserIdentity userIdentity;

  public ActionRuleSurveyPrintTemplateEndpoint(
      ActionRuleSurveyPrintTemplateRepository actionRuleSurveyPrintTemplateRepository,
      SurveyRepository surveyRepository,
      PrintTemplateRepository printTemplateRepository,
      UserIdentity userIdentity) {
    this.actionRuleSurveyPrintTemplateRepository = actionRuleSurveyPrintTemplateRepository;
    this.surveyRepository = surveyRepository;
    this.printTemplateRepository = printTemplateRepository;
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
        UserGroupAuthorisedActivityType.LIST_ALLOWED_PRINT_TEMPLATES_ON_ACTION_RULES);

    return actionRuleSurveyPrintTemplateRepository.findBySurvey(survey).stream()
        .map(arspt -> arspt.getPrintTemplate().getPackCode())
        .collect(Collectors.toList());
  }

  @PostMapping
  public ResponseEntity<String> createActionRuleSurveyPrintTemplate(
      @RequestBody AllowTemplateOnSurvey allowTemplateOnSurvey,
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {
    Survey survey =
        surveyRepository
            .findById(allowTemplateOnSurvey.getSurveyId())
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Survey not found"));

    userIdentity.checkUserPermission(
        userEmail, survey, UserGroupAuthorisedActivityType.ALLOW_PRINT_TEMPLATE_ON_ACTION_RULE);

    PrintTemplate printTemplate =
        printTemplateRepository
            .findById(allowTemplateOnSurvey.getPackCode())
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Print template not found"));

    Optional<String> errorOpt = validate(survey, Set.of(printTemplate.getTemplate()));
    if (errorOpt.isPresent()) {
      return new ResponseEntity<>(errorOpt.get(), HttpStatus.BAD_REQUEST);
    }

    ActionRuleSurveyPrintTemplate actionRuleSurveyPrintTemplate =
        new ActionRuleSurveyPrintTemplate();
    actionRuleSurveyPrintTemplate.setId(UUID.randomUUID());
    actionRuleSurveyPrintTemplate.setSurvey(survey);
    actionRuleSurveyPrintTemplate.setPrintTemplate(printTemplate);

    actionRuleSurveyPrintTemplateRepository.save(actionRuleSurveyPrintTemplate);

    return new ResponseEntity<>("OK", HttpStatus.CREATED);
  }
}
