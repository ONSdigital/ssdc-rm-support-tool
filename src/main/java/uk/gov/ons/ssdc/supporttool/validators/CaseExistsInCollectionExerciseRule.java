package uk.gov.ons.ssdc.supporttool.validators;

import java.util.Optional;
import java.util.UUID;
import org.springframework.context.ApplicationContext;
import uk.gov.ons.ssdc.common.model.entity.CollectionExercise;
import uk.gov.ons.ssdc.common.validation.Rule;
import uk.gov.ons.ssdc.supporttool.config.ApplicationContextProvider;
import uk.gov.ons.ssdc.supporttool.model.repository.CaseRepository;

public class CaseExistsInCollectionExerciseRule implements Rule {
  private final CollectionExercise collectionExercise;
  private static CaseRepository caseRepository = null;

  public CaseExistsInCollectionExerciseRule(CollectionExercise collectionExercise) {
    this.collectionExercise = collectionExercise;
  }

  @Override
  public Optional<String> checkValidity(String data) {

    if (!getCaseRepository()
        .existsByIdAndCollectionExercise(UUID.fromString(data), collectionExercise)) {
      return Optional.of(String.format("Case Id %s does not exist in collection exercise %s",
              data, collectionExercise.getName()));
    }

    return Optional.empty();
  }

  private CaseRepository getCaseRepository() {
    if (caseRepository == null) {
      ApplicationContext applicationContext = ApplicationContextProvider.getApplicationContext();
      caseRepository = applicationContext.getBean(CaseRepository.class);
    }

    return caseRepository;
  }
}
