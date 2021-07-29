package uk.gov.ons.ssdc.supporttool.schedule;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.ons.ssdc.supporttool.messaging.MessageSender;
import uk.gov.ons.ssdc.supporttool.model.entity.Job;
import uk.gov.ons.ssdc.supporttool.model.entity.JobRow;
import uk.gov.ons.ssdc.supporttool.model.entity.JobRowStatus;
import uk.gov.ons.ssdc.supporttool.model.repository.JobRowRepository;
import uk.gov.ons.ssdc.supporttool.transformer.SampleTransformer;
import uk.gov.ons.ssdc.supporttool.transformer.Transformer;
import uk.gov.ons.ssdc.supporttool.validation.ColumnValidator;

@Component
public class RowChunkProcessor {
  private static final Transformer TRANSFORMER = new SampleTransformer();

  private final JobRowRepository jobRowRepository;
  private final MessageSender messageSender;

  @Value("${queueconfig.sample-topic}")
  private String sampleTopic;

  public RowChunkProcessor(JobRowRepository jobRowRepository, MessageSender messageSender) {
    this.jobRowRepository = jobRowRepository;
    this.messageSender = messageSender;
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public boolean processChunk(Job job) {
    boolean hadErrors = false;
    ColumnValidator[] columnValidators =
        job.getCollectionExercise().getSurvey().getSampleValidationRules();

    List<JobRow> jobRows =
        jobRowRepository.findTop500ByJobAndAndJobRowStatus(job, JobRowStatus.STAGED);

    for (JobRow jobRow : jobRows) {
      JobRowStatus rowStatus = JobRowStatus.PROCESSED_OK;
      List<String> rowValidationErrors = new LinkedList<>();

      for (ColumnValidator columnValidator : columnValidators) {
        Optional<String> columnValidationErrors = columnValidator.validateRow(jobRow.getRowData());
        if (columnValidationErrors.isPresent()) {
          rowStatus = JobRowStatus.PROCESSED_ERROR;
          rowValidationErrors.add(columnValidationErrors.get());
          hadErrors = true;
        }
      }

      if (rowValidationErrors.size() == 0) {
        messageSender.sendMessage(
            sampleTopic, TRANSFORMER.transformRow(jobRow.getRowData(), job, columnValidators));
      }

      jobRow.setValidationErrorDescriptions(String.join(", ", rowValidationErrors));
      jobRow.setJobRowStatus(rowStatus);
    }

    jobRowRepository.saveAll(jobRows);

    return hadErrors;
  }
}
