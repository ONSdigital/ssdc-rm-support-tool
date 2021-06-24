package uk.gov.ons.ssdc.supporttool;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.FileInputStream;
import org.junit.Test;
import uk.gov.ons.ssdc.supporttool.utility.ObjectMapperFactory;
import uk.gov.ons.ssdc.supporttool.validation.ColumnValidator;

public class ReadValidationConfigTest {
  ObjectMapper objectMapper = ObjectMapperFactory.objectMapper();

  @Test
  public void testCanReadValidationJson() throws Exception {
    try (FileInputStream fis =
        new FileInputStream("src/test/resources/example-validator-config.json")) {
      ColumnValidator[] columnValidators = objectMapper.readValue(fis, ColumnValidator[].class);
      // This line would be unreachable if we couldn't unmarshal the JSON
    }
  }
}
