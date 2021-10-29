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
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import uk.gov.ons.ssdc.common.model.entity.Job;
import uk.gov.ons.ssdc.common.model.entity.JobStatus;
import uk.gov.ons.ssdc.supporttool.model.repository.JobRepository;
import uk.gov.ons.ssdc.supporttool.utility.ColumnHelper;
import uk.gov.ons.ssdc.supporttool.utility.JobTypeHelper;
import uk.gov.ons.ssdc.supporttool.utility.JobTypeSettings;

@Component
public class FileStager {

  private final JobRepository jobRepository;
  private final JobTypeHelper jobTypeHelper;

  @Value("${file-upload-storage-path}")
  private String fileUploadStoragePath;

  public FileStager(JobRepository jobRepository, JobTypeHelper jobTypeHelper) {
    this.jobRepository = jobRepository;
    this.jobTypeHelper = jobTypeHelper;
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
    try (Reader reader = Files.newBufferedReader(Path.of(fileUploadStoragePath + job.getFileId()));
        CSVReader csvReader =
            new CSVReader(reader, job.getCollectionExercise().getSurvey().getSampleSeparator())) {
      JobStatus jobStatus = JobStatus.STAGING_IN_PROGRESS;

      // Validate the header row has the right number of columns
      String[] headerRow = csvReader.readNext();

      JobTypeSettings jobTypeSettings =
          jobTypeHelper.getJobTypeSettings(
              job.getJobType(), job.getCollectionExercise().getSurvey());

      String[] expectedColumns =
          ColumnHelper.getExpectedColumns(jobTypeSettings.getColumnValidators());

      if (headerRow.length != expectedColumns.length) {
        // The header row doesn't have enough columns
        jobStatus = JobStatus.VALIDATED_TOTAL_FAILURE;
        job.setFatalErrorDescription("Header row does not have expected number of columns");
      } else {
        // Validate that the header rows are correct
        for (int index = 0; index < headerRow.length; index++) {
          if (!headerRow[index].equals(expectedColumns[index])) {
            // The header row doesn't match what we expected
            jobStatus = JobStatus.VALIDATED_TOTAL_FAILURE;
            job.setFatalErrorDescription(
                "Header row does not match expected columns, received: ["
                    + headerRow[index]
                    + "] expected: ["
                    + expectedColumns[index]
                    + "]");
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
                new File(fileUploadStoragePath + job.getFileId()).delete();
              }
            });
      }

      return jobStatus;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
