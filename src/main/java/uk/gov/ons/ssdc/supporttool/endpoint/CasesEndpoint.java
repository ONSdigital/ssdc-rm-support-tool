package uk.gov.ons.ssdc.supporttool.endpoint;

import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.ons.ssdc.supporttool.model.entity.Case;
import uk.gov.ons.ssdc.supporttool.model.repository.CaseRepository;

@RestController
@RequestMapping(value = "/cases")
public class CasesEndpoint {

  private final CaseRepository caseRepository;

  public CasesEndpoint(CaseRepository caseRepository) {

    this.caseRepository = caseRepository;
  }

  // Entity like the current SpringRestMagic? or should it return a DTO,
  // entity for now
  //  can map to caseDTO of some sort if required

  @GetMapping(value = "/search")
  @ResponseBody
  public List<Case> getCases(
      @RequestParam(value = "searchTerm", required = false, defaultValue = "") String searchTerm,
      //            This will possibly become required = true
      @RequestParam(value = "surveyId", required = false) UUID surveyId,
      @RequestParam(value = "receipted", required = false) Boolean receipted,
      @RequestParam(value = "refused", required = false) Boolean refused,
      @RequestParam(value = "invalid", required = false) Boolean invalidAddress) {

    //        Do we build the blooming query dynamically
    //        can possibly make with if null else

    List<Case> cases = caseRepository.findCaseBySearchInSample(searchTerm);




    //        Make iterable
    return cases;
  }
}
