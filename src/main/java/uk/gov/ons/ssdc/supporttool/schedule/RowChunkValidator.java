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
import uk.gov.ons.ssdc.common.validation.ColumnValidator;
import uk.gov.ons.ssdc.supporttool.model.repository.JobRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.JobRowRepository;

@Component
public class RowChunkValidator {
  private final JobRowRepository jobRowRepository;
  private final JobRepository jobRepository;

  public RowChunkValidator(JobRowRepository jobRowRepository, JobRepository jobRepository) {
    this.jobRowRepository = jobRowRepository;
    this.jobRepository = jobRepository;
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public boolean processChunk(Job job) {
    boolean hadErrors = false;
    ColumnValidator[] columnValidators =
        job.getCollectionExercise().getSurvey().getSampleValidationRules();

    List<JobRow> jobRows =
        jobRowRepository.findTop500ByJobAndAndJobRowStatus(job, JobRowStatus.STAGED);

    for (JobRow jobRow : jobRows) {
      JobRowStatus rowStatus = JobRowStatus.VALIDATED_OK;
      List<String> rowValidationErrors = new LinkedList<>();

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