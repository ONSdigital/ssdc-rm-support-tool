package uk.gov.ons.ssdc.supporttool.validation;

import java.util.Optional;
import java.util.UUID;

public class UUIDRule implements Rule {

  @Override
  public Optional<String> checkValidity(String data) {
    try {
      UUID.fromString(data);
      return Optional.empty();
    } catch (Exception e) {
      return Optional.of("Not a valid UUID");
    }
  }
}
