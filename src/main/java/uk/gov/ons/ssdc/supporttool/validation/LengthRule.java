package uk.gov.ons.ssdc.supporttool.validation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;

public class LengthRule implements Rule {

  private final int maxLength;

  @JsonCreator
  public LengthRule(@JsonProperty("maxLength") int maxLength) {
    this.maxLength = maxLength;
  }

  @Override
  public Optional<String> checkValidity(String data) {
    if (data.length() > maxLength) {
      return Optional.of("Exceeded max length of " + maxLength);
    }

    return Optional.empty();
  }

  public int getMaxLength() {
    return maxLength;
  }
}
