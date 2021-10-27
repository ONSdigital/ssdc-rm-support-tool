package uk.gov.ons.ssdc.supporttool.transformer;

import uk.gov.ons.ssdc.common.model.entity.Job;
import uk.gov.ons.ssdc.common.model.entity.JobRow;
import uk.gov.ons.ssdc.common.validation.ColumnValidator;
import uk.gov.ons.ssdc.supporttool.model.dto.messaging.CollectionCase;
import uk.gov.ons.ssdc.supporttool.model.dto.messaging.EventDTO;
import uk.gov.ons.ssdc.supporttool.model.dto.messaging.EventHeaderDTO;
import uk.gov.ons.ssdc.supporttool.model.dto.messaging.PayloadDTO;
import uk.gov.ons.ssdc.supporttool.model.dto.messaging.RefusalDTO;
import uk.gov.ons.ssdc.supporttool.model.dto.messaging.RefusalTypeDTO;
import uk.gov.ons.ssdc.supporttool.utility.EventHelper;

import java.util.Map;
import java.util.UUID;

public class BulkRefusalTransformer implements Transformer {

  @Override
  public Object transformRow(
      Job job, JobRow jobRow, ColumnValidator[] columnValidators, String topic) {
    Map<String, String> rowData = jobRow.getRowData();

    CollectionCase collectionCase = new CollectionCase();
    collectionCase.setCaseId(UUID.fromString(rowData.get("caseId")));

    RefusalDTO refusalDTO = new RefusalDTO();
    refusalDTO.setCaseId(UUID.fromString(rowData.get("caseId")));
    refusalDTO.setType(RefusalTypeDTO.valueOf(rowData.get("refusalType")));

    PayloadDTO payloadDTO = new PayloadDTO();
    payloadDTO.setRefusal(refusalDTO);

    EventDTO event = new EventDTO();
    EventHeaderDTO eventHeader = EventHelper.createEventDTO(topic, job.getProcessedBy());
    eventHeader.setCorrelationId(job.getId());
    event.setHeader(eventHeader);
    event.setPayload(payloadDTO);

    return event;
  }
}
