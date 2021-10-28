package uk.gov.ons.ssdc.supporttool.endpoint;

import com.opencsv.CSVWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.ons.ssdc.common.model.entity.CollectionExercise;
import uk.gov.ons.ssdc.common.model.entity.Job;
import uk.gov.ons.ssdc.common.model.entity.JobRow;
import uk.gov.ons.ssdc.common.model.entity.JobRowStatus;
import uk.gov.ons.ssdc.common.model.entity.JobStatus;
import uk.gov.ons.ssdc.common.model.entity.JobType;
import uk.gov.ons.ssdc.common.model.entity.Survey;
import uk.gov.ons.ssdc.supporttool.model.dto.ui.JobDto;
import uk.gov.ons.ssdc.supporttool.model.dto.ui.JobStatusDto;
import uk.gov.ons.ssdc.supporttool.model.dto.ui.JobTypeDto;
import uk.gov.ons.ssdc.supporttool.model.repository.CollectionExerciseRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.JobRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.JobRowRepository;
import uk.gov.ons.ssdc.supporttool.security.UserIdentity;
import uk.gov.ons.ssdc.supporttool.utility.JobTypeHelper;
import uk.gov.ons.ssdc.supporttool.utility.JobTypeSettings;
import uk.gov.ons.ssdc.supporttool.utility.SampleColumnHelper;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping(value = "/api/job")
public class JobEndpoint {

  private final JobRepository jobRepository;
  private final JobRowRepository jobRowRepository;
  private final CollectionExerciseRepository collectionExerciseRepository;
  private final UserIdentity userIdentity;
  private final JobTypeHelper jobTypeHelper;

  @Value("${file-upload-storage-path}")
  private String fileUploadStoragePath;

  public JobEndpoint(
      JobRepository jobRepository,
      JobRowRepository jobRowRepository,
      CollectionExerciseRepository collectionExerciseRepository,
      UserIdentity userIdentity,
      JobTypeHelper jobTypeHelper) {
    this.jobRepository = jobRepository;
    this.jobRowRepository = jobRowRepository;
    this.collectionExerciseRepository = collectionExerciseRepository;
    this.userIdentity = userIdentity;
    this.jobTypeHelper = jobTypeHelper;
  }

  @GetMapping
  public List<JobDto> findCollexJobs(
      @RequestParam(value = "collectionExercise") UUID collectionExerciseId,
      @RequestParam(value = "jobType") JobType jobType,
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {
    Optional<CollectionExercise> collexOpt =
        collectionExerciseRepository.findById(collectionExerciseId);

    if (collexOpt.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Collection exercise not found");
    }

    CollectionExercise collx = collexOpt.get();
    checkUserViewProgressPermissionByJobType(userEmail, collx.getSurvey(), jobType);

    return jobRepository.findByCollectionExerciseOrderByCreatedAtDesc(collx).stream()
        .map(this::mapJob)
        .collect(Collectors.toList());
  }

  @GetMapping(value = "/{id}")
  public JobDto findJob(
      @PathVariable("id") UUID id,
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {
    Job job = jobRepository.findById(id).get();
    checkUserViewProgressPermissionByJobType(
        userEmail, job.getCollectionExercise().getSurvey(), job.getJobType());

    return mapJob(jobRepository.findById(id).get());
  }

  @GetMapping(value = "/{id}/error")
  @ResponseBody
  public String getErrorCsv(
      @PathVariable("id") UUID id,
      @Value("#{request.getAttribute('userEmail')}") String userEmail,
      HttpServletResponse response) {
    Job job = jobRepository.findById(id).get();

    checkUserLoadFilePermissionByJobType(
        userEmail, job.getCollectionExercise().getSurvey(), job.getJobType());

    List<JobRow> jobRows =
        jobRowRepository.findByJobAndAndJobRowStatusOrderByOriginalRowLineNumber(
            job, JobRowStatus.VALIDATED_ERROR);

    String csvFileName = "ERROR_" + job.getFileName();

    response.setContentType("text/plain; charset=utf-8");

    String headerKey = "Content-Disposition";
    String headerValue = String.format("attachment; filename=\"%s\"", csvFileName);
    response.setHeader(headerKey, headerValue);

    String csvContent;

    try (StringWriter stringWriter = new StringWriter();
        CSVWriter csvWriter = new CSVWriter(stringWriter)) {
      csvWriter.writeNext(SampleColumnHelper.getExpectedColumns(job));

      for (JobRow jobRow : jobRows) {
        csvWriter.writeNext(jobRow.getOriginalRowData());
      }

      csvContent = stringWriter.toString();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return csvContent;
  }

  @GetMapping(value = "/{id}/errorDetail")
  @ResponseBody
  public String getErrorDetailCsv(
      @PathVariable("id") UUID id,
      @Value("#{request.getAttribute('userEmail')}") String userEmail,
      HttpServletResponse response) {
    Job job = jobRepository.findById(id).get();

    checkUserLoadFilePermissionByJobType(
        userEmail, job.getCollectionExercise().getSurvey(), job.getJobType());

    List<JobRow> jobRows =
        jobRowRepository.findByJobAndAndJobRowStatusOrderByOriginalRowLineNumber(
            job, JobRowStatus.VALIDATED_ERROR);

    String csvFileName = "ERROR_DETAIL_" + job.getFileName();

    response.setContentType("text/plain; charset=utf-8");

    String headerKey = "Content-Disposition";
    String headerValue = String.format("attachment; filename=\"%s\"", csvFileName);
    response.setHeader(headerKey, headerValue);

    String csvContent;

    try (StringWriter stringWriter = new StringWriter();
        CSVWriter csvWriter = new CSVWriter(stringWriter)) {
      csvWriter.writeNext(new String[] {"ORIGINAL ROW NUMBER", "ERRORS"});

      for (JobRow jobRow : jobRows) {
        csvWriter.writeNext(
            new String[] {
              String.valueOf(jobRow.getOriginalRowLineNumber()),
              jobRow.getValidationErrorDescriptions()
            });
      }

      csvContent = stringWriter.toString();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return csvContent;
  }

  @PostMapping(value = "/{id}/process")
  @Transactional
  public void processJob(
      @PathVariable("id") UUID id,
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {
    Job job = jobRepository.findById(id).get();

    checkUserLoadFilePermissionByJobType(
        userEmail, job.getCollectionExercise().getSurvey(), job.getJobType());

    if (job.getJobStatus() == JobStatus.VALIDATED_OK
        || job.getJobStatus() == JobStatus.VALIDATED_WITH_ERRORS) {
      job.setJobStatus(JobStatus.PROCESSING_IN_PROGRESS);
      job.setProcessedBy(userEmail);
      job.setProcessedAt(OffsetDateTime.now());
      jobRepository.saveAndFlush(job);
    } else {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Can't process a job which isn't validated");
    }
  }

  @PostMapping(value = "/{id}/cancel")
  @Transactional
  public void cancelJob(
      @PathVariable("id") UUID id,
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {
    Job job = jobRepository.findById(id).get();
    checkUserLoadFilePermissionByJobType(
        userEmail, job.getCollectionExercise().getSurvey(), job.getJobType());

    if (job.getJobStatus() == JobStatus.VALIDATED_OK
        || job.getJobStatus() == JobStatus.VALIDATED_WITH_ERRORS) {
      job.setJobStatus(JobStatus.CANCELLED);
      job.setCancelledBy(userEmail);
      job.setCancelledAt(OffsetDateTime.now());
      jobRepository.saveAndFlush(job);

      jobRowRepository.deleteByJobAndAndJobRowStatus(job, JobRowStatus.VALIDATED_OK);
    } else {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Can't cancel a job which isn't validated");
    }
  }

  @PostMapping
  public ResponseEntity<UUID> submitJob(
      @RequestParam(value = "fileId") UUID fileId,
      @RequestParam(value = "fileName") String fileName,
      @RequestParam(value = "collectionExerciseId") UUID collectionExerciseId,
      @RequestParam(value = "jobType") JobType jobType,
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {

    // Check that collex exists
    Optional<CollectionExercise> collexOpt =
        collectionExerciseRepository.findById(collectionExerciseId);
    if (collexOpt.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Collection exercise not found");
    }

    checkUserLoadFilePermissionByJobType(userEmail, collexOpt.get().getSurvey(), jobType);

    File file = new File(fileUploadStoragePath + fileId);
    int rowCount;
    try (Stream<String> stream = Files.lines(file.toPath(), StandardCharsets.UTF_8)) {
      rowCount = (int) stream.count();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    UUID jobId = UUID.randomUUID();

    Job job = new Job();
    job.setId(jobId);
    job.setFileName(fileName);
    job.setFileId(fileId);
    job.setJobStatus(JobStatus.FILE_UPLOADED);
    job.setCreatedBy(userEmail);
    job.setCollectionExercise(collexOpt.get());
    job.setFileRowCount(rowCount);
    job.setJobType(jobType);

    jobRepository.saveAndFlush(job);

    return new ResponseEntity<>(jobId, HttpStatus.CREATED);
  }

  private void checkUserLoadFilePermissionByJobType(
      String userEmail, Survey survey, JobType jobType) {
    JobTypeSettings jobTypeSettings = jobTypeHelper.getJobTypeSettings(jobType, survey);

    userIdentity.checkUserPermission(userEmail, survey, jobTypeSettings.getFileLoadPermission());
  }

  private void checkUserViewProgressPermissionByJobType(
      String userEmail, Survey survey, JobType jobType) {
    JobTypeSettings jobTypeSettings = jobTypeHelper.getJobTypeSettings(jobType, survey);

    userIdentity.checkUserPermission(
        userEmail, survey, jobTypeSettings.getFileViewProgressPersmission());
  }

  private JobDto mapJob(Job job) {
    JobDto jobDto = new JobDto();
    jobDto.setId(job.getId());
    jobDto.setCreatedAt(job.getCreatedAt());
    jobDto.setCreatedBy(job.getCreatedBy());
    jobDto.setLastUpdatedAt(job.getLastUpdatedAt());
    jobDto.setFileName(job.getFileName());
    jobDto.setFileRowCount(job.getFileRowCount());
    jobDto.setSampleWithHeaderRow(job.getCollectionExercise().getSurvey().isSampleWithHeaderRow());
    jobDto.setJobStatus(JobStatusDto.valueOf(job.getJobStatus().name()));
    jobDto.setStagedRowCount(job.getStagingRowNumber());
    jobDto.setValidatedRowCount(job.getValidatingRowNumber());
    jobDto.setProcessedRowCount(job.getProcessingRowNumber());
    jobDto.setRowErrorCount(job.getErrorRowCount());
    jobDto.setFatalErrorDescription(job.getFatalErrorDescription());
    jobDto.setProcessedBy(job.getProcessedBy());
    jobDto.setProcessedAt(job.getProcessedAt());
    jobDto.setCancelledBy(job.getCancelledBy());
    jobDto.setCancelledAt(job.getCancelledAt());
    jobDto.setJobType(JobTypeDto.valueOf(job.getJobType().name()));
    return jobDto;
  }
}
