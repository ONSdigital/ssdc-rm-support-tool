package uk.gov.ons.ssdc.supporttool.model.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import uk.gov.ons.ssdc.common.model.entity.UacQidLink;

@RepositoryRestResource
public interface UacQidLinkRepository extends PagingAndSortingRepository<UacQidLink, UUID> {
  Optional<UacQidLink> findByQid(String qid);
}
