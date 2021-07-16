package uk.gov.ons.ssdc.supporttool.utility;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import lombok.SneakyThrows;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import uk.gov.ons.ssdc.supporttool.model.dto.CaseSearchResult;

@Component
public class CaseSearchResultsMapper implements RowMapper<CaseSearchResult> {
  private static final ObjectMapper objectMapper = new ObjectMapper();

  @SneakyThrows
  @Override
  public CaseSearchResult mapRow(ResultSet resultSet, int rowNum) throws SQLException {
    CaseSearchResult caseContainerDto = new CaseSearchResult();
    caseContainerDto.setId(resultSet.getObject("id", UUID.class));
    caseContainerDto.setCaseRef(resultSet.getString("case_ref"));
    caseContainerDto.setSample(objectMapper.readValue(resultSet.getString("sample"), Map.class));

    caseContainerDto.setCollectionExerciseId(
        resultSet.getObject("collection_exercise_id", UUID.class));
    caseContainerDto.setCollectionExerciseName(resultSet.getString("collex_name"));
    return caseContainerDto;
  }
}
