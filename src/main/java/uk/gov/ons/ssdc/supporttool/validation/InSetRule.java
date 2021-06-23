package uk.gov.ons.ssdc.supporttool.validation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import java.util.Set;

public class InSetRule implements Rule {

  private final Set<String> set;

  @JsonCreator
  public InSetRule(@JsonProperty("set") String[] set) {
    this.set = Set.of(set);
  }

  @Override
  public Optional<String> checkValidity(String data) {
    if (!set.contains(data)) {
      return Optional.of("Not in set of " + String.join(", ", set));
    }

    return Optional.empty();
  }

  public Set<String> getSet() {
    return set;
  }
}
