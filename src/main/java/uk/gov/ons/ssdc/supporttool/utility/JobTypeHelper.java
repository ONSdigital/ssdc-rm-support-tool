package uk.gov.ons.ssdc.supporttool.utility;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.ons.ssdc.common.model.entity.Job;
import uk.gov.ons.ssdc.common.validation.ColumnValidator;
import uk.gov.ons.ssdc.common.validation.InSetRule;
import uk.gov.ons.ssdc.common.validation.Rule;
import uk.gov.ons.ssdc.supporttool.model.dto.messaging.RefusalTypeDTO;
import uk.gov.ons.ssdc.supporttool.transformer.BulkRefusalTransformer;
import uk.gov.ons.ssdc.supporttool.transformer.NewCaseTransformer;
import uk.gov.ons.ssdc.supporttool.transformer.Transformer;
import uk.gov.ons.ssdc.supporttool.validators.CaseExistsRule;

import java.util.EnumSet;

import static com.google.cloud.spring.pubsub.support.PubSubTopicUtils.toProjectTopicName;

@Component
public class JobTypeHelper {
  private static final Transformer SAMPLE_LOAD_TRANSFORMER = new NewCaseTransformer();
  private static final Transformer BULK_REFUSAL_TRANSFORMER = new BulkRefusalTransformer();

  @Value("${queueconfig.shared-pubsub-project}")
  private String sharedPubsubProject;

  @Value("${queueconfig.new-case-topic}")
  private String newCaseTopic;

  @Value("${queueconfig.refusal-event-topic}")
  private String refusalEventTopic;

  public TransformerValidationAndTopic getTransformerValidationAndTopic(Job job) {
    switch (job.getJobType()) {
      case SAMPLE:
          return new TransformerValidationAndTopic(SAMPLE_LOAD_TRANSFORMER,
                  job.getCollectionExercise().getSurvey().getSampleValidationRules(),
                  toProjectTopicName(newCaseTopic, sharedPubsubProject).toString()
                  );
      case BULK_REFUSAL:
          return new TransformerValidationAndTopic(BULK_REFUSAL_TRANSFORMER,
                  getBulkProcessorValidationRules(),
                  toProjectTopicName(refusalEventTopic, sharedPubsubProject).toString());

      default:
        //    This code should be unreachable, providing we have a case for every JobType
        throw new RuntimeException(String.format("In getTransformerValidationAndTopic the jobType %s wasn't matched",
                job.getJobType());
    }
  }

  private ColumnValidator[] getBulkProcessorValidationRules() {
    Rule[] caseExistsRules = {new CaseExistsRule()};
    ColumnValidator caseExistsValidator = new ColumnValidator("caseId", false, caseExistsRules);

    String[] refusalTypes = EnumSet.allOf(RefusalTypeDTO.class).stream().map(Enum::toString).toArray(String[]::new);
    Rule[] refusalSetRules = {new InSetRule(refusalTypes)};

    ColumnValidator refusalTypeValidator =
        new ColumnValidator("RefusalType", false, refusalSetRules);

    return new ColumnValidator[]{caseExistsValidator, refusalTypeValidator};
  }
}
