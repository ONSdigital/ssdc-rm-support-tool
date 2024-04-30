package uk.gov.ons.ssdc.supporttool.utility;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import uk.gov.ons.ssdc.supporttool.model.dto.ui.CaseSearchResult;

@Component
public class CaseSearchResultsMapper implements RowMapper<CaseSearchResult> {
  private static final Logger log = LoggerFactory.getLogger(CaseSearchResultsMapper.class);

  private static final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public CaseSearchResult mapRow(ResultSet resultSet, int rowNum) {
    CaseSearchResult caseContainerDto = new CaseSearchResult();
    try {
      caseContainerDto.setId(resultSet.getObject("id", UUID.class));
      caseContainerDto.setCaseRef(resultSet.getString("case_ref"));
      caseContainerDto.setCollectionExerciseName(resultSet.getString("collex_name"));
      caseContainerDto.setSample(objectMapper.readValue(resultSet.getString("sample"), Map.class));
    } catch (SQLException | JsonProcessingException e) {
      log.atError().setMessage("Error mapping case search results").log();
      throw new RuntimeException("Error mapping case search results", e);
    }
    return caseContainerDto;
  }
}
