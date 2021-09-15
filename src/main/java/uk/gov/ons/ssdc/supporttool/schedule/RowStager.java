package uk.gov.ons.ssdc.supporttool.schedule;

import com.opencsv.CSVReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import uk.gov.ons.ssdc.common.model.entity.Job;
import uk.gov.ons.ssdc.common.model.entity.JobRow;
import uk.gov.ons.ssdc.common.model.entity.JobStatus;
import uk.gov.ons.ssdc.supporttool.model.repository.JobRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.JobRowRepository;
import uk.gov.ons.ssdc.supporttool.utility.SampleColumnHelper;

@Component
public class RowStager {
  private final JobRepository jobRepository;
  private final JobRowRepository jobRowRepository;
  private final RowChunkStager rowChunkStager;

  @Value("${file-upload-storage-path}")
  private String fileUploadStoragePath;

  public RowStager(
      JobRepository jobRepository,
      JobRowRepository jobRowRepository,
      RowChunkStager rowChunkStager) {
    this.jobRepository = jobRepository;
    this.jobRowRepository = jobRowRepository;
    this.rowChunkStager = rowChunkStager;
  }

  @Scheduled(fixedDelayString = "1000")
  @Transactional
  public void processRows() {
    List<Job> jobs = jobRepository.findByJobStatus(JobStatus.STAGING_IN_PROGRESS);

    for (Job job : jobs) {
      try (Reader reader =
              Files.newBufferedReader(Path.of(fileUploadStoragePath + job.getFileId()));
          CSVReader csvReader =
              new CSVReader(reader, job.getCollectionExercise().getSurvey().getSampleSeparator())) {

        String[] headerRow;
        if (job.getCollectionExercise().getSurvey().isSampleWithHeaderRow()) {
          headerRow = csvReader.readNext();
        } else {
          headerRow = SampleColumnHelper.getExpectedColumns(job);
        }

        // Skip lines which we don't need, until we reach progress point
        for (int i = 0; i < job.getStagingRowNumber(); i++) {
          csvReader.readNext();
        }

        // Stage all the rows
        JobStatus jobStatus = JobStatus.VALIDATION_IN_PROGRESS;
        while (job.getStagingRowNumber() < job.getFileRowCount() - 1) {
          jobStatus = rowChunkStager.stageChunk(job, headerRow, csvReader);
          if (jobStatus == JobStatus.VALIDATED_TOTAL_FAILURE) {
            break;
          }
        }

        if (jobStatus == JobStatus.VALIDATED_TOTAL_FAILURE) {
          List<JobRow> allJobRows = jobRowRepository.findByJob(job);
          jobRowRepository.deleteAllInBatch(allJobRows);
        }

        job.setJobStatus(jobStatus);
        jobRepository.saveAndFlush(job);

        // The file is now fully staged, or we got a fatal error, so we can delete the file
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
          TransactionSynchronizationManager.registerSynchronization(
              new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                  new File(fileUploadStoragePath + job.getFileId()).delete();
                }
              });
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
