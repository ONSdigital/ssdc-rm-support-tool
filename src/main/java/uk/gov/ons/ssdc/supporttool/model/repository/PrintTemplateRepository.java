package uk.gov.ons.ssdc.supporttool.model.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.ons.ssdc.common.model.entity.PrintTemplate;

public interface PrintTemplateRepository extends JpaRepository<PrintTemplate, String> {
  Optional<PrintTemplate> findPrintTemplateByPackCode(String packCode);
}
