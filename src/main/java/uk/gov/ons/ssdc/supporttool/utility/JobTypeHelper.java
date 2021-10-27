package uk.gov.ons.ssdc.supporttool.utility;

import static com.google.cloud.spring.pubsub.support.PubSubTopicUtils.toProjectTopicName;

import java.util.EnumSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.ons.ssdc.common.model.entity.JobType;
import uk.gov.ons.ssdc.common.model.entity.Survey;
import uk.gov.ons.ssdc.common.model.entity.UserGroupAuthorisedActivityType;
import uk.gov.ons.ssdc.common.validation.ColumnValidator;
import uk.gov.ons.ssdc.common.validation.InSetRule;
import uk.gov.ons.ssdc.common.validation.Rule;
import uk.gov.ons.ssdc.supporttool.model.dto.messaging.RefusalTypeDTO;
import uk.gov.ons.ssdc.supporttool.transformer.BulkRefusalTransformer;
import uk.gov.ons.ssdc.supporttool.transformer.NewCaseTransformer;
import uk.gov.ons.ssdc.supporttool.transformer.Transformer;
import uk.gov.ons.ssdc.supporttool.validators.CaseExistsRule;

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

  public JobTypeSettings getJobTypeSettings(JobType jobType, Survey survey) {
    JobTypeSettings jobTypeSettings = new JobTypeSettings();
    switch (jobType) {
      case SAMPLE:
        jobTypeSettings.setTransformer(SAMPLE_LOAD_TRANSFORMER);
        jobTypeSettings.setColumnValidators(survey.getSampleValidationRules());
        jobTypeSettings.setTopic(toProjectTopicName(newCaseTopic, sharedPubsubProject).toString());
        jobTypeSettings.setExpectedColumns(SampleColumnHelper.getExpectedColumns(survey));
        jobTypeSettings.setFileLoadPermission(UserGroupAuthorisedActivityType.LOAD_SAMPLE);
        jobTypeSettings.setFileViewProgressPersmission(
            UserGroupAuthorisedActivityType.VIEW_SAMPLE_LOAD_PROGRESS);
        return jobTypeSettings;

      case BULK_REFUSAL:
        jobTypeSettings.setTransformer(BULK_REFUSAL_TRANSFORMER);
        jobTypeSettings.setColumnValidators(getBulkProcessorValidationRules());
        jobTypeSettings.setTopic(
            toProjectTopicName(refusalEventTopic, sharedPubsubProject).toString());
        jobTypeSettings.setExpectedColumns(new String[] {"caseId", "refusalType"});
        jobTypeSettings.setFileLoadPermission(UserGroupAuthorisedActivityType.LOAD_BULK_REFUSAL);
        jobTypeSettings.setFileViewProgressPersmission(
            UserGroupAuthorisedActivityType.VIEW_BULK_REFUSAL_PROGRESS);
        return jobTypeSettings;

      default:
        //    This code should be unreachable, providing we have a case for every JobType
        throw new RuntimeException(
            String.format(
                "In getTransformerValidationAndTopic the jobType %s wasn't matched", jobType));
    }
  }

  private ColumnValidator[] getBulkProcessorValidationRules() {
    Rule[] caseExistsRules = {new CaseExistsRule()};
    ColumnValidator caseExistsValidator = new ColumnValidator("caseId", false, caseExistsRules);

    String[] refusalTypes =
        EnumSet.allOf(RefusalTypeDTO.class).stream().map(Enum::toString).toArray(String[]::new);
    Rule[] refusalSetRules = {new InSetRule(refusalTypes)};

    ColumnValidator refusalTypeValidator =
        new ColumnValidator("refusalType", false, refusalSetRules);

    return new ColumnValidator[] {caseExistsValidator, refusalTypeValidator};
  }
}