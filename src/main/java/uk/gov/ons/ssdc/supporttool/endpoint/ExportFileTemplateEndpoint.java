package uk.gov.ons.ssdc.supporttool.endpoint;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.ons.ssdc.common.model.entity.ExportFileTemplate;
import uk.gov.ons.ssdc.common.model.entity.UserGroupAuthorisedActivityType;
import uk.gov.ons.ssdc.supporttool.model.dto.ui.ExportFileTemplateDto;
import uk.gov.ons.ssdc.supporttool.model.repository.ExportFileTemplateRepository;
import uk.gov.ons.ssdc.supporttool.security.UserIdentity;

@RestController
@RequestMapping(value = "/api/exportFileTemplates")
public class ExportFileTemplateEndpoint {
  private final ExportFileTemplateRepository exportFileTemplateRepository;
  private final UserIdentity userIdentity;

  public ExportFileTemplateEndpoint(
      ExportFileTemplateRepository exportFileTemplateRepository, UserIdentity userIdentity) {
    this.exportFileTemplateRepository = exportFileTemplateRepository;
    this.userIdentity = userIdentity;
  }

  @GetMapping
  public List<ExportFileTemplateDto> getTemplates(
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {
    userIdentity.checkGlobalUserPermission(
        userEmail, UserGroupAuthorisedActivityType.LIST_EXPORT_FILE_TEMPLATES);

    return exportFileTemplateRepository.findAll().stream()
        .map(
            exportFileTemplate -> {
              ExportFileTemplateDto exportFileTemplateDto = new ExportFileTemplateDto();
              exportFileTemplateDto.setTemplate(exportFileTemplate.getTemplate());
              exportFileTemplateDto.setExportFileDestination(
                  exportFileTemplate.getExportFileDestination());
              exportFileTemplateDto.setPackCode(exportFileTemplate.getPackCode());
              return exportFileTemplateDto;
            })
        .collect(Collectors.toList());
  }

  @PostMapping
  public ResponseEntity<Void> createTemplate(
      @RequestBody ExportFileTemplateDto exportFileTemplateDto,
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {
    userIdentity.checkGlobalUserPermission(
        userEmail, UserGroupAuthorisedActivityType.CREATE_EXPORT_FILE_TEMPLATE);

    exportFileTemplateRepository
        .findAll()
        .forEach(
            exportFileTemplate -> {
              if (exportFileTemplate
                  .getPackCode()
                  .equalsIgnoreCase(exportFileTemplateDto.getPackCode())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
              }
            });

    ExportFileTemplate exportFileTemplate = new ExportFileTemplate();
    exportFileTemplate.setTemplate(exportFileTemplateDto.getTemplate());
    exportFileTemplate.setExportFileDestination(
        exportFileTemplateDto.getExportFileDestination()); // TODO: check it exists
    exportFileTemplate.setPackCode(exportFileTemplateDto.getPackCode());
    exportFileTemplate.setDescription(exportFileTemplateDto.getDescription());
    exportFileTemplate.setMetadata(exportFileTemplateDto.getMetadata());

    exportFileTemplateRepository.saveAndFlush(exportFileTemplate);

    return new ResponseEntity<>(HttpStatus.CREATED);
  }
}
