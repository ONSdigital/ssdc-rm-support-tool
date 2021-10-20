package uk.gov.ons.ssdc.supporttool.model.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.ons.ssdc.common.model.entity.SmsTemplate;

public interface SmsTemplateRepository extends JpaRepository<SmsTemplate, String> {
  Optional<SmsTemplate> findPrintTemplateByPackCode(String packCode);
}
