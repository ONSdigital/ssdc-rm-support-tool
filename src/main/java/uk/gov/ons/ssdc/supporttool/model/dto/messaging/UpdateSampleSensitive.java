package uk.gov.ons.ssdc.supporttool.model.dto.messaging;

import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
public class UpdateSampleSensitive {
    private UUID caseId;
    private Map<String, String> sampleSensitive;
}
