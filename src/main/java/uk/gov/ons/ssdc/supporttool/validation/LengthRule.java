package uk.gov.ons.ssdc.supporttool.validation;

import java.util.Optional;

public class LengthRule implements Rule {

  private final int maxLength;

  public LengthRule(int maxLength) {
    this.maxLength = maxLength;
  }

  @Override
  public Optional<String> checkValidity(String data) {
    if (data.length() > maxLength) {
      return Optional.of("Exceeded max length of " + maxLength);
    }

    return Optional.empty();
  }
}
