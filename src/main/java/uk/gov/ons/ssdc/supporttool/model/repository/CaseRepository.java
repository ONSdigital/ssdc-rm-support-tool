package uk.gov.ons.ssdc.supporttool.model.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import uk.gov.ons.ssdc.supporttool.model.entity.Case;

@RepositoryRestResource
public interface CaseRepository extends PagingAndSortingRepository<Case, UUID> {
  Optional<Case> findByCaseRef(Long caseRef);

  @Query(
      value =
          "SELECT * FROM casev3.cases WHERE collection_exercise_id = :collexId AND UPPER(REPLACE(sample ->> :key, ' ', '')) LIKE CONCAT('%', UPPER(REPLACE(:value, ' ', '')), '%')",
      nativeQuery = true)
  Iterable<Case> findBySampleContains(UUID collexId, String key, String value);
}
