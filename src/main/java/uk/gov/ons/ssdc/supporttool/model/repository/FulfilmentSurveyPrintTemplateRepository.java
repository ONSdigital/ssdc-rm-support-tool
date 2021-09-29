package uk.gov.ons.ssdc.supporttool.model.repository;

import java.util.UUID;
import org.springframework.data.repository.PagingAndSortingRepository;
import uk.gov.ons.ssdc.common.model.entity.FulfilmentSurveyPrintTemplate;

public interface FulfilmentSurveyPrintTemplateRepository
    extends PagingAndSortingRepository<FulfilmentSurveyPrintTemplate, UUID> {}
