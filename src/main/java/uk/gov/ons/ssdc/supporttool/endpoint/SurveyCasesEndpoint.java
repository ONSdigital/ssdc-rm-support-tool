package uk.gov.ons.ssdc.supporttool.endpoint;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.ons.ssdc.common.model.entity.Survey;
import uk.gov.ons.ssdc.common.model.entity.UserGroupAuthorisedActivityType;
import uk.gov.ons.ssdc.supporttool.model.dto.ui.CaseSearchResult;
import uk.gov.ons.ssdc.supporttool.model.dto.ui.UIRefusalTypeDTO;
import uk.gov.ons.ssdc.supporttool.model.repository.SurveyRepository;
import uk.gov.ons.ssdc.supporttool.security.AuthUser;
import uk.gov.ons.ssdc.supporttool.utility.CaseSearchResultsMapper;

@RestController
@RequestMapping(value = "/api/surveyCases")
public class SurveyCasesEndpoint {
  private static final Logger log = LoggerFactory.getLogger(SurveyCasesEndpoint.class);

  private final SurveyRepository surveyRepository;
  private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
  private final CaseSearchResultsMapper caseRowMapper;
  private final AuthUser authUser;

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
      AuthUser authUser) {
    this.surveyRepository = surveyRepository;
    this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    this.caseRowMapper = caseRowMapper;
    this.authUser = authUser;
  }

  @GetMapping(value = "/{surveyId}")
  @ResponseBody
  public List<CaseSearchResult> searchCasesBySampleData(
      @Value("#{request.getAttribute('userEmail')}") String userEmail,
      @PathVariable(value = "surveyId") UUID surveyId,
      @RequestParam(value = "searchTerm") String searchTerm,
      @RequestParam(value = "collexId", required = false) Optional<UUID> collexId,
      @RequestParam(value = "invalid", required = false) Optional<Boolean> caseInvalid,
      @RequestParam(value = "refusal", required = false)
          Optional<UIRefusalTypeDTO> refusalReceived) {

    checkSurveySearchCasesPermission(userEmail, surveyId);

    String escapedSearchTerm = escapeSqlLikeSpecialCharacters(searchTerm);
    String likeSearchTerm = String.format("%%%s%%", escapedSearchTerm);
    StringBuilder queryStringBuilder = new StringBuilder(searchCasesInSurveyPartialQuery);
    queryStringBuilder
        .append(" AND EXISTS (SELECT * FROM jsonb_each_text(c.sample) AS x(ky, val)")
        .append(
            " WHERE LOWER(REPLACE(x.val, ' ', '')) LIKE LOWER(REPLACE(:likeSearchTerm, ' ', '')) ESCAPE '\\')");

    Map<String, Object> namedParameters = new HashMap();
    namedParameters.put("surveyId", surveyId);
    namedParameters.put("likeSearchTerm", likeSearchTerm);

    if (collexId.isPresent()) {
      queryStringBuilder.append(" AND e.id = :collexId");
      namedParameters.put("collexId", collexId.get());
    }

    if (caseInvalid.isPresent()) {
      queryStringBuilder.append(" AND c.invalid = :caseInvalid");
      namedParameters.put("caseInvalid", caseInvalid.get());
    }

    if (refusalReceived.isPresent()) {
      if (refusalReceived.get() == UIRefusalTypeDTO.NOT_REFUSED) {
        queryStringBuilder.append(" AND c.refusal_received IS NULL");
      } else {
        queryStringBuilder.append(" AND c.refusal_received = :refusalReceived");
        namedParameters.put("refusalReceived", refusalReceived.get().toString());
      }
    }

    queryStringBuilder.append(" LIMIT 100");

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
      log.atWarn()
          .setMessage("Failed to get case for survey, survey not found")
          .addKeyValue("surveyId", surveyId)
          .addKeyValue("userEmail", userEmail)
          .addKeyValue("httpStatus", HttpStatus.NOT_FOUND)
          .log();
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Survey not found");
    }
    authUser.checkUserPermission(
        userEmail, surveyOptional.get().getId(), UserGroupAuthorisedActivityType.SEARCH_CASES);
  }

  private String escapeSqlLikeSpecialCharacters(String stringToEscape) {
    return stringToEscape.replace("%", "\\%").replace("_", "\\_");
  }
}
