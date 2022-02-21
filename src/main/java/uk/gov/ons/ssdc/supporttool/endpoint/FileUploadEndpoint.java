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

@Controller
public class FileUploadEndpoint {

  @Value("${file-upload-storage-path}")
  private String fileUploadStoragePath;

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

  // The mark of the beast: BOM
  private final byte[] BYTE_ORDER_MARK = {(byte) 239, (byte) 187, (byte) 191};

  private String stripBomFromStringIfExists(String stringToCheckAndStrip) throws IOException {

    try (InputStream input = new ByteArrayInputStream(stringToCheckAndStrip.getBytes())) {

      // Read in the length of a possible BOM in bytes
      byte[] firstFewBytes = input.readNBytes(BYTE_ORDER_MARK.length);

      if (!Arrays.equals(firstFewBytes, BYTE_ORDER_MARK)) {
        // So not a standard Excel BOM csv, leave it as it is.
        return stringToCheckAndStrip;
      }

      // We've already read the BOM length at the start, so now make into a string
      return new String(input.readAllBytes());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
