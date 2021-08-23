package uk.gov.ons.ssdc.supporttool.model.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import uk.gov.ons.ssdc.supporttool.model.entity.SmsTemplate;

@RepositoryRestResource
public interface SmsTemplateRepository extends PagingAndSortingRepository<SmsTemplate, String> {}
