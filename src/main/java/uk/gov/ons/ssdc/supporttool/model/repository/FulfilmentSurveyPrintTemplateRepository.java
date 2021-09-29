package uk.gov.ons.ssdc.supporttool.model.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.ons.ssdc.common.model.entity.FulfilmentSurveyPrintTemplate;
import uk.gov.ons.ssdc.common.model.entity.Survey;

public interface FulfilmentSurveyPrintTemplateRepository
    extends JpaRepository<FulfilmentSurveyPrintTemplate, UUID> {
  List<FulfilmentSurveyPrintTemplate> findBySurvey(Survey survey);
}
