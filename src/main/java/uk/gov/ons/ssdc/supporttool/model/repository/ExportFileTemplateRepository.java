package uk.gov.ons.ssdc.supporttool.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.ons.ssdc.common.model.entity.ExportFileTemplate;

public interface ExportFileTemplateRepository extends JpaRepository<ExportFileTemplate, String> {}
