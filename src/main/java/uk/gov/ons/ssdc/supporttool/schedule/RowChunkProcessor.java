package uk.gov.ons.ssdc.supporttool.schedule;

import static com.google.cloud.spring.pubsub.support.PubSubTopicUtils.toProjectTopicName;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.concurrent.ListenableFuture;
import uk.gov.ons.ssdc.common.model.entity.Job;
import uk.gov.ons.ssdc.common.model.entity.JobRow;
import uk.gov.ons.ssdc.common.model.entity.JobRowStatus;
import uk.gov.ons.ssdc.common.validation.ColumnValidator;
import uk.gov.ons.ssdc.supporttool.model.repository.JobRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.JobRowRepository;
import uk.gov.ons.ssdc.supporttool.transformer.SampleTransformer;
import uk.gov.ons.ssdc.supporttool.transformer.Transformer;

@Component
public class RowChunkProcessor {
  private static final Logger log = LoggerFactory.getLogger(RowChunkProcessor.class);
  private static final Transformer TRANSFORMER = new SampleTransformer();

  private final JobRowRepository jobRowRepository;
  private final PubSubTemplate pubSubTemplate;
  private final JobRepository jobRepository;

  @Value("${queueconfig.shared-pubsub-project}")
  private String sharedPubsubProject;

  @Value("${queueconfig.new-case-topic}")
  private String newCaseTopic;

  public RowChunkProcessor(
      JobRowRepository jobRowRepository,
      PubSubTemplate pubSubTemplate,
      JobRepository jobRepository) {
    this.jobRowRepository = jobRowRepository;
    this.pubSubTemplate = pubSubTemplate;
    this.jobRepository = jobRepository;
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public boolean processChunk(Job job) {
    boolean hadErrors = false;
    ColumnValidator[] columnValidators =
        job.getCollectionExercise().getSurvey().getSampleValidationRules();

    List<JobRow> jobRows =
        jobRowRepository.findTop500ByJobAndAndJobRowStatus(job, JobRowStatus.VALIDATED_OK);

    for (JobRow jobRow : jobRows) {
      try {
        String topic = toProjectTopicName(newCaseTopic, sharedPubsubProject).toString();

        ListenableFuture<String> future =
            pubSubTemplate.publish(
                topic, TRANSFORMER.transformRow(jobRow.getRowData(), job, columnValidators));

        // Wait for up to 30 seconds to confirm that message was published
        future.get(30, TimeUnit.SECONDS);

        jobRow.setJobRowStatus(JobRowStatus.PROCESSED);
        job.setProcessingRowNumber(job.getProcessingRowNumber() + 1);
      } catch (Exception e) {
        // The message sending will be retried...
        log.with("job ID", job.getId())
            .with("row ID", jobRow.getId())
            .error("Failed to send message to pubsub", e);
      }
    }

    jobRepository.save(job);
    jobRowRepository.saveAll(jobRows);

    return hadErrors;
  }
}
