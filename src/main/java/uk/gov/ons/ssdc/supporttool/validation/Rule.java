package uk.gov.ons.ssdc.supporttool.validation;

import java.io.Serializable;
import java.util.Optional;

public interface Rule extends Serializable {
  Optional<String> checkValidity(String data);
}
