package uk.gov.ons.ssdc.supporttool.endpoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import uk.gov.ons.ssdc.supporttool.model.dto.CaseContainerDto;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

//Needs moving somewhere
@Component
public class CaseRowMapper implements RowMapper<CaseContainerDto> {
    @SneakyThrows
    @Override
    public CaseContainerDto mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        CaseContainerDto caseContainerDto = new CaseContainerDto();
//         caseContainerDto.setId(resultSet.getString("id"));
        caseContainerDto.setCaseRef(resultSet.getString("case_ref"));
        ObjectMapper objectMapper = new ObjectMapper();
        caseContainerDto.setSample(objectMapper.readValue(resultSet.getString("sample"), Map.class));


//        String sample = resultSet.getString("sample");

//        caseContainerDto.setSample(resultSet.get("sample"));


        return caseContainerDto;
    }
}