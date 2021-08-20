package uk.gov.ons.ssdc.supporttool.schedule;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.ons.ssdc.supporttool.model.entity.Job;
import uk.gov.ons.ssdc.supporttool.model.entity.JobRow;
import uk.gov.ons.ssdc.supporttool.model.entity.JobRowStatus;
import uk.gov.ons.ssdc.supporttool.model.repository.JobRowRepository;
import uk.gov.ons.ssdc.supporttool.transformer.SampleTransformer;
import uk.gov.ons.ssdc.supporttool.transformer.Transformer;
import uk.gov.ons.ssdc.supporttool.validation.ColumnValidator;

@Component
public class RowChunkProcessor {
  private static final Logger log = LoggerFactory.getLogger(RowChunkProcessor.class);
  private static final Transformer TRANSFORMER = new SampleTransformer();

  private final JobRowRepository jobRowRepository;
  private final PubSubTemplate pubSubTemplate;

  @Value("${queueconfig.sample-topic}")
  private String sampleTopic;

  public RowChunkProcessor(JobRowRepository jobRowRepository, PubSubTemplate pubSubTemplate) {
    this.jobRowRepository = jobRowRepository;
    this.pubSubTemplate = pubSubTemplate;
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

      boolean retryBecausePubsubFailed = false;

      if (rowValidationErrors.size() == 0) {
        try {
          pubSubTemplate.publish(
              sampleTopic, TRANSFORMER.transformRow(jobRow.getRowData(), job, columnValidators));
        } catch (Exception e) {
          retryBecausePubsubFailed = true;

          log.with(jobRow).error("Failed to send message to pubsub", e);
        }
      }

      // If we had a problem with pubsub, don't bother changing the status of the row, so that we
      // will keep retrying to send it, indefinitely
      if (!retryBecausePubsubFailed) {
        jobRow.setValidationErrorDescriptions(String.join(", ", rowValidationErrors));
        jobRow.setJobRowStatus(rowStatus);
      }
    }

    jobRowRepository.saveAll(jobRows);

    return hadErrors;
  }
}
