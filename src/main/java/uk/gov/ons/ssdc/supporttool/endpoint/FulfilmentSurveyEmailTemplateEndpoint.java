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
import uk.gov.ons.ssdc.common.model.entity.EmailTemplate;
import uk.gov.ons.ssdc.common.model.entity.FulfilmentSurveyEmailTemplate;
import uk.gov.ons.ssdc.common.model.entity.Survey;
import uk.gov.ons.ssdc.common.model.entity.UserGroupAuthorisedActivityType;
import uk.gov.ons.ssdc.supporttool.model.dto.ui.AllowTemplateOnSurvey;
import uk.gov.ons.ssdc.supporttool.model.repository.EmailTemplateRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.FulfilmentSurveyEmailTemplateRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.SurveyRepository;
import uk.gov.ons.ssdc.supporttool.security.UserIdentity;
import uk.gov.ons.ssdc.supporttool.service.SurveyService;

@RestController
@RequestMapping(value = "/api/fulfilmentSurveyEmailTemplates")
public class FulfilmentSurveyEmailTemplateEndpoint {
  private final FulfilmentSurveyEmailTemplateRepository fulfilmentSurveyEmailTemplateRepository;
  private final SurveyRepository surveyRepository;
  private final EmailTemplateRepository emailTemplateRepository;
  private final UserIdentity userIdentity;
  private final SurveyService surveyService;

  public FulfilmentSurveyEmailTemplateEndpoint(
      FulfilmentSurveyEmailTemplateRepository fulfilmentSurveyEmailTemplateRepository,
      SurveyRepository surveyRepository,
      EmailTemplateRepository emailTemplateRepository,
      UserIdentity userIdentity,
      SurveyService surveyService) {
    this.fulfilmentSurveyEmailTemplateRepository = fulfilmentSurveyEmailTemplateRepository;
    this.surveyRepository = surveyRepository;
    this.emailTemplateRepository = emailTemplateRepository;
    this.userIdentity = userIdentity;
    this.surveyService = surveyService;
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
        UserGroupAuthorisedActivityType.LIST_ALLOWED_EMAIL_TEMPLATES_ON_FULFILMENTS);

    return fulfilmentSurveyEmailTemplateRepository.findBySurvey(survey).stream()
        .map(fsst -> fsst.getEmailTemplate().getPackCode())
        .collect(Collectors.toList());
  }

  @PostMapping
  public ResponseEntity<String> createFulfilmentSurveyEmailTemplate(
      @RequestBody AllowTemplateOnSurvey allowTemplateOnSurvey,
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {
    Survey survey =
        surveyRepository
            .findById(allowTemplateOnSurvey.getSurveyId())
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Survey not found"));

    userIdentity.checkUserPermission(
        userEmail, survey, UserGroupAuthorisedActivityType.ALLOW_EMAIL_TEMPLATE_ON_FULFILMENT);

    EmailTemplate emailTemplate =
        emailTemplateRepository
            .findById(allowTemplateOnSurvey.getPackCode())
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Email template not found"));

    Optional<String> errorOpt = validate(survey, Set.of(emailTemplate.getTemplate()));
    if (errorOpt.isPresent()) {
      return new ResponseEntity<>(errorOpt.get(), HttpStatus.BAD_REQUEST);
    }

    FulfilmentSurveyEmailTemplate fulfilmentSurveyEmailTemplate =
        new FulfilmentSurveyEmailTemplate();
    fulfilmentSurveyEmailTemplate.setId(UUID.randomUUID());
    fulfilmentSurveyEmailTemplate.setSurvey(survey);
    fulfilmentSurveyEmailTemplate.setEmailTemplate(emailTemplate);

    fulfilmentSurveyEmailTemplate =
        fulfilmentSurveyEmailTemplateRepository.saveAndFlush(fulfilmentSurveyEmailTemplate);

    surveyService.publishSurveyUpdate(fulfilmentSurveyEmailTemplate.getSurvey(), userEmail);

    return new ResponseEntity<>("OK", HttpStatus.CREATED);
  }
}
