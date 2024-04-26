package uk.gov.ons.ssdc.supporttool.schedule;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import uk.gov.ons.ssdc.common.model.entity.Job;
import uk.gov.ons.ssdc.common.model.entity.JobStatus;
import uk.gov.ons.ssdc.supporttool.model.repository.JobRepository;
import uk.gov.ons.ssdc.supporttool.utility.JobTypeHelper;

@Component
public class FileStager {
  private static final Logger log = LoggerFactory.getLogger(FileStager.class);

  private final JobRepository jobRepository;
  private final JobTypeHelper jobTypeHelper;

  @Value("${file-upload-storage-path}")
  private String fileUploadStoragePath;

  private final String hostName = InetAddress.getLocalHost().getHostName();

  public FileStager(JobRepository jobRepository, JobTypeHelper jobTypeHelper)
      throws UnknownHostException {
    this.jobRepository = jobRepository;
    this.jobTypeHelper = jobTypeHelper;
  }

  @Scheduled(fixedDelayString = "1000")
  public void processFiles() throws CsvValidationException {
    List<Job> jobs = jobRepository.findByJobStatus(JobStatus.FILE_UPLOADED);

    for (Job job : jobs) {
      String filePath = fileUploadStoragePath + job.getFileId();
      if (!new File(filePath).exists()) {
        log.atInfo()
            .setMessage(
                "File can't be seen by this host; probably being handled by a different host")
            .addKeyValue("filePath", filePath)
            .addKeyValue("hostName", hostName)
            .log();
        continue; // Skip this job... hopefully another host (pod) is handling it
      }

      JobStatus jobStatus = JobStatus.STAGING_IN_PROGRESS;

      if (job.getCollectionExercise().getSurvey().isSampleWithHeaderRow()) {
        jobStatus = checkHeaderRow(job);
      }

      job.setJobStatus(jobStatus);
      jobRepository.saveAndFlush(job);
    }
  }

  private JobStatus checkHeaderRow(Job job) throws CsvValidationException {
    CSVParser parser =
        new CSVParserBuilder()
            .withSeparator(job.getCollectionExercise().getSurvey().getSampleSeparator())
            .build();
    try (Reader reader = Files.newBufferedReader(Path.of(fileUploadStoragePath + job.getFileId()));
        CSVReader csvReader = new CSVReaderBuilder(reader).withCSVParser(parser).build()) {
      JobStatus jobStatus = JobStatus.STAGING_IN_PROGRESS;

      // Validate the header row has the right number of columns
      String[] headerRow = csvReader.readNext();

      String[] expectedColumns =
          jobTypeHelper.getExpectedColumns(job.getJobType(), job.getCollectionExercise());

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
      log.atError()
          .setMessage("IOException checking header row, CSV data is malformed")
          .addKeyValue("file_id", job.getFileId())
          .addKeyValue("job_id", job.getId())
          .addKeyValue("file_name", job.getFileName())
          .log();

      job.setFatalErrorDescription("Exception Message: " + e.getMessage());
      job.setJobStatus(JobStatus.VALIDATED_TOTAL_FAILURE);
      return JobStatus.VALIDATED_TOTAL_FAILURE;
    }
  }
}
