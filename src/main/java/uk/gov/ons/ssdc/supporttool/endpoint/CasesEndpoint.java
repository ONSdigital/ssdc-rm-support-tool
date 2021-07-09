package uk.gov.ons.ssdc.supporttool.endpoint;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.ons.ssdc.supporttool.model.dto.CaseSearchResults;
import uk.gov.ons.ssdc.supporttool.utility.CaseSearchResultsMapper;

@RestController
@RequestMapping(value = "/cases")
public class CasesEndpoint {

  private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
  private final CaseSearchResultsMapper caseRowMapper;

  public CasesEndpoint(
      NamedParameterJdbcTemplate namedParameterJdbcTemplate,
      CaseSearchResultsMapper caseRowMapper) {
    this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    this.caseRowMapper = caseRowMapper;
  }

  @GetMapping(value = "/search")
  @ResponseBody
  public List<CaseSearchResults> searchCasesBySampleData(
      @RequestParam(value = "searchTerm") String searchTerm,
      @RequestParam(value = "surveyId") UUID surveyId,
      @RequestParam(value = "collexId", required = false) UUID collexId,
      @RequestParam(value = "receipted", required = false) Boolean receiptReceived,
      @RequestParam(value = "invalid", required = false) Boolean addressInvalid,
      @RequestParam(value = "launched", required = false) Boolean surveyLaunched,
      @RequestParam(value = "refusal", required = false) String refusalReceived) {

    if (refusalReceived != null && refusalReceived.equals("null")) {
      // We want to be able to search for cases where refusal received is null by supplying the
      // string "null". If no refusalReceived query string is provided at all then we do not filter
      // at all
      refusalReceived = null;
    }

    searchTerm = '%' + searchTerm + '%';

    String query =
        "SELECT ca.id, ca.case_ref, ca.sample, ca.address_invalid, ca.receipt_received, ca.refusal_received,"
            + " ca.survey_launched, ca.created_at, ca.last_updated_at, ca.collection_exercise_id, ce.name collex_name"
            + " FROM casev3.cases ca, casev3.collection_exercise ce WHERE ca.collection_exercise_id = ce.id"
            + " AND ce.survey_id = :surveyId"
            + " AND EXISTS (SELECT * FROM jsonb_each_text(ca.sample) AS x(ky, val) WHERE lower(x.val) LIKE lower(:searchTerm))";

    Map<String, Object> namedParameters = new HashMap();
    namedParameters.put("surveyId", surveyId);
    namedParameters.put("searchTerm", searchTerm);

    if (collexId != null) {
      query += " AND ce.id = :collexId";
      namedParameters.put("collexId", collexId);
    }
    if (receiptReceived != null) {
      query += " AND ca.receipt_received = :receiptReceived";
      namedParameters.put("receiptReceived", receiptReceived);
    }

    if (addressInvalid != null) {
      query += " AND ca.address_invalid = :addressInvalid";
      namedParameters.put("addressInvalid", addressInvalid);
    }

    if (surveyLaunched != null) {
      query += " AND ca.survey_launched = :surveyLaunched";
      namedParameters.put("surveyLaunched", surveyLaunched);
    }

    if (refusalReceived != null) {
      query += " AND ca.refusal_received = :refusalReceived";
      namedParameters.put("refusalReceiced", refusalReceived);
    }

    return namedParameterJdbcTemplate.query(query, namedParameters, caseRowMapper);
  }
}
