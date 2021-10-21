package uk.gov.ons.ssdc.supporttool.endpoint;

import static uk.gov.ons.ssdc.common.model.entity.UserGroupAuthorisedActivityType.LOAD_SAMPLE;
import static uk.gov.ons.ssdc.common.model.entity.UserGroupAuthorisedActivityType.VIEW_SAMPLE_LOAD_PROGRESS;

import com.opencsv.CSVWriter;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletResponse;
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
import uk.gov.ons.ssdc.common.model.entity.UserGroupAuthorisedActivityType;
import uk.gov.ons.ssdc.supporttool.client.RasRmSampleServiceClient;
import uk.gov.ons.ssdc.supporttool.model.dto.ui.JobDto;
import uk.gov.ons.ssdc.supporttool.model.dto.ui.JobStatusDto;
import uk.gov.ons.ssdc.supporttool.model.repository.CollectionExerciseRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.JobRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.JobRowRepository;
import uk.gov.ons.ssdc.supporttool.security.UserIdentity;
import uk.gov.ons.ssdc.supporttool.utility.SampleColumnHelper;

@RestController
@RequestMapping(value = "/api/job")
public class JobEndpoint {
  private static final Set<String> MANDATORY_RAS_RM_BUSINESS_COLLEX_METADATA =
      Set.of("rasRmCollectionExerciseId", "rasRmCollectionInstrumentId");

  private final JobRepository jobRepository;
  private final JobRowRepository jobRowRepository;
  private final CollectionExerciseRepository collectionExerciseRepository;
  private final UserIdentity userIdentity;
  private final RasRmSampleServiceClient rasRmSampleServiceClient;

  @Value("${file-upload-storage-path}")
  private String fileUploadStoragePath;

  public JobEndpoint(
      JobRepository jobRepository,
      JobRowRepository jobRowRepository,
      CollectionExerciseRepository collectionExerciseRepository,
      UserIdentity userIdentity,
      RasRmSampleServiceClient rasRmSampleServiceClient) {
    this.jobRepository = jobRepository;
    this.jobRowRepository = jobRowRepository;
    this.collectionExerciseRepository = collectionExerciseRepository;
    this.userIdentity = userIdentity;
    this.rasRmSampleServiceClient = rasRmSampleServiceClient;
  }

  @GetMapping
  public List<JobDto> findCollexJobs(
      @RequestParam(value = "collectionExercise") UUID collectionExerciseId,
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {
    Optional<CollectionExercise> collexOpt =
        collectionExerciseRepository.findById(collectionExerciseId);

    if (!collexOpt.isPresent()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Collection exercise not found");
    }

    CollectionExercise collx = collexOpt.get();

    userIdentity.checkUserPermission(userEmail, collx.getSurvey(), VIEW_SAMPLE_LOAD_PROGRESS);

    return jobRepository.findByCollectionExerciseOrderByCreatedAtDesc(collx).stream()
        .map(this::mapJob)
        .collect(Collectors.toList());
  }

  @GetMapping(value = "/{id}")
  public JobDto findJob(
      @PathVariable("id") UUID id,
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {
    Job job = jobRepository.findById(id).get();
    userIdentity.checkUserPermission(
        userEmail, job.getCollectionExercise().getSurvey(), VIEW_SAMPLE_LOAD_PROGRESS);

    return mapJob(jobRepository.findById(id).get());
  }

  @GetMapping(value = "/{id}/error")
  @ResponseBody
  public String getErrorCsv(
      @PathVariable("id") UUID id,
      @Value("#{request.getAttribute('userEmail')}") String userEmail,
      HttpServletResponse response) {
    Job job = jobRepository.findById(id).get();
    userIdentity.checkUserPermission(
        userEmail, job.getCollectionExercise().getSurvey(), LOAD_SAMPLE);

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
    userIdentity.checkUserPermission(
        userEmail, job.getCollectionExercise().getSurvey(), LOAD_SAMPLE);

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
    userIdentity.checkUserPermission(
        userEmail, job.getCollectionExercise().getSurvey(), LOAD_SAMPLE);

    if (job.getJobStatus() == JobStatus.VALIDATED_OK
        || job.getJobStatus() == JobStatus.VALIDATED_WITH_ERRORS) {

      if (job.getCollectionExercise()
          .getSurvey()
          .getSampleDefinitionUrl()
          .endsWith("business.json")) {

        CollectionExercise collex = job.getCollectionExercise();
        Object metadataObject = collex.getMetadata();

        if (metadataObject == null) {
          throw new RuntimeException(
              "Unexpected null metadata. Metadata is required for RAS-RM business.");
        }

        if (!(metadataObject instanceof Map)) {
          throw new RuntimeException(
              "Unexpected metadata type. Wanted Map but got "
                  + metadataObject.getClass().getSimpleName());
        }

        Map metadata = (Map) metadataObject;

        if (!metadata.keySet().containsAll(MANDATORY_RAS_RM_BUSINESS_COLLEX_METADATA)) {
          throw new RuntimeException("Metadata does not contain mandatory values");
        }

        // TODO: has been hard-coded to 1 collection instrument, for now
        UUID rasRmSampleSummaryId =
            rasRmSampleServiceClient.createSampleSummary(job.getFileRowCount(), 1).getId();

        // Now store the rasRmSampleSummaryId into the metadata for later use
        metadata.put("rasRmSampleSummaryId", rasRmSampleSummaryId.toString());

        collex.setMetadata(metadata);
        collectionExerciseRepository.saveAndFlush(collex);
      }

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
    userIdentity.checkUserPermission(
        userEmail, job.getCollectionExercise().getSurvey(), LOAD_SAMPLE);

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
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {

    // Check that collex exists
    Optional<CollectionExercise> collexOpt =
        collectionExerciseRepository.findById(collectionExerciseId);
    if (!collexOpt.isPresent()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Collection exercise not found");
    }

    // Check user is authorised to submit job for this survey
    userIdentity.checkUserPermission(
        userEmail, collexOpt.get().getSurvey(), UserGroupAuthorisedActivityType.LOAD_SAMPLE);

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

    jobRepository.saveAndFlush(job);

    return new ResponseEntity<>(jobId, HttpStatus.CREATED);
  }

  private JobDto mapJob(Job job) {
    JobDto jobDto = new JobDto();
    jobDto.setId(job.getId());
    jobDto.setCreatedAt(job.getCreatedAt());
    jobDto.setCreatedBy(job.getCreatedBy());
    jobDto.setLastUpdatedAt(job.getLastUpdatedAt());
    jobDto.setFileName(job.getFileName());
    jobDto.setFileRowCount(job.getFileRowCount());
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
    return jobDto;
  }
}
