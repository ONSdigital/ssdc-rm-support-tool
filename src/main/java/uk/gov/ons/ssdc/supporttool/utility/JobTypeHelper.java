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
import uk.gov.ons.ssdc.common.validation.MandatoryRule;
import uk.gov.ons.ssdc.common.validation.Rule;
import uk.gov.ons.ssdc.supporttool.model.dto.messaging.RefusalTypeDTO;
import uk.gov.ons.ssdc.supporttool.transformer.BulkInvalidCaseTransformer;
import uk.gov.ons.ssdc.supporttool.transformer.BulkRefusalTransformer;
import uk.gov.ons.ssdc.supporttool.transformer.NewCaseTransformer;
import uk.gov.ons.ssdc.supporttool.transformer.Transformer;
import uk.gov.ons.ssdc.supporttool.validators.CaseExistsRule;

@Component
public class JobTypeHelper {
  private static final Transformer SAMPLE_LOAD_TRANSFORMER = new NewCaseTransformer();
  private static final Transformer BULK_REFUSAL_TRANSFORMER = new BulkRefusalTransformer();
  private static final Transformer BULK_INVALID_TRANSFORMER = new BulkInvalidCaseTransformer();

  @Value("${queueconfig.shared-pubsub-project}")
  private String sharedPubsubProject;

  @Value("${queueconfig.new-case-topic}")
  private String newCaseTopic;

  @Value("${queueconfig.refusal-event-topic}")
  private String refusalEventTopic;

  @Value("${queueconfig. invalid-case-event-topic}")
  private String invalidCaseTopic;

  public JobTypeSettings getJobTypeSettings(JobType jobType, Survey survey) {
    JobTypeSettings jobTypeSettings = new JobTypeSettings();
    switch (jobType) {
      case SAMPLE:
        jobTypeSettings.setTransformer(SAMPLE_LOAD_TRANSFORMER);
        jobTypeSettings.setColumnValidators(survey.getSampleValidationRules());
        jobTypeSettings.setTopic(toProjectTopicName(newCaseTopic, sharedPubsubProject).toString());
        jobTypeSettings.setFileLoadPermission(UserGroupAuthorisedActivityType.LOAD_SAMPLE);
        jobTypeSettings.setFileViewProgressPersmission(
            UserGroupAuthorisedActivityType.VIEW_SAMPLE_LOAD_PROGRESS);
        return jobTypeSettings;

      case BULK_REFUSAL:
        jobTypeSettings.setTransformer(BULK_REFUSAL_TRANSFORMER);
        jobTypeSettings.setColumnValidators(getBulkRefusalProcessorValidationRules());
        jobTypeSettings.setTopic(
            toProjectTopicName(refusalEventTopic, sharedPubsubProject).toString());
        jobTypeSettings.setFileLoadPermission(UserGroupAuthorisedActivityType.LOAD_BULK_REFUSAL);
        jobTypeSettings.setFileViewProgressPersmission(
            UserGroupAuthorisedActivityType.VIEW_BULK_REFUSAL_PROGRESS);
        return jobTypeSettings;

      case BULK_INVALID:
        jobTypeSettings.setTransformer(BULK_INVALID_TRANSFORMER);
        jobTypeSettings.setColumnValidators(getBulkInvalidCaseValidationRules());
        jobTypeSettings.setTopic(
                toProjectTopicName(invalidCaseTopic, sharedPubsubProject).toString());
        jobTypeSettings.setFileLoadPermission(UserGroupAuthorisedActivityType.LOAD_BULK_INVALID);
        jobTypeSettings.setFileViewProgressPersmission(
                UserGroupAuthorisedActivityType.VIEW_BULK_INVALID_PROGRESS);
        return jobTypeSettings;

      default:
        // This code should be unreachable, providing we have a case for every JobType
        throw new RuntimeException(
            String.format("In getJobTypeSettings the jobType %s wasn't matched", jobType));
    }
  }

  private ColumnValidator[] getBulkInvalidCaseValidationRules() {
    Rule[] caseExistsRules = {new CaseExistsRule()};
    ColumnValidator caseExistsValidator = new ColumnValidator("caseId", false, caseExistsRules);

    Rule[] reasonRule = {new MandatoryRule()};
    ColumnValidator reasonRuleValidator =
            new ColumnValidator("reason", false, reasonRule);

    return new ColumnValidator[] {caseExistsValidator, reasonRuleValidator};
  }

  private ColumnValidator[] getBulkRefusalProcessorValidationRules() {
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
