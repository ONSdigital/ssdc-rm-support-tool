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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import uk.gov.ons.ssdc.common.model.entity.Job;
import uk.gov.ons.ssdc.common.model.entity.JobStatus;
import uk.gov.ons.ssdc.supporttool.model.repository.JobRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.JobRowRepository;
import uk.gov.ons.ssdc.supporttool.utility.JobTypeHelper;

@Component
public class RowStager {
  private static final Logger log = LoggerFactory.getLogger(RowStager.class);

  private final JobRepository jobRepository;
  private final JobRowRepository jobRowRepository;
  private final JobTypeHelper jobTypeHelper;
  private final RowChunkStager rowChunkStager;

  @Value("${file-upload-storage-path}")
  private String fileUploadStoragePath;

  private final String hostName = InetAddress.getLocalHost().getHostName();

  public RowStager(
      JobRepository jobRepository,
      JobRowRepository jobRowRepository,
      RowChunkStager rowChunkStager,
      JobTypeHelper jobTypeHelper)
      throws UnknownHostException {
    this.jobRepository = jobRepository;
    this.jobRowRepository = jobRowRepository;
    this.rowChunkStager = rowChunkStager;
    this.jobTypeHelper = jobTypeHelper;
  }

  @Scheduled(fixedDelayString = "1000")
  @Transactional
  public void processRows() throws CsvValidationException {
    List<Job> jobs = jobRepository.findByJobStatus(JobStatus.STAGING_IN_PROGRESS);

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

      processJob(job);
    }
  }

  private void processJob(Job job) throws CsvValidationException {
    CSVParser parser =
        new CSVParserBuilder()
            .withSeparator(job.getCollectionExercise().getSurvey().getSampleSeparator())
            .build();
    try (Reader reader = Files.newBufferedReader(Path.of(fileUploadStoragePath + job.getFileId()));
        CSVReader csvReader = new CSVReaderBuilder(reader).withCSVParser(parser).build()) {

      String[] headerRow;
      int headerRowCorrection = 1;
      if (job.getCollectionExercise().getSurvey().isSampleWithHeaderRow()) {
        headerRow = csvReader.readNext();
      } else {
        headerRow = jobTypeHelper.getExpectedColumns(job.getJobType(), job.getCollectionExercise());
        headerRowCorrection = 0;
      }

      // Skip lines which we don't need, until we reach progress point
      for (int i = 0; i < job.getStagingRowNumber(); i++) {
        csvReader.readNext();
      }

      // Stage all the rows
      JobStatus jobStatus = JobStatus.VALIDATION_IN_PROGRESS;
      while (job.getStagingRowNumber() < job.getFileRowCount() - headerRowCorrection) {
        jobStatus = rowChunkStager.stageChunk(job, headerRow, csvReader);
        if (jobStatus == JobStatus.VALIDATED_TOTAL_FAILURE) {
          break;
        }
      }

      if (jobStatus == JobStatus.VALIDATED_TOTAL_FAILURE) {
        jobRowRepository.deleteByJob(job);
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
      log.atError()
          .setMessage("IOException staging job, CSV data is malformed")
          .addKeyValue("file_id", job.getFileId())
          .addKeyValue("job_id", job.getId())
          .addKeyValue("file_name", job.getFileName())
          .log();

      job.setFatalErrorDescription("Exception Message: " + e.getMessage());
      job.setJobStatus(JobStatus.VALIDATED_TOTAL_FAILURE);
      jobRepository.saveAndFlush(job);
    }
  }
}
