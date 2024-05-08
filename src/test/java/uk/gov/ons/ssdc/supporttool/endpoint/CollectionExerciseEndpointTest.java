package uk.gov.ons.ssdc.supporttool.endpoint;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.ons.ssdc.common.model.entity.CollectionInstrumentSelectionRule;
import uk.gov.ons.ssdc.common.model.entity.Survey;
import uk.gov.ons.ssdc.supporttool.model.dto.ui.CollectionExerciseDto;
import uk.gov.ons.ssdc.supporttool.model.repository.SurveyRepository;
import uk.gov.ons.ssdc.supporttool.security.AuthUser;

@ExtendWith(MockitoExtension.class)
public class CollectionExerciseEndpointTest {
  @Mock SurveyRepository surveyRepository;
  @Mock AuthUser authUser; // This is required, trust me

  @InjectMocks CollectionExerciseEndpoint underTest;

  @Test
  void createCollectionExercisesRejectsEmptyListOfCollectionInstrumentSelectionRules() {
    CollectionExerciseDto collectionExerciseDto = new CollectionExerciseDto();
    collectionExerciseDto.setSurveyId(UUID.randomUUID());
    collectionExerciseDto.setCollectionInstrumentSelectionRules(
        new CollectionInstrumentSelectionRule[] {});

    when(surveyRepository.findById(any(UUID.class))).thenReturn(Optional.of(new Survey()));

    ResponseStatusException thrown =
        assertThrows(
            ResponseStatusException.class,
            () -> underTest.createCollectionExercises(collectionExerciseDto, null));

    assertThat(thrown.getReason())
        .isEqualTo("Rules must include zero priority default with null expression");
  }

  @Test
  void createCollectionExercisesRejectsUnparseableSpelExpression() {
    CollectionExerciseDto collectionExerciseDto = new CollectionExerciseDto();
    collectionExerciseDto.setSurveyId(UUID.randomUUID());
    collectionExerciseDto.setCollectionInstrumentSelectionRules(
        new CollectionInstrumentSelectionRule[] {
          new CollectionInstrumentSelectionRule(100, "this is not SPEL", "dummyUrl", null)
        });

    when(surveyRepository.findById(any(UUID.class))).thenReturn(Optional.of(new Survey()));

    ResponseStatusException thrown =
        assertThrows(
            ResponseStatusException.class,
            () -> underTest.createCollectionExercises(collectionExerciseDto, null));

    assertThat(thrown.getReason()).isEqualTo("Invalid SPEL: this is not SPEL");
  }

  @Test
  void createCollectionExercisesRejectsBlankInstrumentUrl() {
    CollectionExerciseDto collectionExerciseDto = new CollectionExerciseDto();
    collectionExerciseDto.setSurveyId(UUID.randomUUID());
    collectionExerciseDto.setCollectionInstrumentSelectionRules(
        new CollectionInstrumentSelectionRule[] {
          new CollectionInstrumentSelectionRule(100, "true == false", "", null)
        });

    when(surveyRepository.findById(any(UUID.class))).thenReturn(Optional.of(new Survey()));

    ResponseStatusException thrown =
        assertThrows(
            ResponseStatusException.class,
            () -> underTest.createCollectionExercises(collectionExerciseDto, null));

    assertThat(thrown.getReason()).isEqualTo("CI URL cannot be blank");
  }

  @Test
  void createCollectionExercisesRejectsMissingDefault() {
    CollectionExerciseDto collectionExerciseDto = new CollectionExerciseDto();
    collectionExerciseDto.setSurveyId(UUID.randomUUID());
    collectionExerciseDto.setCollectionInstrumentSelectionRules(
        new CollectionInstrumentSelectionRule[] {
          new CollectionInstrumentSelectionRule(100, "true == false", "dummyUrl", null)
        });

    when(surveyRepository.findById(any(UUID.class))).thenReturn(Optional.of(new Survey()));

    ResponseStatusException thrown =
        assertThrows(
            ResponseStatusException.class,
            () -> underTest.createCollectionExercises(collectionExerciseDto, null));

    assertThat(thrown.getReason())
        .isEqualTo("Rules must include zero priority default with null expression");
  }
}
