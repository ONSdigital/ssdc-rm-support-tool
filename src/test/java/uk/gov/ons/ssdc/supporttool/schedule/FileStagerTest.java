package uk.gov.ons.ssdc.supporttool.schedule;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

import com.opencsv.exceptions.CsvValidationException;
import java.io.*;
import java.util.*;
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
import uk.gov.ons.ssdc.supporttool.testhelper.ScheduleHelper;
import uk.gov.ons.ssdc.supporttool.utility.JobTypeHelper;

@ExtendWith(MockitoExtension.class)
public class FileStagerTest {
  @Mock private JobRepository jobRepository;

  private final String fileUploadStoragePath = "/tmp/";

  @Mock private JobTypeHelper jobTypeHelper;

  @Captor private ArgumentCaptor<Job> jobCaptor;

  @InjectMocks FileStager underTest;

  @BeforeEach
  public void setup() {
    ReflectionTestUtils.setField(underTest, "fileUploadStoragePath", fileUploadStoragePath);
  }

  @Test
  void testFileStagerSuccess() throws IOException, CsvValidationException {
    // Given
    Job job = ScheduleHelper.getJob("Junk", true, JobStatus.FILE_UPLOADED);
    List<Job> jobList = new ArrayList<>();
    jobList.add(job);
    ScheduleHelper.createFile(job, "Junk");
    when(jobRepository.findByJobStatus(JobStatus.FILE_UPLOADED)).thenReturn(jobList);
    when(jobTypeHelper.getExpectedColumns(job.getJobType(), job.getCollectionExercise()))
        .thenReturn(new String[] {"Junk"});

    // When
    underTest.processFiles();

    // Then
    verify(jobRepository).saveAndFlush(jobCaptor.capture());
    Job capturedJob = jobCaptor.getValue();
    assertThat(capturedJob.getJobStatus()).isEqualTo(JobStatus.STAGING_IN_PROGRESS);
  }

  @Test
  void testFileStagerUnexpectedColumnName() throws IOException, CsvValidationException {
    // Given
    Job job = ScheduleHelper.getJob("Junk", true, JobStatus.FILE_UPLOADED);
    List<Job> jobList = new ArrayList<>();
    jobList.add(job);
    ScheduleHelper.createFile(job, "Junk");
    when(jobRepository.findByJobStatus(JobStatus.FILE_UPLOADED)).thenReturn(jobList);
    when(jobTypeHelper.getExpectedColumns(job.getJobType(), job.getCollectionExercise()))
        .thenReturn(new String[] {"Funk"});

    // When
    underTest.processFiles();

    // Then
    verify(jobRepository).saveAndFlush(jobCaptor.capture());
    Job capturedJob = jobCaptor.getValue();
    assertThat(capturedJob.getJobStatus()).isEqualTo(JobStatus.VALIDATED_TOTAL_FAILURE);
  }

  @Test
  void testFileStagerMismatchColumns() throws IOException, CsvValidationException {
    // Given
    Job job = ScheduleHelper.getJob("Junk", true, JobStatus.FILE_UPLOADED);
    List<Job> jobList = new ArrayList<>();
    jobList.add(job);
    ScheduleHelper.createFile(job, "Junk");
    when(jobRepository.findByJobStatus(JobStatus.FILE_UPLOADED)).thenReturn(jobList);
    when(jobTypeHelper.getExpectedColumns(job.getJobType(), job.getCollectionExercise()))
        .thenReturn(new String[] {"Junk", "Funk"});

    // When
    underTest.processFiles();

    // Then
    verify(jobRepository).saveAndFlush(jobCaptor.capture());
    Job capturedJob = jobCaptor.getValue();
    assertThat(capturedJob.getJobStatus()).isEqualTo(JobStatus.VALIDATED_TOTAL_FAILURE);
    assertThat(capturedJob.getFatalErrorDescription())
        .isEqualTo("Header row does not have expected number of columns");
  }

  @Test
  void testRowChunkStagerFailedWithIOException() throws IOException, CsvValidationException {
    // Given
    Job job = ScheduleHelper.getJob("Junk", true, JobStatus.FILE_UPLOADED);
    List<Job> jobList = new ArrayList<>();
    jobList.add(job);

    ScheduleHelper.createFile(job, "\"junk");

    when(jobRepository.findByJobStatus(JobStatus.FILE_UPLOADED)).thenReturn(jobList);

    // When
    underTest.processFiles();

    // Then
    verify(jobRepository).saveAndFlush(jobCaptor.capture());
    Job capturedJob = jobCaptor.getValue();
    assertThat(capturedJob.getJobStatus()).isEqualTo(JobStatus.VALIDATED_TOTAL_FAILURE);
    assertThat(capturedJob.getFatalErrorDescription()).contains("Exception Message");
  }

  @Test
  void testFileStagerFileDoesNotExist() throws CsvValidationException {
    // Given
    Job job = ScheduleHelper.getJob("Junk", true, JobStatus.FILE_UPLOADED);
    List<Job> jobList = new ArrayList<>();
    jobList.add(job);
    when(jobRepository.findByJobStatus(JobStatus.FILE_UPLOADED)).thenReturn(jobList);

    // When
    underTest.processFiles();

    // Then
    assertThat(job.getJobStatus()).isEqualTo(JobStatus.FILE_UPLOADED);
  }
}
