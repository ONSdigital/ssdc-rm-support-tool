package uk.gov.ons.ssdc.supporttool.model.dto.ui;

import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.ons.ssdc.common.model.entity.ExportFileTemplate;

@Data
@NoArgsConstructor
public class ExportFileTemplateDto {
  private String packCode;
  private String[] template;
  private String exportFileDestination;
  private String description;
  private Object metadata;

  public ExportFileTemplateDto(ExportFileTemplate exportFileTemplate) {
    packCode = exportFileTemplate.getPackCode();
    template = exportFileTemplate.getTemplate();
    exportFileDestination = exportFileTemplate.getExportFileDestination();
    description = exportFileTemplate.getDescription();
    metadata = exportFileTemplate.getMetadata();
  }
}
