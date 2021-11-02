package uk.gov.ons.ssdc.supporttool.schedule;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.ons.ssdc.common.model.entity.Job;
import uk.gov.ons.ssdc.common.model.entity.JobRow;
import uk.gov.ons.ssdc.common.model.entity.JobRowStatus;
import uk.gov.ons.ssdc.common.model.entity.JobType;
import uk.gov.ons.ssdc.common.validation.ColumnValidator;
import uk.gov.ons.ssdc.supporttool.model.repository.JobRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.JobRowRepository;
import uk.gov.ons.ssdc.supporttool.utility.JobTypeHelper;
import uk.gov.ons.ssdc.supporttool.utility.JobTypeSettings;

@Component
public class RowChunkValidator {
  private final JobRowRepository jobRowRepository;
  private final JobRepository jobRepository;
  private final JobTypeHelper jobTypeHelper;

  public RowChunkValidator(
      JobRowRepository jobRowRepository, JobRepository jobRepository, JobTypeHelper jobTypeHelper) {
    this.jobRowRepository = jobRowRepository;
    this.jobRepository = jobRepository;
    this.jobTypeHelper = jobTypeHelper;
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public boolean processChunk(Job job) {
    boolean hadErrors = false;

    JobTypeSettings jobTypeSettings =
        jobTypeHelper.getJobTypeSettings(
            job.getJobType(), job.getCollectionExercise().getSurvey(), job);

    List<JobRow> jobRows =
        jobRowRepository.findTop500ByJobAndJobRowStatus(job, JobRowStatus.STAGED);

    boolean getValidationRulesPerRow = false;
    boolean isSensitive = false;
    ColumnValidator[] columnValidators = null;

    if (job.getJobType() == JobType.BULK_UPDATE_SAMPLE) {
      getValidationRulesPerRow = true;
    } else if (job.getJobType() == JobType.BULK_UPDATE_SAMPLE_SENSITIVE) {
      isSensitive = true;
      getValidationRulesPerRow = true;
    } else {
      columnValidators = jobTypeSettings.getColumnValidators();
    }

    for (JobRow jobRow : jobRows) {
      JobRowStatus rowStatus = JobRowStatus.VALIDATED_OK;
      List<String> rowValidationErrors = new LinkedList<>();

      if (getValidationRulesPerRow) {
        String fieldToUpdate = jobRow.getRowData().get("fieldToUpdate");
        columnValidators =
            jobTypeSettings.getColumnValidatorForSampleOrSensitive(
                fieldToUpdate, isSensitive, job.getCollectionExercise());

        if (columnValidators == null) {
          rowStatus = JobRowStatus.VALIDATED_ERROR;
          rowValidationErrors.add(
              String.format("Column %s has no validation rules", fieldToUpdate));
          hadErrors = true;
        }
      }

      for (ColumnValidator columnValidator : columnValidators) {
        Optional<String> columnValidationErrors = columnValidator.validateRow(jobRow.getRowData());

        if (columnValidationErrors.isPresent()) {
          rowStatus = JobRowStatus.VALIDATED_ERROR;
          rowValidationErrors.add(columnValidationErrors.get());
          hadErrors = true;
        }
      }

      if (rowStatus == JobRowStatus.VALIDATED_ERROR) {
        job.setErrorRowCount(job.getErrorRowCount() + 1);
      }

      jobRow.setValidationErrorDescriptions(String.join(", ", rowValidationErrors));
      jobRow.setJobRowStatus(rowStatus);
      job.setValidatingRowNumber(job.getValidatingRowNumber() + 1);
    }

    jobRowRepository.saveAll(jobRows);
    jobRepository.saveAndFlush(job);

    return hadErrors;
  }
}
