package uk.gov.ons.ssdc.supporttool.model.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.ons.ssdc.common.model.entity.CollectionExercise;
import uk.gov.ons.ssdc.common.model.entity.Survey;

public interface CollectionExerciseRepository extends JpaRepository<CollectionExercise, UUID> {
  List<CollectionExercise> findBySurvey(Survey survey);
}
