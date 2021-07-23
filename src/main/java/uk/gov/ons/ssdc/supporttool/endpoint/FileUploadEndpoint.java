package uk.gov.ons.ssdc.supporttool.endpoint;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.ons.ssdc.supporttool.model.entity.CollectionExercise;
import uk.gov.ons.ssdc.supporttool.model.entity.Job;
import uk.gov.ons.ssdc.supporttool.model.entity.JobStatus;
import uk.gov.ons.ssdc.supporttool.model.entity.UserGroupAuthorisedActivityType;
import uk.gov.ons.ssdc.supporttool.model.repository.CollectionExerciseRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.JobRepository;
import uk.gov.ons.ssdc.supporttool.security.UserIdentity;

@Controller
public class FileUploadEndpoint {

  private final JobRepository jobRepository;
  private final UserIdentity userIdentity;
  private final CollectionExerciseRepository collectionExerciseRepository;

  public FileUploadEndpoint(
      JobRepository jobRepository,
      UserIdentity userIdentity,
      CollectionExerciseRepository collectionExerciseRepository) {
    this.jobRepository = jobRepository;
    this.userIdentity = userIdentity;
    this.collectionExerciseRepository = collectionExerciseRepository;
  }

  @PostMapping("/upload")
  public ResponseEntity<?> handleFileUpload(
      @RequestParam("file") MultipartFile file,
      @RequestParam(value = "collectionExerciseId") UUID collectionExerciseId,
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {

    // Check that collex exists
    Optional<CollectionExercise> collexOpt =
        collectionExerciseRepository.findById(collectionExerciseId);
    if (!collexOpt.isPresent()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Collection exercise not found");
    }

    // Check user is authorised to upload sample for this survey
    userIdentity.checkUserPermission(
        userEmail, collexOpt.get().getSurvey(), UserGroupAuthorisedActivityType.LOAD_SAMPLE);

    UUID fileId = UUID.randomUUID();

    try (FileOutputStream fos = new FileOutputStream("/tmp/" + fileId);
        BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
      Job job = new Job();
      job.setId(UUID.randomUUID());

      job.setFileName(file.getOriginalFilename());
      job.setFileId(fileId);
      job.setJobStatus(JobStatus.FILE_UPLOADED);
      job.setCreatedBy(userEmail);
      job.setCollectionExercise(collexOpt.get());

      int rowCount = 0;
      while (reader.ready()) {
        String line = reader.readLine();
        rowCount++;
        fos.write(line.getBytes());
        fos.write("\n".getBytes());
      }

      job.setFileRowCount(rowCount);

      jobRepository.saveAndFlush(job);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return new ResponseEntity<>(null, HttpStatus.CREATED);
  }
}
