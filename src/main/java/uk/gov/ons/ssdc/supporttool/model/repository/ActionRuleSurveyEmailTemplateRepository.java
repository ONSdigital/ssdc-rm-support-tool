package uk.gov.ons.ssdc.supporttool.model.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.ons.ssdc.common.model.entity.ActionRuleSurveyEmailTemplate;
import uk.gov.ons.ssdc.common.model.entity.EmailTemplate;
import uk.gov.ons.ssdc.common.model.entity.Survey;

public interface ActionRuleSurveyEmailTemplateRepository
    extends JpaRepository<ActionRuleSurveyEmailTemplate, UUID> {
  List<ActionRuleSurveyEmailTemplate> findBySurvey(Survey survey);

  boolean existsActionRuleSurveyEmailTemplateByEmailTemplateAndSurvey(
      EmailTemplate emailTemplate, Survey survey);
}
