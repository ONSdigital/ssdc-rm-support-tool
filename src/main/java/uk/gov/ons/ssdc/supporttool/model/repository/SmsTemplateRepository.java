package uk.gov.ons.ssdc.supporttool.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import uk.gov.ons.ssdc.common.model.entity.SmsTemplate;

public interface SmsTemplateRepository extends JpaRepository<SmsTemplate, String> {}
