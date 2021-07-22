package uk.gov.ons.ssdc.supporttool.validation;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import java.util.Optional;
import org.springframework.util.StringUtils;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class MandatoryRule implements Rule {

  @Override
  public Optional<String> checkValidity(String data) {
    if (!StringUtils.hasText(data)) {
      return Optional.of("Mandatory value missing");
    }

    return Optional.empty();
  }
}
