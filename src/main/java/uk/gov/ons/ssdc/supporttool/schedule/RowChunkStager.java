package uk.gov.ons.ssdc.supporttool.schedule;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.opencsv.CSVReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.ons.ssdc.common.model.entity.Job;
import uk.gov.ons.ssdc.common.model.entity.JobRow;
import uk.gov.ons.ssdc.common.model.entity.JobRowStatus;
import uk.gov.ons.ssdc.common.model.entity.JobStatus;
import uk.gov.ons.ssdc.supporttool.model.repository.JobRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.JobRowRepository;

@Component
public class RowChunkStager {
  private static final Logger log = LoggerFactory.getLogger(RowChunkStager.class);
  private final int CHUNK_SIZE = 500;
  private final JobRepository jobRepository;
  private final JobRowRepository jobRowRepository;

  public RowChunkStager(JobRepository jobRepository, JobRowRepository jobRowRepository) {
    this.jobRepository = jobRepository;
    this.jobRowRepository = jobRowRepository;
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public JobStatus stageChunk(Job job, String[] headerRow, CSVReader csvReader) {
    JobStatus jobStatus = JobStatus.VALIDATION_IN_PROGRESS;

    try {
      List<JobRow> jobRows = new LinkedList<>();

      for (int i = 0; i < CHUNK_SIZE; i++) {
        String[] line = csvReader.readNext();
        if (line == null && i == 0) {
          log.error(
              "Error staging job row, next line in csvreader is null but it's the first iteration. This shouldn't be possible so must be something wrong with the file");
          return JobStatus.VALIDATED_TOTAL_FAILURE;
        } else if (line == null) {
          break;
        }

        if (line.length != headerRow.length) {
          jobStatus = JobStatus.VALIDATED_TOTAL_FAILURE;
          job.setFatalErrorDescription("CSV corrupt: row data does not match columns");
          break;
        }

        Map<String, String> rowData = new HashMap<>();

        JobRow jobRow = new JobRow();
        jobRow.setId(UUID.randomUUID());
        jobRow.setJob(job);
        jobRow.setJobRowStatus(JobRowStatus.STAGED);

        for (int index = 0; index < line.length; index++) {
          rowData.put(headerRow[index], line[index]);
        }

        jobRow.setRowData(rowData);
        jobRow.setOriginalRowData(line);

        job.setStagingRowNumber(job.getStagingRowNumber() + 1);
        jobRow.setOriginalRowLineNumber(job.getStagingRowNumber());
        jobRows.add(jobRow);
      }

      jobRepository.saveAndFlush(job);
      jobRowRepository.saveAll(jobRows);
    } catch (IOException e) {
      log.with("Lines read", csvReader.getRecordsRead())
          .error("Error staging job row, CSV data is malformed");
      return JobStatus.VALIDATED_TOTAL_FAILURE;
    }

    return jobStatus;
  }
}
