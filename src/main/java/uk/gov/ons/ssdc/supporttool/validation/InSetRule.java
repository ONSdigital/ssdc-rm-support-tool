package uk.gov.ons.ssdc.supporttool.validation;

import java.util.Optional;
import java.util.Set;

public class InSetRule implements Rule {

  private final Set<String> set;

  public InSetRule(String[] set) {
    this.set = Set.of(set);
  }

  @Override
  public Optional<String> checkValidity(String data) {
    if (!set.contains(data)) {
      return Optional.of("Not in set of " + String.join(", ", set));
    }

    return Optional.empty();
  }
}
