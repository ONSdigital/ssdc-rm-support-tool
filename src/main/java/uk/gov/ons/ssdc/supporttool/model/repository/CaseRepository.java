package uk.gov.ons.ssdc.supporttool.model.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import uk.gov.ons.ssdc.supporttool.model.entity.Case;

import java.util.Optional;
import java.util.UUID;

@RepositoryRestResource()
public interface CaseRepository extends PagingAndSortingRepository<Case, UUID> {
  Optional<Case> findByCaseRef(@Param("caseRef") Long caseRef);

  @Query(
          value =
                  "SELECT * FROM casev3.cases WHERE collection_exercise_id = :collexId AND UPPER(REPLACE(sample ->> :key, ' ', '')) LIKE CONCAT('%', UPPER(REPLACE(:value, ' ', '')), '%')",
          nativeQuery = true)
  Iterable<Case> findBySampleContains(
          @Param("collexId") UUID collexId, @Param("key") String key, @Param("value") String value);
}
