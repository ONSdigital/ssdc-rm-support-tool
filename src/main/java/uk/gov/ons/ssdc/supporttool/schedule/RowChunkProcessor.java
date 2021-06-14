package uk.gov.ons.ssdc.supporttool.schedule;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.ons.ssdc.supporttool.model.entity.Job;
import uk.gov.ons.ssdc.supporttool.model.entity.JobRow;
import uk.gov.ons.ssdc.supporttool.model.entity.JobRowStatus;
import uk.gov.ons.ssdc.supporttool.model.repository.JobRowRepository;
import uk.gov.ons.ssdc.supporttool.validation.ColumnValidator;

@Component
public class RowChunkProcessor {

  private final JobRowRepository jobRowRepository;
  private final RabbitTemplate rabbitTemplate;

  public RowChunkProcessor(JobRowRepository jobRowRepository, RabbitTemplate rabbitTemplate) {
    this.jobRowRepository = jobRowRepository;
    this.rabbitTemplate = rabbitTemplate;
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public boolean processChunk(Job job) {
    boolean hadErrors = false;

    List<JobRow> jobRows =
        jobRowRepository.findTop500ByJobAndAndJobRowStatus(job, JobRowStatus.STAGED);

    for (JobRow jobRow : jobRows) {
      JobRowStatus rowStatus = JobRowStatus.PROCESSED_OK;
      List<String> rowValidationErrors = new LinkedList<>();

      for (ColumnValidator columnValidator : job.getBulkProcess().getColumnValidators()) {
        Optional<String> columnValidationErrors = columnValidator.validateRow(jobRow.getRowData());
        if (columnValidationErrors.isPresent()) {
          rowStatus = JobRowStatus.PROCESSED_ERROR;
          rowValidationErrors.add(columnValidationErrors.get());
          hadErrors = true;
        }
      }

      if (rowValidationErrors.size() == 0) {
        rabbitTemplate.convertAndSend(
            job.getBulkProcess().getTargetExchange(),
            job.getBulkProcess().getTargetRoutingKey(),
            job.getBulkProcess().getTransformer().transformRow(jobRow.getRowData()));
      }

      jobRow.setValidationErrorDescriptions(String.join(", ", rowValidationErrors));
      jobRow.setJobRowStatus(rowStatus);
    }

    jobRowRepository.saveAll(jobRows);

    return hadErrors;
  }
}
