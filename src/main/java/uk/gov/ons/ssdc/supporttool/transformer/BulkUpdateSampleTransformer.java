package uk.gov.ons.ssdc.supporttool.transformer;

import java.util.Map;
import java.util.UUID;
import uk.gov.ons.ssdc.common.model.entity.Job;
import uk.gov.ons.ssdc.common.model.entity.JobRow;
import uk.gov.ons.ssdc.common.validation.ColumnValidator;
import uk.gov.ons.ssdc.supporttool.model.dto.messaging.EventDTO;
import uk.gov.ons.ssdc.supporttool.model.dto.messaging.EventHeaderDTO;
import uk.gov.ons.ssdc.supporttool.model.dto.messaging.PayloadDTO;
import uk.gov.ons.ssdc.supporttool.model.dto.messaging.UpdateSample;
import uk.gov.ons.ssdc.supporttool.utility.EventHelper;

public class BulkUpdateSampleTransformer implements Transformer {

  @Override
  public Object transformRow(
      Job job, JobRow jobRow, ColumnValidator[] columnValidators, String topic) {
    Map<String, String> rowData = jobRow.getRowData();

    UpdateSample updateSample = new UpdateSample();
    updateSample.setCaseId(UUID.fromString(rowData.get("caseId")));
    //    updateSample.setSample(rowData.get("sample")));

    PayloadDTO payloadDTO = new PayloadDTO();
    payloadDTO.setUpdateSample(updateSample);

    EventDTO event = new EventDTO();
    EventHeaderDTO eventHeader = EventHelper.createEventDTO(topic, job.getProcessedBy());
    eventHeader.setCorrelationId(job.getId());
    event.setHeader(eventHeader);
    event.setPayload(payloadDTO);

    return event;
  }
}
