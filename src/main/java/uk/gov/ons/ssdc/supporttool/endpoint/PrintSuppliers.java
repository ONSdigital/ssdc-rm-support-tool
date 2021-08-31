package uk.gov.ons.ssdc.supporttool.endpoint;

import static uk.gov.ons.ssdc.supporttool.model.entity.UserGroupAuthorisedActivityType.CREATE_PRINT_TEMPLATE;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.ons.ssdc.supporttool.security.UserIdentity;
import uk.gov.ons.ssdc.supporttool.utility.ObjectMapperFactory;

@RestController
@RequestMapping(value = "/api/printsuppliers")
public class PrintSuppliers {

  public static final ObjectMapper OBJECT_MAPPER = ObjectMapperFactory.objectMapper();

  private final UserIdentity userIdentity;

  @Value("${printsupplierconfigfile}")
  private String configFile;

  private Set<String> printSuppliers = null;

  public PrintSuppliers(UserIdentity userIdentity) {
    this.userIdentity = userIdentity;
  }

  @GetMapping
  public Set<String> getPrintSuppliers(
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {
    userIdentity.checkGlobalUserPermission(userEmail, CREATE_PRINT_TEMPLATE);

    if (printSuppliers != null) {
      return printSuppliers;
    }

    try (InputStream configFileStream = new FileInputStream(configFile)) {
      Map map = OBJECT_MAPPER.readValue(configFileStream, Map.class);
      printSuppliers = map.keySet();
      return printSuppliers;
    } catch (JsonProcessingException | FileNotFoundException e) {
      throw new RuntimeException(e);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
