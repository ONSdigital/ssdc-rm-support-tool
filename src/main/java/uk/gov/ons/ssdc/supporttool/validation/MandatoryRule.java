package uk.gov.ons.ssdc.supporttool.validation;

import java.util.Optional;

public class MandatoryRule implements Rule {

  @Override
  public Optional<String> checkValidity(String data) {
    if (data.strip().isEmpty()) {
      return Optional.of("Mandatory value missing");
    }

    return Optional.empty();
  }
}
