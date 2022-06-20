package uk.gov.ons.ssdc.supporttool.endpoint;

import static uk.gov.ons.ssdc.supporttool.utility.AllowTemplateOnSurveyValidator.validate;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
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
import uk.gov.ons.ssdc.supporttool.model.dto.ui.EmailTemplateDto;
import uk.gov.ons.ssdc.supporttool.model.repository.EmailTemplateRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.FulfilmentSurveyEmailTemplateRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.SurveyRepository;
import uk.gov.ons.ssdc.supporttool.security.UserIdentity;
import uk.gov.ons.ssdc.supporttool.service.SurveyService;

@RestController
@RequestMapping(value = "/api/fulfilmentSurveyEmailTemplates")
public class FulfilmentSurveyEmailTemplateEndpoint {
  private static final Logger log =
      LoggerFactory.getLogger(FulfilmentSurveyEmailTemplateEndpoint.class);

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
  public List<EmailTemplateDto> getAllowedEmailTemplatesBySurvey(
      @RequestParam(value = "surveyId") UUID surveyId,
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {
    Survey survey =
        surveyRepository
            .findById(surveyId)
            .orElseThrow(
                () -> {
                  log.warn("Survey not found {}", HttpStatus.BAD_REQUEST);
                  return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Survey not found");
                });

    userIdentity.checkUserPermission(
        userEmail,
        survey,
        UserGroupAuthorisedActivityType.LIST_ALLOWED_EMAIL_TEMPLATES_ON_FULFILMENTS);

    return fulfilmentSurveyEmailTemplateRepository.findBySurvey(survey).stream()
        .map(fset -> new EmailTemplateDto(fset.getEmailTemplate()))
        .toList();
  }

  @PostMapping
  public ResponseEntity<String> createFulfilmentSurveyEmailTemplate(
      @RequestBody AllowTemplateOnSurvey allowTemplateOnSurvey,
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {
    Survey survey =
        surveyRepository
            .findById(allowTemplateOnSurvey.getSurveyId())
            .orElseThrow(
                () -> {
                  log.warn("Survey not found {}", HttpStatus.BAD_REQUEST);
                  return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Survey not found");
                });

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
