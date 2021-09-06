package uk.gov.ons.ssdc.supporttool.model.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import uk.gov.ons.ssdc.common.model.entity.PrintTemplate;

@RepositoryRestResource
public interface PrintTemplateRepository
    extends PagingAndSortingRepository<PrintTemplate, String> {}
