package uk.gov.ons.ssdc.supporttool.endpoint;

import java.util.LinkedList;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.ons.ssdc.supporttool.model.dto.BulkProcessDto;
import uk.gov.ons.ssdc.supporttool.model.entity.BulkProcess;
import uk.gov.ons.ssdc.supporttool.security.UserIdentity;

@RestController
@RequestMapping(value = "/bulkprocess")
public class BulkProcessEndpoint {
  private final UserIdentity userIdentity;

  public BulkProcessEndpoint(UserIdentity userIdentity) {
    this.userIdentity = userIdentity;
  }

  @GetMapping
  public List<BulkProcessDto> getBulkProcesses(
      @RequestHeader(required = false, value = "x-goog-iap-jwt-assertion") String jwtToken) {
    List<BulkProcessDto> bulkProcessDtos = new LinkedList<>();

    userIdentity
        .getBulkProcesses(jwtToken)
        .forEach(bulkProcess -> bulkProcessDtos.add(mapBulkProcess(bulkProcess)));

    return bulkProcessDtos;
  }

  private BulkProcessDto mapBulkProcess(BulkProcess bulkProcess) {
    BulkProcessDto bulkProcessDto = new BulkProcessDto();
    bulkProcessDto.setBulkProcess(bulkProcess.name());
    bulkProcessDto.setTitle(bulkProcess.getTitle());
    return bulkProcessDto;
  }
}
