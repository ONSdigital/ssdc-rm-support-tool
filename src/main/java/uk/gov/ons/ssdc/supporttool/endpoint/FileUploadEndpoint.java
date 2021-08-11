package uk.gov.ons.ssdc.supporttool.endpoint;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.ons.ssdc.supporttool.model.repository.CollectionExerciseRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.JobRepository;
import uk.gov.ons.ssdc.supporttool.security.UserIdentity;

@Controller
public class FileUploadEndpoint {

  private final JobRepository jobRepository;
  private final UserIdentity userIdentity;
  private final CollectionExerciseRepository collectionExerciseRepository;

  @Value("${file-upload-storage-path}")
  private String fileUploadStoragePath;

  public FileUploadEndpoint(
      JobRepository jobRepository,
      UserIdentity userIdentity,
      CollectionExerciseRepository collectionExerciseRepository) {
    this.jobRepository = jobRepository;
    this.userIdentity = userIdentity;
    this.collectionExerciseRepository = collectionExerciseRepository;
  }

  @PostMapping("/api/upload")
  public ResponseEntity<UUID> handleFileUpload(@RequestParam("file") MultipartFile file) {

    UUID fileId = UUID.randomUUID();

    try (FileOutputStream fos = new FileOutputStream(fileUploadStoragePath + fileId);
        BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {

      int rowCount = 0;
      while (reader.ready()) {
        String line = reader.readLine();
        rowCount++;
        fos.write(line.getBytes());
        fos.write("\n".getBytes());
      }

    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return new ResponseEntity<UUID>(fileId, HttpStatus.CREATED);
  }
}
