package uk.gov.ons.ssdc.supporttool.model.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import uk.gov.ons.ssdc.supporttool.model.entity.Case;

import java.util.Optional;
import java.util.UUID;

public interface CaseRepository extends PagingAndSortingRepository<Case, UUID> {
  Optional<Case> findByCaseRef(@Param("caseRef") Long caseRef);

}
