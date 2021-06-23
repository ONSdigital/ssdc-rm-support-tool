package uk.gov.ons.ssdc.supporttool.validation;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class AlphanumericRule implements Rule {

  @Override
  public Optional<String> checkValidity(String data) {
    if (!StringUtils.isAlphanumeric(data.replace(" ", ""))) {
      return Optional.of("Value is not alphanumeric");
    }

    return Optional.empty();
  }
}
