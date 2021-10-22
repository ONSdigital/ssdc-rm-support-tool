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
import uk.gov.ons.ssdc.common.model.entity.PrintTemplate;
import uk.gov.ons.ssdc.common.model.entity.UserGroupAuthorisedActivityType;
import uk.gov.ons.ssdc.supporttool.model.dto.ui.PrintTemplateDto;
import uk.gov.ons.ssdc.supporttool.model.repository.PrintTemplateRepository;
import uk.gov.ons.ssdc.supporttool.security.UserIdentity;

@RestController
@RequestMapping(value = "/api/printTemplates")
public class PrintTemplateEndpoint {
  private final PrintTemplateRepository printTemplateRepository;
  private final UserIdentity userIdentity;

  public PrintTemplateEndpoint(
      PrintTemplateRepository printTemplateRepository, UserIdentity userIdentity) {
    this.printTemplateRepository = printTemplateRepository;
    this.userIdentity = userIdentity;
  }

  @GetMapping
  public List<PrintTemplateDto> getTemplates(
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {
    userIdentity.checkGlobalUserPermission(
        userEmail, UserGroupAuthorisedActivityType.LIST_PRINT_TEMPLATES);

    return printTemplateRepository.findAll().stream()
        .map(
            printTemplate -> {
              PrintTemplateDto printTemplateDto = new PrintTemplateDto();
              printTemplateDto.setTemplate(printTemplate.getTemplate());
              printTemplateDto.setPrintSupplier(printTemplate.getPrintSupplier());
              printTemplateDto.setPackCode(printTemplate.getPackCode());
              return printTemplateDto;
            })
        .collect(Collectors.toList());
  }

  @PostMapping
  public ResponseEntity<Void> createTemplate(
      @RequestBody PrintTemplateDto printTemplateDto,
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {
    userIdentity.checkGlobalUserPermission(
        userEmail, UserGroupAuthorisedActivityType.CREATE_PRINT_TEMPLATE);

    printTemplateRepository
        .findById(printTemplateDto.getPackCode())
        .ifPresent(
            printTemplate -> {
              throw new ResponseStatusException(
                  HttpStatus.CONFLICT,
                  String.format("Packcode '%s' already exists", printTemplate.getPackCode()));
            });

    PrintTemplate printTemplate = new PrintTemplate();
    printTemplate.setTemplate(printTemplateDto.getTemplate());
    printTemplate.setPrintSupplier(printTemplateDto.getPrintSupplier()); // TODO: check it exists
    printTemplate.setPackCode(printTemplateDto.getPackCode());

    printTemplateRepository.saveAndFlush(printTemplate);

    return new ResponseEntity<>(HttpStatus.CREATED);
  }
}
