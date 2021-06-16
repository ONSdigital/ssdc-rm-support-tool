package uk.gov.ons.ssdc.supporttool.schedule;

import com.opencsv.CSVReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.ons.ssdc.supporttool.model.entity.Job;
import uk.gov.ons.ssdc.supporttool.model.entity.JobStatus;
import uk.gov.ons.ssdc.supporttool.model.repository.JobRepository;

@Component
public class RowStager {
  private final JobRepository jobRepository;
  private final RowChunkStager rowChunkStager;

  public RowStager(JobRepository jobRepository, RowChunkStager rowChunkStager) {
    this.jobRepository = jobRepository;
    this.rowChunkStager = rowChunkStager;
  }

  @Scheduled(fixedDelayString = "1000")
  @Transactional
  public void processRows() {
    List<Job> jobs = jobRepository.findByJobStatus(JobStatus.STAGING_IN_PROGRESS);

    for (Job job : jobs) {
      JobStatus jobStatus = JobStatus.PROCESSING_IN_PROGRESS;

      try (Reader reader = Files.newBufferedReader(Path.of("/tmp/" + job.getFileId()));
          CSVReader csvReader = new CSVReader(reader)) {
        String[] headerRow = csvReader.readNext();

        // Skip lines which we don't need, until we reach progress point
        for (int i = 0; i < job.getStagingRowNumber(); i++) {
          csvReader.readNext();
        }

        // Stage all the rows
        while (job.getStagingRowNumber() < job.getFileRowCount() - 1) {
          rowChunkStager.stageChunk(job, headerRow, csvReader);
        }

        job.setJobStatus(jobStatus);
        jobRepository.saveAndFlush(job);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
