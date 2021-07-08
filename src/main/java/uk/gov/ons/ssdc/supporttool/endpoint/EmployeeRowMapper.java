//package uk.gov.ons.ssdc.supporttool.endpoint;
//
//import org.springframework.jdbc.core.RowMapper;
//import uk.gov.ons.ssdc.supporttool.model.dto.CaseContainerDto;
//
//import java.sql.ResultSet;
//import java.sql.SQLException;
//
////Needs moving somewhere
//public class CaseRowMapper implements RowMapper<CaseContainerDto> {
//    @Override
//    public CaseContainerDto mapRow(ResultSet rs, int rowNum) throws SQLException {
//        CaseContainerDto caseContainerDto = new CaseContainerDto();
////         caseContainerDto.setId(rs.getString("id"));
//        caseContainerDto.setCaseRef(rs.getString("caseRef"));
////        caseContainerDto.setSample(rs.get("sample"));
//
//
//        return caseContainerDto;
//    }
//}