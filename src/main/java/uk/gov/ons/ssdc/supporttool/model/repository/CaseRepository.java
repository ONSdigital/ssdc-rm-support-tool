package uk.gov.ons.ssdc.supporttool.model.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.gov.ons.ssdc.common.model.entity.Case;

public interface CaseRepository extends JpaRepository<Case, UUID> {
  Optional<Case> findByCaseRef(@Param("caseRef") Long caseRef);

  @Query(
      value =
          "SELECT * FROM casev3.cases WHERE collection_exercise_id = :collexId AND UPPER(REPLACE(sample ->> :key, ' ', '')) LIKE CONCAT('%', UPPER(REPLACE(:value, ' ', '')), '%')",
      nativeQuery = true)
  Iterable<Case> findBySampleContains(
      @Param("collexId") UUID collexId, @Param("key") String key, @Param("value") String value);
}
