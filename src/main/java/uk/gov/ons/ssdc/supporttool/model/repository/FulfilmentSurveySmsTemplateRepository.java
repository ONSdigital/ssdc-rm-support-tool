package uk.gov.ons.ssdc.supporttool.model.repository;

import java.util.UUID;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import uk.gov.ons.ssdc.common.model.entity.FulfilmentSurveySmsTemplate;

@RepositoryRestResource
public interface FulfilmentSurveySmsTemplateRepository
    extends PagingAndSortingRepository<FulfilmentSurveySmsTemplate, UUID> {}
