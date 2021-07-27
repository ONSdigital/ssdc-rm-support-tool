package uk.gov.ons.ssdc.supporttool.endpoint;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.ons.ssdc.supporttool.model.dto.ui.UIRefusalTypeDTO;

@RestController
@RequestMapping(value = "/api/refusals")
public class RefusalsEndpoint {

  @GetMapping(value = "/types")
  @ResponseBody
  public List<String> getRefusalType() {
    List<String> refusals =
        Stream.of(UIRefusalTypeDTO.values()).map(Enum::name).collect(Collectors.toList());

    return refusals;
  }
}
