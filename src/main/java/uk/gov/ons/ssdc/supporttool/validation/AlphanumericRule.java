package uk.gov.ons.ssdc.supporttool.validation;

import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

public class AlphanumericRule implements Rule {

  @Override
  public Optional<String> checkValidity(String data) {
    if (!StringUtils.isAlphanumeric(data.replace(" ", ""))) {
      return Optional.of("Value is not alphanumeric");
    }

    return Optional.empty();
  }
}
