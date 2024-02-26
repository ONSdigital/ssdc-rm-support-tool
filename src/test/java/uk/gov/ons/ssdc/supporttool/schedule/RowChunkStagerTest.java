package uk.gov.ons.ssdc.supporttool.schedule;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.ons.ssdc.common.model.entity.*;
import uk.gov.ons.ssdc.supporttool.model.repository.JobRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.JobRowRepository;

@ExtendWith(MockitoExtension.class)
public class RowChunkStagerTest {
  @Mock private JobRepository jobRepository;

  @Mock private JobRowRepository jobRowRepository;

  @Captor private ArgumentCaptor<List<JobRow>> jobRowCaptor;

  @Captor private ArgumentCaptor<Job> jobCaptor;

  @Mock CSVReader csvReader;

  @InjectMocks RowChunkStager underTest;

  @Test
  void testRowChunkStagerFailedWithNullChunk() throws IOException, CsvValidationException {
    Job job = getJob();

    String[] headerRow = {"a", "b", "c"};

    when(csvReader.readNext()).thenReturn(null);

    JobStatus jobStatus = underTest.stageChunk(job, headerRow, csvReader);

    assertThat(jobStatus).isEqualTo(JobStatus.VALIDATED_TOTAL_FAILURE);
    verify(csvReader).readNext();
    verify(jobRepository).saveAndFlush(jobCaptor.capture());
    Job capturedJob = jobCaptor.getValue();
    assertThat(capturedJob.getFatalErrorDescription())
        .isEqualTo(
            "Failed to process job due to an empty chunk, this probably indicates a mismatch between file line count and row count");
  }

  @Test
  void testRowChunkStagerSuccess() throws IOException, CsvValidationException {
    Job job = getJob();

    String[] headerRow = {"a", "b", "c"};

    when(csvReader.readNext()).thenReturn(new String[] {"1", "2", "3"}).thenReturn(null);

    JobStatus jobStatus = underTest.stageChunk(job, headerRow, csvReader);

    assertThat(jobStatus).isEqualTo(JobStatus.VALIDATION_IN_PROGRESS);
    verify(csvReader, times(2)).readNext();
    verify(jobRepository).saveAndFlush(jobCaptor.capture());

    Job capturedJob = jobCaptor.getValue();
    assertThat(capturedJob.getStagingRowNumber()).isEqualTo(1);

    verify(jobRowRepository).saveAll(jobRowCaptor.capture());
    List<JobRow> jobRow = jobRowCaptor.getValue();

    assertThat(jobRow.get(0).getJob()).isEqualTo(capturedJob);
    assertThat(jobRow.get(0).getRowData()).isEqualTo(Map.of("a", "1", "b", "2", "c", "3"));
  }

  @Test
  void testRowChunkStagerFailedWithIOException() throws IOException, CsvValidationException {
    Job job = getJob();

    String[] headerRow = {"a", "b", "c"};

    when(csvReader.readNext()).thenThrow(new IOException());

    JobStatus jobStatus = underTest.stageChunk(job, headerRow, csvReader);

    assertThat(jobStatus).isEqualTo(JobStatus.VALIDATED_TOTAL_FAILURE);
    verify(csvReader).readNext();
  }

  private Job getJob() {
    CollectionExercise collectionExercise = new CollectionExercise();
    Job job = new Job();
    job.setCollectionExercise(collectionExercise);
    job.setJobStatus(JobStatus.FILE_UPLOADED);
    job.setJobType(JobType.SAMPLE);
    job.setFileRowCount(1);
    job.setId(UUID.randomUUID());
    job.setFileId(UUID.randomUUID());
    return job;
  }
}
