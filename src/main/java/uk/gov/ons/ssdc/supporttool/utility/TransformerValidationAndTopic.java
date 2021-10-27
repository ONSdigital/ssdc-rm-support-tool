package uk.gov.ons.ssdc.supporttool.utility;

import lombok.AllArgsConstructor;
import lombok.Data;
import uk.gov.ons.ssdc.common.validation.ColumnValidator;
import uk.gov.ons.ssdc.supporttool.transformer.Transformer;

@Data
@AllArgsConstructor
public class TransformerValidationAndTopic {
    private Transformer transformer;
    private ColumnValidator[] columnValidators;
    private String topic;
}
