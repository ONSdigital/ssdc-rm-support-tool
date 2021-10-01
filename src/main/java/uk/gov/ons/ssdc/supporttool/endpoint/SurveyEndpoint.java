package uk.gov.ons.ssdc.supporttool.endpoint;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import uk.gov.ons.ssdc.supporttool.security.UserIdentity;

@RestController
@RequestMapping(value = "/api/surveys")
public class SurveyEndpoint {
  private final SurveyRepository surveyRepository;
  private final UserIdentity userIdentity;

  public SurveyEndpoint(SurveyRepository surveyRepository, UserIdentity userIdentity) {
    this.surveyRepository = surveyRepository;
    this.userIdentity = userIdentity;
  }

  @GetMapping
  public List<SurveyDto> getSurveys(
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {
    userIdentity.checkGlobalUserPermission(userEmail, UserGroupAuthorisedActivityType.LIST_SURVEYS);

    return surveyRepository.findAll().stream().map(this::mapToDto).collect(Collectors.toList());
  }

  private SurveyDto mapToDto(Survey survey) {
    SurveyDto surveyDto = new SurveyDto();
    surveyDto.setId(survey.getId());
    surveyDto.setName(survey.getName());
    surveyDto.setSampleSeparator(survey.getSampleSeparator());
    surveyDto.setSampleValidationRules(survey.getSampleValidationRules());
    surveyDto.setSampleWithHeaderRow(survey.isSampleWithHeaderRow());
    return surveyDto;
  }

  @GetMapping("/{surveyId}")
  public SurveyDto getSurvey(
      @PathVariable(value = "surveyId") UUID surveyId,
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {
    Survey survey =
        surveyRepository
            .findById(surveyId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Survey not found"));

    userIdentity.checkUserPermission(
        userEmail, survey, UserGroupAuthorisedActivityType.VIEW_SURVEY);

    return mapToDto(survey);
  }

  @PostMapping
  public ResponseEntity<UUID> createSurvey(
      @RequestBody SurveyDto surveyDto,
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {
    userIdentity.checkGlobalUserPermission(
        userEmail, UserGroupAuthorisedActivityType.CREATE_SURVEY);

    Survey survey = new Survey();
    survey.setId(UUID.randomUUID());
    survey.setName(surveyDto.getName());
    survey.setSampleSeparator(surveyDto.getSampleSeparator());
    survey.setSampleValidationRules(surveyDto.getSampleValidationRules());
    survey.setSampleWithHeaderRow(surveyDto.isSampleWithHeaderRow());
    surveyRepository.saveAndFlush(survey);

    return new ResponseEntity<>(survey.getId(), HttpStatus.CREATED);
  }
}
