package uk.gov.ons.ssdc.supporttool.transformer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import uk.gov.ons.ssdc.common.model.entity.Job;
import uk.gov.ons.ssdc.common.validation.ColumnValidator;
import uk.gov.ons.ssdc.supporttool.model.dto.messaging.EventDTO;
import uk.gov.ons.ssdc.supporttool.model.dto.messaging.EventHeaderDTO;
import uk.gov.ons.ssdc.supporttool.model.dto.messaging.NewCase;
import uk.gov.ons.ssdc.supporttool.model.dto.messaging.PayloadDTO;
import uk.gov.ons.ssdc.supporttool.utility.EventHelper;

public class NewCaseTransformer implements Transformer {
  @Override
  public Object transformRow(
      Map<String, String> rowData, Job job, ColumnValidator[] columnValidators, String topic) {
    NewCase newCase = new NewCase();
    newCase.setCaseId(UUID.randomUUID());
    newCase.setCollectionExerciseId(job.getCollectionExercise().getId());

    Map<String, String> nonSensitiveSampleData = new HashMap<>();
    Map<String, String> sensitiveSampleData = new HashMap<>();

    for (ColumnValidator columnValidator : columnValidators) {
      String columnName = columnValidator.getColumnName();
      String sampleValue = rowData.get(columnName);

      if (columnValidator.isSensitive()) {
        sensitiveSampleData.put(columnName, sampleValue);
      } else {
        nonSensitiveSampleData.put(columnName, sampleValue);
      }
    }

    newCase.setSample(nonSensitiveSampleData);
    newCase.setSampleSensitive(sensitiveSampleData);

    PayloadDTO payloadDTO = new PayloadDTO();
    payloadDTO.setNewCase(newCase);

    EventDTO event = new EventDTO();
    EventHeaderDTO eventHeader = EventHelper.createEventDTO(topic, job.getCreatedBy());
    eventHeader.setCorrelationId(job.getId());
    event.setHeader(eventHeader);
    event.setPayload(payloadDTO);

    return event;
  }
}