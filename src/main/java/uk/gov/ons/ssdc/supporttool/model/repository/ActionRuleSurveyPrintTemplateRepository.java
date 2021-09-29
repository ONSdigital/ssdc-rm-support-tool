package uk.gov.ons.ssdc.supporttool.model.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.ons.ssdc.common.model.entity.ActionRuleSurveyPrintTemplate;
import uk.gov.ons.ssdc.common.model.entity.Survey;

public interface ActionRuleSurveyPrintTemplateRepository
    extends JpaRepository<ActionRuleSurveyPrintTemplate, UUID> {
  List<ActionRuleSurveyPrintTemplate> findBySurvey(Survey survey);
}
