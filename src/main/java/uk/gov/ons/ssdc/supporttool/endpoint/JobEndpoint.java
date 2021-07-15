package uk.gov.ons.ssdc.supporttool.endpoint;

import static uk.gov.ons.ssdc.supporttool.model.entity.UserGroupAuthorisedActivityType.LOAD_SAMPLE;
import static uk.gov.ons.ssdc.supporttool.model.entity.UserGroupAuthorisedActivityType.VIEW_SAMPLE_LOAD_PROGRESS;

import com.opencsv.CSVWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.ons.ssdc.supporttool.model.dto.ui.JobDto;
import uk.gov.ons.ssdc.supporttool.model.dto.ui.JobStatusDto;
import uk.gov.ons.ssdc.supporttool.model.entity.CollectionExercise;
import uk.gov.ons.ssdc.supporttool.model.entity.Job;
import uk.gov.ons.ssdc.supporttool.model.entity.JobRow;
import uk.gov.ons.ssdc.supporttool.model.entity.JobRowStatus;
import uk.gov.ons.ssdc.supporttool.model.entity.JobStatus;
import uk.gov.ons.ssdc.supporttool.model.repository.CollectionExerciseRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.JobRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.JobRowRepository;
import uk.gov.ons.ssdc.supporttool.security.UserIdentity;
import uk.gov.ons.ssdc.supporttool.utility.SampleColumnHelper;

@RestController
@RequestMapping(value = "/job")
public class JobEndpoint {

  private final JobRepository jobRepository;
  private final JobRowRepository jobRowRepository;
  private final CollectionExerciseRepository collectionExerciseRepository;
  private final UserIdentity userIdentity;

  public JobEndpoint(
      JobRepository jobRepository,
      JobRowRepository jobRowRepository,
      CollectionExerciseRepository collectionExerciseRepository,
      UserIdentity userIdentity) {
    this.jobRepository = jobRepository;
    this.jobRowRepository = jobRowRepository;
    this.collectionExerciseRepository = collectionExerciseRepository;
    this.userIdentity = userIdentity;
  }

  @GetMapping
  public List<JobDto> findCollexJobs(
      @RequestParam(value = "collectionExercise") UUID collectionExerciseId,
      @RequestHeader(required = false, value = "x-goog-iap-jwt-assertion") String jwtToken) {
    Optional<CollectionExercise> collexOpt =
        collectionExerciseRepository.findById(collectionExerciseId);

    if (!collexOpt.isPresent()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Collection exercise not found");
    }

    CollectionExercise collx = collexOpt.get();

    userIdentity.checkUserPermission(jwtToken, collx.getSurvey(), VIEW_SAMPLE_LOAD_PROGRESS);

    return jobRepository.findByCollectionExerciseOrderByCreatedAtDesc(collx).stream()
        .map(this::mapJob)
        .collect(Collectors.toList());
  }

  @GetMapping(value = "/{id}")
  public JobDto findJob(
      @PathVariable("id") UUID id,
      @RequestHeader(required = false, value = "x-goog-iap-jwt-assertion") String jwtToken) {
    Job job = jobRepository.findById(id).get();
    userIdentity.checkUserPermission(
        jwtToken, job.getCollectionExercise().getSurvey(), VIEW_SAMPLE_LOAD_PROGRESS);

    return mapJob(jobRepository.findById(id).get());
  }

  @GetMapping(value = "/{id}/error")
  @ResponseBody
  public String getErrorCsv(
      @PathVariable("id") UUID id,
      @RequestHeader(required = false, value = "x-goog-iap-jwt-assertion") String jwtToken,
      HttpServletResponse response) {
    Job job = jobRepository.findById(id).get();
    userIdentity.checkUserPermission(
        jwtToken, job.getCollectionExercise().getSurvey(), LOAD_SAMPLE);

    List<JobRow> jobRows =
        jobRowRepository.findByJobAndAndJobRowStatusOrderByOriginalRowLineNumber(
            job, JobRowStatus.PROCESSED_ERROR);

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
      @RequestHeader(required = false, value = "x-goog-iap-jwt-assertion") String jwtToken,
      HttpServletResponse response) {
    Job job = jobRepository.findById(id).get();
    userIdentity.checkUserPermission(
        jwtToken, job.getCollectionExercise().getSurvey(), LOAD_SAMPLE);

    List<JobRow> jobRows =
        jobRowRepository.findByJobAndAndJobRowStatusOrderByOriginalRowLineNumber(
            job, JobRowStatus.PROCESSED_ERROR);

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

  private JobDto mapJob(Job job) {
    JobDto jobDto = new JobDto();
    jobDto.setId(job.getId());
    jobDto.setCreatedAt(job.getCreatedAt());
    jobDto.setCreatedBy(job.getCreatedBy());
    jobDto.setLastUpdatedAt(job.getLastUpdatedAt());
    jobDto.setFileName(job.getFileName());
    jobDto.setFileRowCount(job.getFileRowCount());
    jobDto.setJobStatus(JobStatusDto.valueOf(job.getJobStatus().name()));

    if (job.getJobStatus() == JobStatus.FILE_UPLOADED) {
      jobDto.setStagedRowCount(0);
    } else {
      jobDto.setStagedRowCount(job.getStagingRowNumber());

      if (job.getJobStatus() != JobStatus.STAGING_IN_PROGRESS) {
        jobDto.setProcessedRowCount(
            jobRowRepository.countByJobAndAndJobRowStatus(job, JobRowStatus.PROCESSED_OK));
        jobDto.setRowErrorCount(
            jobRowRepository.countByJobAndAndJobRowStatus(job, JobRowStatus.PROCESSED_ERROR));
      }
    }

    jobDto.setFatalErrorDescription(job.getFatalErrorDescription());
    return jobDto;
  }
}
