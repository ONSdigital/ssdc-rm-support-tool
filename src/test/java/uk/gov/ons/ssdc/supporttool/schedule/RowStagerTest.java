package uk.gov.ons.ssdc.supporttool.schedule;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static uk.gov.ons.ssdc.supporttool.testhelper.ScheduleHelper.*;

import com.opencsv.exceptions.CsvValidationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.ons.ssdc.common.model.entity.*;
import uk.gov.ons.ssdc.supporttool.model.repository.JobRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.JobRowRepository;
import uk.gov.ons.ssdc.supporttool.testhelper.ScheduleHelper;
import uk.gov.ons.ssdc.supporttool.utility.JobTypeHelper;

@ExtendWith(MockitoExtension.class)
public class RowStagerTest {
  @Mock private JobRepository jobRepository;
  @Mock private JobRowRepository jobRowRepository;
  @Mock private JobTypeHelper jobTypeHelper;

  private final String fileUploadStoragePath = "/tmp/";

  @Mock private RowChunkStager rowChunkStager;

  @Captor private ArgumentCaptor<Job> jobCaptor;

  @InjectMocks RowStager underTest;

  @BeforeEach
  public void setup() {
    ReflectionTestUtils.setField(underTest, "fileUploadStoragePath", fileUploadStoragePath);
  }

  @Test
  void testRowStagerSuccess() throws IOException, CsvValidationException {
    // Given
    Job job = ScheduleHelper.getJob("Junk", true, JobStatus.STAGING_IN_PROGRESS);
    job.setStagingRowNumber(1);
    List<Job> jobList = new ArrayList<>();
    jobList.add(job);
    ScheduleHelper.createFile(job, "Junk");
    when(jobRepository.findByJobStatus(JobStatus.STAGING_IN_PROGRESS)).thenReturn(jobList);
    doAnswer(
            (i) -> {
              job.setStagingRowNumber(job.getStagingRowNumber() + 1);
              return JobStatus.VALIDATION_IN_PROGRESS;
            })
        .when(rowChunkStager)
        .stageChunk(eq(job), any(), any());

    // When
    underTest.processRows();

    // Then
    verify(jobRepository).saveAndFlush(jobCaptor.capture());
    Job capturedJob = jobCaptor.getValue();
    assertThat(capturedJob.getJobStatus()).isEqualTo(JobStatus.VALIDATION_IN_PROGRESS);
    assertThat(capturedJob.getStagingRowNumber()).isEqualTo(2);
  }

  @Test
  void testRowStagerFailedToStageChunk() throws IOException, CsvValidationException {
    // Given
    Job job = ScheduleHelper.getJob("Junk", false, JobStatus.STAGING_IN_PROGRESS);
    List<Job> jobList = new ArrayList<>();
    jobList.add(job);
    ScheduleHelper.createFile(job, "Junk");
    when(jobRepository.findByJobStatus(JobStatus.STAGING_IN_PROGRESS)).thenReturn(jobList);
    when(rowChunkStager.stageChunk(eq(job), any(), any()))
        .thenReturn(JobStatus.VALIDATED_TOTAL_FAILURE);

    // When
    underTest.processRows();

    // Then
    verify(jobRepository).saveAndFlush(jobCaptor.capture());
    Job capturedJob = jobCaptor.getValue();
    assertThat(capturedJob.getJobStatus()).isEqualTo(JobStatus.VALIDATED_TOTAL_FAILURE);
    verify(jobRowRepository).deleteByJob(any());
  }

  @Test
  void testRowStagerThrowIOException() throws IOException, CsvValidationException {
    // Given
    Job job = ScheduleHelper.getJob("Junk", true, JobStatus.STAGING_IN_PROGRESS);
    List<Job> jobList = new ArrayList<>();
    jobList.add(job);
    ScheduleHelper.createFile(job, "\"Junk");
    when(jobRepository.findByJobStatus(JobStatus.STAGING_IN_PROGRESS)).thenReturn(jobList);

    // When
    underTest.processRows();

    // Then
    verify(jobRepository).saveAndFlush(jobCaptor.capture());
    Job capturedJob = jobCaptor.getValue();
    assertThat(capturedJob.getJobStatus()).isEqualTo(JobStatus.VALIDATED_TOTAL_FAILURE);
    assertThat(capturedJob.getFatalErrorDescription()).contains("Exception Message");
  }

  @Test
  void testFileStagerFileDoesNotExist() throws CsvValidationException {
    // Given
    Job job = ScheduleHelper.getJob("Junk", true, JobStatus.STAGING_IN_PROGRESS);
    List<Job> jobList = new ArrayList<>();
    jobList.add(job);
    when(jobRepository.findByJobStatus(JobStatus.STAGING_IN_PROGRESS)).thenReturn(jobList);

    // When
    underTest.processRows();

    // Then
    assertThat(job.getJobStatus()).isEqualTo(JobStatus.STAGING_IN_PROGRESS);
  }
}
