package uk.gov.ons.ssdc.supporttool.endpoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
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

  private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  public CasesEndpoint(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
    this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
  }

  // Entity like the current SpringRestMagic? or should it return a DTO,
  // entity for now
  //  can map to caseDTO of some sort if required

  @GetMapping(value = "/search")
  @ResponseBody
  public ResponseEntity searchCasesBySampleData(
      @RequestParam(value = "searchTerm") String searchTerm,
      // TODO make survey required
      @RequestParam(value = "surveyId") UUID surveyId,
      @RequestParam(value = "collexId", required = false) UUID collexId,
      @RequestParam(value = "receipted", required = false) Boolean receiptReceived,
      @RequestParam(value = "invalid", required = false) Boolean invalidAddress,
      @RequestParam(value = "launched", required = false) Boolean surveyLaunched,
      @RequestParam(value = "refusal", required = false) String refusalReceived) throws JsonProcessingException {

    //        Do we build the blooming query dynamically
    //        can possibly make with if null else

    if (refusalReceived != null) {
      if (refusalReceived.equals("null")) {
        refusalReceived = null;
      }
    }

    searchTerm = '%' + searchTerm + '%';

    String query =
        "SELECT ca.id, ca.case_ref, ca.sample, ca.address_invalid, ca.receipt_received, ca.refusal_received"
            + " FROM casev3.cases ca, casev3.collection_exercise ce WHERE ca.collection_exercise_id = ce.id"
            + " AND ce.survey_id = :surveyId"
            + " AND EXISTS (SELECT * FROM jsonb_each_text(ca.sample) AS x(ky, val) WHERE lower(x.val) LIKE lower(:searchTerm))";

    Map namedParameters = new HashMap();
    namedParameters.put("surveyId", surveyId);
    namedParameters.put("searchTerm", searchTerm);

    if (collexId != null) {
      query += " AND ce.id = :collexId";
      namedParameters.put("collexId", collexId);
    }

    List<Map<String, Object>> result = namedParameterJdbcTemplate.queryForList(query, namedParameters);
//    ObjectMapper objectMapper = new ObjectMapper();
//    final String str = objectMapper.writeValueAsString(result);
    var headers = new HttpHeaders();
    headers.add("Responded", "MyController");

//    Not sure about headers, ignore?  Accepted is that 202? want 200?
    return ResponseEntity.accepted().headers(headers).body(result);

    //
    //    query = "SELECT ca.id, ca.case_ref, ca.sample, ca.address_invalid, ca.receipt_received,
    // ca.refusal_received" +
    //        " FROM casev3.cases ca, casev3.collection_exercise ce WHERE ca.collection_exercise_id
    // = ce.id" +
    //        " AND ce.survey_id = ?" +
    //        " AND EXISTS (SELECT * FROM jsonb_each_text(ca.sample) AS x(ky, val) WHERE
    // lower(x.val) LIKE lower(?))";
    //    ArrayList<Object> args = new ArrayList<>();
    //    args.add(surveyId);
    //    args.add(searchTerm);
    //
    //
    //    List<Map<String, Object>> results = jdbcTemplate.queryForList(query, args);

    //        Make iterable
  }
}
