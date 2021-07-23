package uk.gov.ons.ssdc.supporttool.endpoint;

import static uk.gov.ons.ssdc.supporttool.model.entity.UserGroupAuthorisedActivityType.CREATE_PRINT_TEMPLATE;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

  @Value("${printsupplierconfig}")
  private String printSupplierConfig;

  private List<String> printSuppliers = null;

  public PrintSuppliers(UserIdentity userIdentity) {
    this.userIdentity = userIdentity;
  }

  @GetMapping
  public List<String> getPrintSuppliers(
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {
    userIdentity.checkGlobalUserPermission(userEmail, CREATE_PRINT_TEMPLATE);

    if (printSuppliers != null) {
      return printSuppliers;
    }

    try {
      Map map = OBJECT_MAPPER.readValue(printSupplierConfig, Map.class);
      printSuppliers = new ArrayList<>(map.keySet());
      return printSuppliers;
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }
}
