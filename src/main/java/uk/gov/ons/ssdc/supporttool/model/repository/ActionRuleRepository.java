package uk.gov.ons.ssdc.supporttool.model.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.ons.ssdc.common.model.entity.ActionRule;
import uk.gov.ons.ssdc.common.model.entity.CollectionExercise;

public interface ActionRuleRepository extends JpaRepository<ActionRule, UUID> {
  List<ActionRule> findByCollectionExercise(CollectionExercise collectionExercise);
}
