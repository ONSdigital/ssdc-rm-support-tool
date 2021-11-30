package uk.gov.ons.ssdc.supporttool.utility;

import java.util.Arrays;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import uk.gov.ons.ssdc.common.model.entity.CollectionExercise;
import uk.gov.ons.ssdc.common.model.entity.JobType;
import uk.gov.ons.ssdc.common.model.entity.UserGroupAuthorisedActivityType;
import uk.gov.ons.ssdc.common.validation.ColumnValidator;
import uk.gov.ons.ssdc.common.validation.Rule;

@Component
public class JobTypeHelper {
  public String[] getExpectedColumns(JobType jobType, CollectionExercise collectionExercise) {
    ColumnValidator[] columnValidators;

    switch (jobType) {
      case SAMPLE:
        columnValidators = collectionExercise.getSurvey().getSampleValidationRules();
        break;

      case BULK_REFUSAL:
        columnValidators =
            new ColumnValidator[] {
              new ColumnValidator("caseId", false, new Rule[0]),
              new ColumnValidator("refusalType", false, new Rule[0])
            };
        break;

      case BULK_INVALID:
        columnValidators =
            new ColumnValidator[] {
              new ColumnValidator("caseId", false, new Rule[0]),
              new ColumnValidator("reason", false, new Rule[0])
            };
        break;

      case BULK_UPDATE_SAMPLE:
      case BULK_UPDATE_SAMPLE_SENSITIVE:
        columnValidators =
            new ColumnValidator[] {
              new ColumnValidator("caseId", false, new Rule[0]),
              new ColumnValidator("fieldToUpdate", false, new Rule[0]),
              new ColumnValidator("newValue", false, new Rule[0])
            };
        break;

      default:
        // This code should be unreachable, providing we have a case for every JobType
        throw new RuntimeException(
            String.format("In getJobTypeSettings the jobType %s wasn't matched", jobType));
    }

    return Arrays.stream(columnValidators)
        .map(columnValidator -> columnValidator.getColumnName())
        .collect(Collectors.toList())
        .toArray(new String[0]);
  }

  public UserGroupAuthorisedActivityType getFileLoadPermission(JobType jobType) {
    switch (jobType) {
      case SAMPLE:
        return UserGroupAuthorisedActivityType.LOAD_SAMPLE;

      case BULK_REFUSAL:
        return UserGroupAuthorisedActivityType.LOAD_BULK_REFUSAL;

      case BULK_INVALID:
        return UserGroupAuthorisedActivityType.LOAD_BULK_INVALID;

      case BULK_UPDATE_SAMPLE:
        return UserGroupAuthorisedActivityType.LOAD_BULK_UPDATE_SAMPLE;

      case BULK_UPDATE_SAMPLE_SENSITIVE:
        return UserGroupAuthorisedActivityType.LOAD_BULK_UPDATE_SAMPLE_SENSITIVE;

      default:
        // This code should be unreachable, providing we have a case for every JobType
        throw new RuntimeException(
            String.format("In getJobTypeSettings the jobType %s wasn't matched", jobType));
    }
  }

  public UserGroupAuthorisedActivityType getFileViewProgressPermission(JobType jobType) {
    switch (jobType) {
      case SAMPLE:
        return UserGroupAuthorisedActivityType.VIEW_SAMPLE_LOAD_PROGRESS;

      case BULK_REFUSAL:
        return UserGroupAuthorisedActivityType.VIEW_BULK_REFUSAL_PROGRESS;

      case BULK_INVALID:
        return UserGroupAuthorisedActivityType.VIEW_BULK_INVALID_PROGRESS;

      case BULK_UPDATE_SAMPLE:
        return UserGroupAuthorisedActivityType.VIEW_BULK_UPDATE_SAMPLE_PROGRESS;

      case BULK_UPDATE_SAMPLE_SENSITIVE:
        return UserGroupAuthorisedActivityType.VIEW_BULK_UPDATE_SAMPLE_SENSITIVE_PROGRESS;

      default:
        // This code should be unreachable, providing we have a case for every JobType
        throw new RuntimeException(
            String.format("In getJobTypeSettings the jobType %s wasn't matched", jobType));
    }
  }
}
