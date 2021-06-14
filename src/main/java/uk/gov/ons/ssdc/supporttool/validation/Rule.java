package uk.gov.ons.ssdc.supporttool.validation;

import java.util.Optional;

public interface Rule {
  Optional<String> checkValidity(String data);
}
