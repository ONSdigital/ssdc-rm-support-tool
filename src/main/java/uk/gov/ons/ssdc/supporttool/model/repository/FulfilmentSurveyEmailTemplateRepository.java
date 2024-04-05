package uk.gov.ons.ssdc.supporttool.model.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.ons.ssdc.common.model.entity.EmailTemplate;
import uk.gov.ons.ssdc.common.model.entity.FulfilmentSurveyEmailTemplate;
import uk.gov.ons.ssdc.common.model.entity.Survey;

public interface FulfilmentSurveyEmailTemplateRepository
    extends JpaRepository<FulfilmentSurveyEmailTemplate, UUID> {
  List<FulfilmentSurveyEmailTemplate> findBySurvey(Survey survey);

  boolean existsFulfilmentSurveyEmailTemplateByEmailTemplateAndSurvey(
      EmailTemplate emailTemplate, Survey survey);
}
