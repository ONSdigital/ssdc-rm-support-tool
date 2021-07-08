package uk.gov.ons.ssdc.supporttool.endpoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import uk.gov.ons.ssdc.supporttool.model.dto.CaseContainerDto;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

//Needs moving somewhere
@Component
public class CaseRowMapper implements RowMapper<CaseContainerDto> {
  private static final ObjectMapper objectMapper = new ObjectMapper();

  @SneakyThrows
  @Override
  public CaseContainerDto mapRow(ResultSet resultSet, int rowNum) throws SQLException {
    CaseContainerDto caseContainerDto = new CaseContainerDto();
    caseContainerDto.setId(resultSet.getObject("id", UUID.class));
    caseContainerDto.setCaseRef(resultSet.getString("case_ref"));
    caseContainerDto.setSurveyLaunched(resultSet.getObject("survey_launched", boolean.class));
    caseContainerDto.setReceiptReceived(resultSet.getObject("receipt_received", boolean.class));
    caseContainerDto.setAddressInvalid(resultSet.getObject("address_invalid", boolean.class));
    caseContainerDto.setSample(objectMapper.readValue(resultSet.getString("sample"), Map.class));


    return caseContainerDto;
  }
}