package uk.gov.ons.ssdc.supporttool.validation;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import java.util.Optional;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class MandatoryRule implements Rule {

  @Override
  public Optional<String> checkValidity(String data) {
    if (data.strip().isEmpty()) {
      return Optional.of("Mandatory value missing");
    }

    return Optional.empty();
  }
}
