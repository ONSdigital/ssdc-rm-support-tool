package uk.gov.ons.ssdc.supporttool.endpoint;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.ons.ssdc.supporttool.model.dto.CaseSearchResult;
import uk.gov.ons.ssdc.supporttool.model.entity.Survey;
import uk.gov.ons.ssdc.supporttool.model.entity.UserGroupAuthorisedActivityType;
import uk.gov.ons.ssdc.supporttool.model.repository.SurveyRepository;
import uk.gov.ons.ssdc.supporttool.security.UserIdentity;
import uk.gov.ons.ssdc.supporttool.utility.CaseSearchResultsMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping(value = "/searchInSurvey")
public class SurveyCasesEndpoint {

  private final SurveyRepository surveyRepository;
  private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
  private final CaseSearchResultsMapper caseRowMapper;
  private final UserIdentity userIdentity;


  private static final String searchCasesPartialQuery =
      "SELECT c.id, c.case_ref, c.sample, c.address_invalid, c.receipt_received, c.refusal_received,"
          + " c.survey_launched, c.created_at, c.last_updated_at, c.collection_exercise_id, e.name collex_name";
  private static final String searchCasesInSurveyPartialQuery = searchCasesPartialQuery
      + " FROM casev3.cases c, casev3.collection_exercise e WHERE c.collection_exercise_id = e.id"
      + " AND e.survey_id = :surveyId";

  public SurveyCasesEndpoint(
      SurveyRepository surveyRepository, NamedParameterJdbcTemplate namedParameterJdbcTemplate,
      CaseSearchResultsMapper caseRowMapper, UserIdentity userIdentity) {
    this.surveyRepository = surveyRepository;
    this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    this.caseRowMapper = caseRowMapper;
    this.userIdentity = userIdentity;
  }

  @GetMapping(value = "/{surveyId}")
  @ResponseBody
  public List<CaseSearchResult> searchCasesBySampleData(
      @RequestHeader(required = false, value = "x-goog-iap-jwt-assertion") String jwt,
      @PathVariable(value = "surveyId") UUID surveyId,
      @RequestParam(value = "searchTerm") String searchTerm,
      @RequestParam(value = "collexId", required = false) UUID collexId,
      @RequestParam(value = "receipted", required = false) Boolean receiptReceived,
      @RequestParam(value = "invalid", required = false) Boolean addressInvalid,
      @RequestParam(value = "launched", required = false) Boolean surveyLaunched,
      @RequestParam(value = "refusal", required = false) String refusalReceived) {

    checkSurveySearchCasesPermission(jwt, surveyId);

    if (refusalReceived != null && refusalReceived.equals("null")) {
      // We want to be able to search for cases where refusal received is null by supplying the
      // string "null". If no refusalReceived query string is provided at all then we do not filter
      // at all
      refusalReceived = null;
    }

    searchTerm = '%' + searchTerm + '%';

    String query = searchCasesInSurveyPartialQuery
        + " AND EXISTS (SELECT * FROM jsonb_each_text(c.sample) AS x(ky, val) WHERE lower(x.val) LIKE lower(:searchTerm))";

    Map<String, Object> namedParameters = new HashMap();
    namedParameters.put("surveyId", surveyId);
    namedParameters.put("searchTerm", searchTerm);

    if (collexId != null) {
      query += " AND e.id = :collexId";
      namedParameters.put("collexId", collexId);
    }
    if (receiptReceived != null) {
      query += " AND c.receipt_received = :receiptReceived";
      namedParameters.put("receiptReceived", receiptReceived);
    }

    if (addressInvalid != null) {
      query += " AND c.address_invalid = :addressInvalid";
      namedParameters.put("addressInvalid", addressInvalid);
    }

    if (surveyLaunched != null) {
      query += " AND c.survey_launched = :surveyLaunched";
      namedParameters.put("surveyLaunched", surveyLaunched);
    }

    if (refusalReceived != null) {
      query += " AND c.refusal_received = :refusalReceived";
      namedParameters.put("refusalReceiced", refusalReceived);
    }

    return namedParameterJdbcTemplate.query(query, namedParameters, caseRowMapper);
  }

  @GetMapping(value = "/{surveyId}/caseRef/{caseRef}")
  @ResponseBody
  public List<CaseSearchResult> getCaseByCaseRef(
      @RequestHeader(required = false, value = "x-goog-iap-jwt-assertion") String jwt,
                                                 @PathVariable(value = "surveyId") UUID surveyId,
                                                 @PathVariable(value = "caseRef") long caseRef) {
    checkSurveySearchCasesPermission(jwt, surveyId);

    String query = searchCasesInSurveyPartialQuery + " AND c.case_ref = :caseRef";

    Map<String, Object> namedParameters = new HashMap();
    namedParameters.put("surveyId", surveyId);
    namedParameters.put("caseRef", caseRef);
    return namedParameterJdbcTemplate.query(query, namedParameters, caseRowMapper);
  }

  @GetMapping(value = "/{surveyId}/qid/{qid}")
  @ResponseBody
  public List<CaseSearchResult> getCaseByQid(
      @RequestHeader(required = false, value = "x-goog-iap-jwt-assertion") String jwt,
                                             @PathVariable(value = "surveyId") UUID surveyId,
                                             @PathVariable(value = "qid") String qid) {
    checkSurveySearchCasesPermission(jwt, surveyId);

    String query = searchCasesPartialQuery + " FROM casev3.uac_qid_link u, casev3.cases c, casev3.collection_exercise e"
        + " WHERE c.collection_exercise_id = e.id AND u.caze_id = c.id"
        + " AND u.qid = :qid";
    Map<String, Object> namedParameters = new HashMap();
    namedParameters.put("surveyId", surveyId);
    namedParameters.put("qid", qid);

    return namedParameterJdbcTemplate.query(query, namedParameters, caseRowMapper);
  }

  private void checkSurveySearchCasesPermission(String jwt, UUID surveyId) {
    Optional<Survey> surveyOptional = surveyRepository.findById(surveyId);
    if (surveyOptional.isEmpty()) {
      throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Survey not found");
    }
    userIdentity.checkUserPermission(jwt, surveyOptional.get(), UserGroupAuthorisedActivityType.SEARCH_CASES);
  }
}
