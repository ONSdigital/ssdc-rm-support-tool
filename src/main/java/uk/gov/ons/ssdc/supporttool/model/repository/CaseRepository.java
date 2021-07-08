package uk.gov.ons.ssdc.supporttool.model.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import uk.gov.ons.ssdc.supporttool.model.entity.Case;

public interface CaseRepository extends PagingAndSortingRepository<Case, UUID> {
  Optional<Case> findByCaseRef(@Param("caseRef") Long caseRef);
}
