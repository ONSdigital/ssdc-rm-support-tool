package uk.gov.ons.ssdc.supporttool.schedule;

import com.opencsv.CSVReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import uk.gov.ons.ssdc.supporttool.model.entity.Job;
import uk.gov.ons.ssdc.supporttool.model.entity.JobStatus;
import uk.gov.ons.ssdc.supporttool.model.repository.JobRepository;
import uk.gov.ons.ssdc.supporttool.utility.SampleColumnHelper;

@Component
public class FileStager {

  private final JobRepository jobRepository;

  public FileStager(JobRepository jobRepository) {
    this.jobRepository = jobRepository;
  }

  @Scheduled(fixedDelayString = "1000")
  public void processFiles() {
    List<Job> jobs = jobRepository.findByJobStatus(JobStatus.FILE_UPLOADED);

    for (Job job : jobs) {

      JobStatus jobStatus = JobStatus.STAGING_IN_PROGRESS;

      if (job.getCollectionExercise().getSurvey().isSampleWithHeaderRow()) {
        jobStatus = checkHeaderRow(job);
      }

      job.setJobStatus(jobStatus);
      jobRepository.saveAndFlush(job);
    }
  }

  private JobStatus checkHeaderRow(Job job) {
    try (Reader reader = Files.newBufferedReader(Path.of("/tmp/" + job.getFileId()));
        CSVReader csvReader =
            new CSVReader(reader, job.getCollectionExercise().getSurvey().getSampleSeparator())) {
      JobStatus jobStatus = JobStatus.STAGING_IN_PROGRESS;

      // Validate the header row has the right number of columns
      String[] headerRow = csvReader.readNext();
      String[] expectedColumns = SampleColumnHelper.getExpectedColumns(job);
      if (headerRow.length != expectedColumns.length) {
        // The header row doesn't have enough columns
        jobStatus = JobStatus.PROCESSED_TOTAL_FAILURE;
        job.setFatalErrorDescription("Header row does not have expected number of columns");
      } else {
        // Validate that the header rows are correct
        for (int index = 0; index < headerRow.length; index++) {
          if (!headerRow[index].equals(expectedColumns[index])) {
            // The header row doesn't match what we expected
            jobStatus = JobStatus.PROCESSED_TOTAL_FAILURE;
            job.setFatalErrorDescription("Header row does not match expected columns");
          }
        }
      }

      // We got a fatal error, so we can delete the file
      if (jobStatus != JobStatus.STAGING_IN_PROGRESS
          && TransactionSynchronizationManager.isSynchronizationActive()) {
        TransactionSynchronizationManager.registerSynchronization(
            new TransactionSynchronization() {
              @Override
              public void afterCompletion(int status) {
                new File("/tmp/" + job.getFileId()).delete();
              }
            });
      }

      return jobStatus;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
