package uk.gov.ons.ssdc.supporttool.model.repository;

import java.util.UUID;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import uk.gov.ons.ssdc.supporttool.model.entity.WaveOfContact;

@RepositoryRestResource
public interface WaveOfContactRepository extends PagingAndSortingRepository<WaveOfContact, UUID> {}
