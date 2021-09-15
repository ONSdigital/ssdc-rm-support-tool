package uk.gov.ons.ssdc.supporttool.endpoint;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.ons.ssdc.common.model.entity.Survey;
import uk.gov.ons.ssdc.common.model.entity.UserGroupAuthorisedActivityType;
import uk.gov.ons.ssdc.supporttool.model.dto.ui.CaseSearchResult;
import uk.gov.ons.ssdc.supporttool.model.dto.ui.UIRefusalTypeDTO;
import uk.gov.ons.ssdc.supporttool.model.repository.SurveyRepository;
import uk.gov.ons.ssdc.supporttool.security.UserIdentity;
import uk.gov.ons.ssdc.supporttool.utility.CaseSearchResultsMapper;

@RestController
@RequestMapping(value = "/api/surveyCases")
public class SurveyCasesEndpoint {

  private final SurveyRepository surveyRepository;
  private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
  private final CaseSearchResultsMapper caseRowMapper;
  private final UserIdentity userIdentity;

  private static final String searchCasesPartialQuery =
      "SELECT c.id, c.case_ref, c.sample, e.name collex_name";
  private static final String searchCasesInSurveyPartialQuery =
      searchCasesPartialQuery
          + " FROM casev3.cases c, casev3.collection_exercise e WHERE c.collection_exercise_id = e.id"
          + " AND e.survey_id = :surveyId";

  public SurveyCasesEndpoint(
      SurveyRepository surveyRepository,
      NamedParameterJdbcTemplate namedParameterJdbcTemplate,
      CaseSearchResultsMapper caseRowMapper,
      UserIdentity userIdentity) {
    this.surveyRepository = surveyRepository;
    this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    this.caseRowMapper = caseRowMapper;
    this.userIdentity = userIdentity;
  }

  @GetMapping(value = "/{surveyId}")
  @ResponseBody
  public List<CaseSearchResult> searchCasesBySampleData(
      @Value("#{request.getAttribute('userEmail')}") String userEmail,
      @PathVariable(value = "surveyId") UUID surveyId,
      @RequestParam(value = "searchTerm") String searchTerm,
      @RequestParam(value = "collexId", required = false) Optional<UUID> collexId,
      @RequestParam(value = "receipted", required = false) Optional<Boolean> receiptReceived,
      @RequestParam(value = "invalid", required = false) Optional<Boolean> addressInvalid,
      @RequestParam(value = "launched", required = false) Optional<Boolean> eqLaunched,
      @RequestParam(value = "refusal", required = false)
          Optional<UIRefusalTypeDTO> refusalReceived) {

    checkSurveySearchCasesPermission(userEmail, surveyId);

    String likeSearchTerm = String.format("%%%s%%", searchTerm);
    StringBuilder queryStringBuilder = new StringBuilder(searchCasesInSurveyPartialQuery);
    queryStringBuilder
        .append(" AND EXISTS (SELECT * FROM jsonb_each_text(c.sample) AS x(ky, val)")
        .append(
            " WHERE LOWER(REPLACE(x.val, ' ', '')) LIKE LOWER(REPLACE(:likeSearchTerm, ' ', '')))");

    Map<String, Object> namedParameters = new HashMap();
    namedParameters.put("surveyId", surveyId);
    namedParameters.put("likeSearchTerm", likeSearchTerm);

    if (collexId.isPresent()) {
      queryStringBuilder.append(" AND e.id = :collexId");
      namedParameters.put("collexId", collexId.get());
    }

    if (receiptReceived.isPresent()) {
      queryStringBuilder.append(" AND c.receipt_received = :receiptReceived");
      namedParameters.put("receiptReceived", receiptReceived.get());
    }

    if (addressInvalid.isPresent()) {
      queryStringBuilder.append(" AND c.address_invalid = :addressInvalid");
      namedParameters.put("addressInvalid", addressInvalid.get());
    }

    if (eqLaunched.isPresent()) {
      queryStringBuilder.append(" AND c.eq_launched = :eqLaunched");
      namedParameters.put("eqLaunched", eqLaunched.get());
    }

    if (refusalReceived.isPresent()) {
      if (refusalReceived.get() == UIRefusalTypeDTO.NOT_REFUSED) {
        queryStringBuilder.append(" AND c.refusal_received IS NULL");
      } else {
        queryStringBuilder.append(" AND c.refusal_received = :refusalReceived");
        namedParameters.put("refusalReceived", refusalReceived.get().toString());
      }
    }

    return namedParameterJdbcTemplate.query(
        queryStringBuilder.toString(), namedParameters, caseRowMapper);
  }

  @GetMapping(value = "/{surveyId}/caseRef/{caseRef}")
  @ResponseBody
  public List<CaseSearchResult> getCaseByCaseRef(
      @PathVariable(value = "surveyId") UUID surveyId,
      @PathVariable(value = "caseRef") long caseRef,
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {
    checkSurveySearchCasesPermission(userEmail, surveyId);

    String query = searchCasesInSurveyPartialQuery + " AND c.case_ref = :caseRef";

    Map<String, Object> namedParameters = new HashMap();
    namedParameters.put("surveyId", surveyId);
    namedParameters.put("caseRef", caseRef);
    return namedParameterJdbcTemplate.query(query, namedParameters, caseRowMapper);
  }

  @GetMapping(value = "/{surveyId}/qid/{qid}")
  @ResponseBody
  public List<CaseSearchResult> getCaseByQid(
      @PathVariable(value = "surveyId") UUID surveyId,
      @PathVariable(value = "qid") String qid,
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {
    checkSurveySearchCasesPermission(userEmail, surveyId);

    String query =
        searchCasesPartialQuery
            + " FROM casev3.uac_qid_link u, casev3.cases c, casev3.collection_exercise e"
            + " WHERE c.collection_exercise_id = e.id AND u.caze_id = c.id"
            + " AND u.qid = :qid";
    Map<String, Object> namedParameters = new HashMap();
    namedParameters.put("surveyId", surveyId);
    namedParameters.put("qid", qid);

    return namedParameterJdbcTemplate.query(query, namedParameters, caseRowMapper);
  }

  private void checkSurveySearchCasesPermission(String userEmail, UUID surveyId) {
    Optional<Survey> surveyOptional = surveyRepository.findById(surveyId);
    if (surveyOptional.isEmpty()) {
      throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Survey not found");
    }
    userIdentity.checkUserPermission(
        userEmail, surveyOptional.get(), UserGroupAuthorisedActivityType.SEARCH_CASES);
  }
}
