package uk.gov.ons.ssdc.supporttool.schedule;

import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.ons.ssdc.supporttool.model.entity.Job;
import uk.gov.ons.ssdc.supporttool.model.entity.JobRowStatus;
import uk.gov.ons.ssdc.supporttool.model.entity.JobStatus;
import uk.gov.ons.ssdc.supporttool.model.repository.JobRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.JobRowRepository;

@Component
public class StagedJobProcessor {

  private final JobRepository jobRepository;
  private final JobRowRepository jobRowRepository;
  private final RowChunkProcessor rowChunkProcessor;

  public StagedJobProcessor(
      JobRepository jobRepository,
      JobRowRepository jobRowRepository,
      RowChunkProcessor rowChunkProcessor) {
    this.jobRepository = jobRepository;
    this.jobRowRepository = jobRowRepository;
    this.rowChunkProcessor = rowChunkProcessor;
  }

  @Scheduled(fixedDelayString = "1000")
  @Transactional
  public void processStagedJobs() {
    List<Job> jobs = jobRepository.findByJobStatus(JobStatus.PROCESSING_IN_PROGRESS);

    for (Job job : jobs) {
      JobStatus jobStatus = JobStatus.PROCESSED_OK;

      while (jobRowRepository.existsByJobAndAndJobRowStatus(job, JobRowStatus.STAGED)) {
        if (rowChunkProcessor.processChunk(job)) {
          jobStatus = JobStatus.PROCESSED_WITH_ERRORS;
        }
      }

      job.setJobStatus(jobStatus);
      jobRepository.saveAndFlush(job);
    }
  }
}
