package uk.gov.ons.ssdc.supporttool.validators;

import java.util.Optional;
import java.util.UUID;
import org.springframework.context.ApplicationContext;
import uk.gov.ons.ssdc.common.validation.Rule;
import uk.gov.ons.ssdc.supporttool.config.ApplicationContextProvider;
import uk.gov.ons.ssdc.supporttool.model.repository.CaseRepository;

public class CaseExistsRule implements Rule {
  private CaseRepository caseRepository = null;

  @Override
  public Optional<String> checkValidity(String data) {
    try {
      getCaseRepository().existsById(UUID.fromString(data));
    } catch (Exception e) {
      return Optional.of(String.format("Case Id %s not recognised", data));
    }

    return Optional.empty();
  }

  private CaseRepository getCaseRepository() {
    if (caseRepository == null) {
      ApplicationContext applicationContext = ApplicationContextProvider.getApplicationContext();
      caseRepository = applicationContext.getBean(CaseRepository.class);
    }

    return caseRepository;
  }
}
