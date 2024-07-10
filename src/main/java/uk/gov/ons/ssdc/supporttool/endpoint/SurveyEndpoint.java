package uk.gov.ons.ssdc.supporttool.endpoint;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.ons.ssdc.common.model.entity.Survey;
import uk.gov.ons.ssdc.common.model.entity.UserGroupAuthorisedActivityType;
import uk.gov.ons.ssdc.supporttool.model.dto.ui.SurveyDto;
import uk.gov.ons.ssdc.supporttool.model.repository.SurveyRepository;
import uk.gov.ons.ssdc.supporttool.security.AuthUser;
import uk.gov.ons.ssdc.supporttool.service.SurveyService;

@RestController
@RequestMapping(value = "/api/surveys")
public class SurveyEndpoint {
  private static final Logger log = LoggerFactory.getLogger(SurveyEndpoint.class);
  private final SurveyRepository surveyRepository;
  private final AuthUser authUser;
  private final SurveyService surveyService;

  public SurveyEndpoint(
      SurveyRepository surveyRepository, AuthUser authUser, SurveyService surveyService) {
    this.surveyRepository = surveyRepository;
    this.authUser = authUser;
    this.surveyService = surveyService;
  }

  @GetMapping
  public List<SurveyDto> getSurveys(
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {

    authUser.checkGlobalUserPermission(userEmail, UserGroupAuthorisedActivityType.LIST_SURVEYS);

    return surveyRepository.findAll().stream().map(this::mapToDto).collect(Collectors.toList());
  }

  private SurveyDto mapToDto(Survey survey) {
    SurveyDto surveyDto = new SurveyDto();
    surveyDto.setId(survey.getId());
    surveyDto.setName(survey.getName());
    surveyDto.setSampleSeparator(survey.getSampleSeparator());
    surveyDto.setSampleValidationRules(survey.getSampleValidationRules());
    surveyDto.setSampleWithHeaderRow(survey.isSampleWithHeaderRow());
    surveyDto.setSampleDefinitionUrl(survey.getSampleDefinitionUrl());
    surveyDto.setMetadata(survey.getMetadata());
    return surveyDto;
  }

  @GetMapping("/{surveyId}")
  public SurveyDto getSurvey(
      @PathVariable(value = "surveyId") UUID surveyId,
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {

    authUser.checkUserPermission(userEmail, surveyId, UserGroupAuthorisedActivityType.VIEW_SURVEY);

    Survey survey =
        surveyRepository
            .findById(surveyId)
            .orElseThrow(
                () -> {
                  log.atWarn()
                      .setMessage("Failed to get survey, survey not found")
                      .addKeyValue("surveyId", surveyId)
                      .addKeyValue("userEmail", userEmail)
                      .addKeyValue("httpStatus", HttpStatus.BAD_REQUEST)
                      .log();
                  return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Survey not found");
                });

    return mapToDto(survey);
  }

  @PostMapping
  @Transactional
  public ResponseEntity<UUID> createSurvey(
      @RequestBody SurveyDto surveyDto,
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {
    authUser.checkGlobalUserPermission(userEmail, UserGroupAuthorisedActivityType.CREATE_SURVEY);

    Survey survey = new Survey();
    survey.setId(UUID.randomUUID());
    survey.setName(surveyDto.getName());
    survey.setSampleSeparator(surveyDto.getSampleSeparator());
    survey.setSampleValidationRules(surveyDto.getSampleValidationRules());
    survey.setSampleWithHeaderRow(surveyDto.isSampleWithHeaderRow());
    survey.setSampleDefinitionUrl(surveyDto.getSampleDefinitionUrl());
    survey.setMetadata(surveyDto.getMetadata());
    survey = surveyRepository.saveAndFlush(survey);

    surveyService.publishSurveyUpdate(survey, userEmail);

    return new ResponseEntity<>(survey.getId(), HttpStatus.CREATED);
  }
}
