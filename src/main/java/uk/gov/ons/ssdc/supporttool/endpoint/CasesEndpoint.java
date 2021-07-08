package uk.gov.ons.ssdc.supporttool.endpoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.ons.ssdc.supporttool.model.dto.CaseContainerDto;
import uk.gov.ons.ssdc.supporttool.model.entity.Case;
import uk.gov.ons.ssdc.supporttool.model.repository.CaseRepository;

@RestController
@RequestMapping(value = "/cases")
public class CasesEndpoint {

  private final JdbcTemplate jdbcTemplate;

  public CasesEndpoint(JdbcTemplate jdbcTemplate) {

    this.jdbcTemplate = jdbcTemplate;
  }

  // Entity like the current SpringRestMagic? or should it return a DTO,
  // entity for now
  //  can map to caseDTO of some sort if required

  @GetMapping(value = "/search")
  @ResponseBody
  public List<Case> searchCasesBySampleData(
      @RequestParam(value = "searchTerm") String searchTerm,
      // TODO make survey required
      @RequestParam(value = "surveyId") UUID surveyId,
      @RequestParam(value = "collexId", required = false) UUID collexId,
      @RequestParam(value = "receipted", required = false) Boolean receiptReceived,
      @RequestParam(value = "invalid", required = false) Boolean invalidAddress,
      @RequestParam(value = "launched", required = false) Boolean surveyLaunched,
      @RequestParam(value = "refusal", required = false) String refusalReceived) {

    //        Do we build the blooming query dynamically
    //        can possibly make with if null else
    if (refusalReceived.equals("null")) {
      refusalReceived = null;
    }
    searchTerm = '%' + searchTerm + '%';

    String query = "SELECT ca.id, ca.case_ref, ca.sample, ca.address_invalid, ca.receipt_received, ca.refusal_received" +
        " FROM casev3.cases ca, casev3.collection_exercise ce WHERE ca.collection_exercise_id = ce.id" +
        " AND ce.survey_id = ?" +
        " AND EXISTS (SELECT * FROM jsonb_each_text(ca.sample) AS x(ky, val) WHERE lower(x.val) LIKE lower(?))";
    ArrayList<Object> args = new ArrayList<>();
    args.add(surveyId);
    args.add(searchTerm);

    if (collexId != null) {
      query += " AND ce.id = ?";
      args.add(collexId);
    }
    List<Map<String, Object>> results = jdbcTemplate.queryForList(query, args);

    //        Make iterable
    return cases;
  }
}
