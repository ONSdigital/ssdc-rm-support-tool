package uk.gov.ons.ssdc.supporttool.model.repository;

import java.util.UUID;
import org.springframework.data.repository.PagingAndSortingRepository;
import uk.gov.ons.ssdc.common.model.entity.ActionRuleSurveyPrintTemplate;

public interface ActionRuleSurveyPrintTemplateRepository
    extends PagingAndSortingRepository<ActionRuleSurveyPrintTemplate, UUID> {}
