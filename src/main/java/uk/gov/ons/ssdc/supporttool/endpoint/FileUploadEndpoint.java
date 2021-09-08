package uk.gov.ons.ssdc.supporttool.endpoint;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
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

      boolean firstLine = true;

      while (reader.ready()) {
        String line = reader.readLine();

        if (firstLine) {
          line = stripBomFromStringIfExists(line);
          firstLine = false;
        }

        fos.write(line.getBytes());

        fos.write("\n".getBytes());
      }

    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return new ResponseEntity<UUID>(fileId, HttpStatus.CREATED);
  }

  private final int[] BYTE_ORDER_MARK = {239, 187, 191};

  private String stripBomFromStringIfExists(String stringToCheckAndStrip) throws IOException {
    int[] firstFewBytes = new int[BYTE_ORDER_MARK.length];

    InputStream input = null;

    try {
      input = new ByteArrayInputStream(stringToCheckAndStrip.getBytes());

      for (int index = 0; index < BYTE_ORDER_MARK.length; ++index) {
        firstFewBytes[index] = input.read(); // read a single byte
      }

      if (!Arrays.equals(firstFewBytes, BYTE_ORDER_MARK)) {
        return stringToCheckAndStrip;
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      input.close();
    }

    return stripBomFromString(stringToCheckAndStrip);
  }

  private String stripBomFromString(String stringToStrip) throws IOException {
    long truncatedSize = stringToStrip.getBytes().length - BYTE_ORDER_MARK.length;
    byte[] memory = new byte[(int) (truncatedSize)];
    String strippedString;

    InputStream input = null;
    try {
      input = new ByteArrayInputStream(stringToStrip.getBytes());
      input.skip(BYTE_ORDER_MARK.length);
      int totalBytesReadIntoMemory = 0;

      while (totalBytesReadIntoMemory < truncatedSize) {
        int bytesRemaining = (int) truncatedSize - totalBytesReadIntoMemory;
        int bytesRead = input.read(memory, totalBytesReadIntoMemory, bytesRemaining);

        if (bytesRead > 0) {
          totalBytesReadIntoMemory = totalBytesReadIntoMemory + bytesRead;
        }
      }

      strippedString = new String(memory);
    } finally {
      input.close();
    }

    return strippedString;
  }
}
