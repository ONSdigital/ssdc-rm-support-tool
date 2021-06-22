package uk.gov.ons.ssdc.supporttool.validation;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import java.util.Optional;
import java.util.UUID;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
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
