package uk.gov.ons.ssdc.supporttool.schedule;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
import uk.gov.ons.ssdc.supporttool.utility.ObjectMapperFactory;
import uk.gov.ons.ssdc.supporttool.validation.ColumnValidator;

@Component
public class RowChunkProcessor {
  private static final Transformer TRANSFORMER = new SampleTransformer();
  private static final ObjectMapper OBJECT_MAPPER = ObjectMapperFactory.objectMapper();

  private final JobRowRepository jobRowRepository;
  private final RabbitTemplate rabbitTemplate;

  @Value("${queueconfig.sample-queue}")
  private String sampleQueue;

  public RowChunkProcessor(JobRowRepository jobRowRepository, RabbitTemplate rabbitTemplate) {
    this.jobRowRepository = jobRowRepository;
    this.rabbitTemplate = rabbitTemplate;
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public boolean processChunk(Job job) {
    boolean hadErrors = false;
    ColumnValidator[] columnValidators;

    try {
      columnValidators = OBJECT_MAPPER
          .readValue(job.getCollectionExercise().getSurvey().getSampleValidationRules(),
              ColumnValidator[].class);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Validation JSON could not be unmarshalled", e);
    }

    List<JobRow> jobRows =
        jobRowRepository.findTop500ByJobAndAndJobRowStatus(job, JobRowStatus.STAGED);

    for (JobRow jobRow : jobRows) {
      JobRowStatus rowStatus = JobRowStatus.PROCESSED_OK;
      List<String> rowValidationErrors = new LinkedList<>();

      for (ColumnValidator columnValidator :
          columnValidators) {
        Optional<String> columnValidationErrors = columnValidator.validateRow(jobRow.getRowData());
        if (columnValidationErrors.isPresent()) {
          rowStatus = JobRowStatus.PROCESSED_ERROR;
          rowValidationErrors.add(columnValidationErrors.get());
          hadErrors = true;
        }
      }

      if (rowValidationErrors.size() == 0) {
        rabbitTemplate.convertAndSend(
            "", // default exchange (i.e. direct to queue)
            sampleQueue,
            TRANSFORMER.transformRow(jobRow.getRowData(), job));
      }

      jobRow.setValidationErrorDescriptions(String.join(", ", rowValidationErrors));
      jobRow.setJobRowStatus(rowStatus);
    }

    jobRowRepository.saveAll(jobRows);

    return hadErrors;
  }
}
