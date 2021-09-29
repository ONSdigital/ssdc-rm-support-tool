package uk.gov.ons.ssdc.supporttool.endpoint;

import java.util.List;
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
import uk.gov.ons.ssdc.common.model.entity.CollectionExercise;
import uk.gov.ons.ssdc.common.model.entity.Survey;
import uk.gov.ons.ssdc.common.model.entity.UserGroupAuthorisedActivityType;
import uk.gov.ons.ssdc.supporttool.model.dto.ui.CollectionExerciseDto;
import uk.gov.ons.ssdc.supporttool.model.repository.CollectionExerciseRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.SurveyRepository;
import uk.gov.ons.ssdc.supporttool.security.UserIdentity;

@RestController
@RequestMapping(value = "/api/collectionExercises")
public class CollectionExerciseEndpoint {
  private final CollectionExerciseRepository collectionExerciseRepository;
  private final SurveyRepository surveyRepository;
  private final UserIdentity userIdentity;

  public CollectionExerciseEndpoint(
      CollectionExerciseRepository collectionExerciseRepository,
      SurveyRepository surveyRepository,
      UserIdentity userIdentity) {
    this.collectionExerciseRepository = collectionExerciseRepository;
    this.surveyRepository = surveyRepository;
    this.userIdentity = userIdentity;
  }

  @GetMapping
  public List<CollectionExerciseDto> findCollexsBySurvey(
      @RequestParam(value = "surveyId") UUID surveyId,
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {

    Survey survey =
        surveyRepository
            .findById(surveyId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Survey not found"));

    userIdentity.checkUserPermission(
        userEmail, survey, UserGroupAuthorisedActivityType.LIST_COLLECTION_EXERCISES);

    return collectionExerciseRepository.findBySurvey(survey).stream()
        .map(
            collex -> {
              CollectionExerciseDto collectionExerciseDto = new CollectionExerciseDto();
              collectionExerciseDto.setId(collex.getId());
              collectionExerciseDto.setSurveyId(surveyId);
              collectionExerciseDto.setName(collex.getName());
              return collectionExerciseDto;
            })
        .collect(Collectors.toList());
  }

  @PostMapping
  public ResponseEntity<UUID> createCollectionExercises(
      @RequestBody CollectionExerciseDto collectionExerciseDto,
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {

    Survey survey =
        surveyRepository
            .findById(collectionExerciseDto.getSurveyId())
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Survey not found"));

    userIdentity.checkUserPermission(
        userEmail, survey, UserGroupAuthorisedActivityType.CREATE_COLLECTION_EXERCISE);

    CollectionExercise collectionExercise = new CollectionExercise();
    collectionExercise.setId(UUID.randomUUID());
    collectionExercise.setName(collectionExerciseDto.getName());
    collectionExercise.setSurvey(survey);

    collectionExercise = collectionExerciseRepository.saveAndFlush(collectionExercise);
    return new ResponseEntity<>(collectionExercise.getId(), HttpStatus.CREATED);
  }
}
