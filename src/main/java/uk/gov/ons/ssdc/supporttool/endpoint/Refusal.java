package uk.gov.ons.ssdc.supporttool.endpoint;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.ons.ssdc.supporttool.model.dto.RefusalDTO;
import uk.gov.ons.ssdc.supporttool.model.entity.CollectionExercise;
import uk.gov.ons.ssdc.supporttool.model.entity.UserGroupAuthorisedActivityType;
import uk.gov.ons.ssdc.supporttool.model.repository.CollectionExerciseRepository;
import uk.gov.ons.ssdc.supporttool.security.UserIdentity;

@Controller
public class Refusal {

  private final UserIdentity userIdentity;
  private final CollectionExerciseRepository collectionExerciseRepository;

  @Autowired
  public Refusal(UserIdentity userIdentity,
      CollectionExerciseRepository collectionExerciseRepository) {
    this.userIdentity = userIdentity;
    this.collectionExerciseRepository = collectionExerciseRepository;
  }

  @PostMapping(value = "/refusal")
  public ResponseEntity<?> handleRefusal(
      @RequestBody RefusalDTO refusalDTO,
      @RequestHeader(required = false, value = "x-goog-iap-jwt-assertion") String jwtToken) {

    // Check that collex exists
    Optional<CollectionExercise> collexOpt =
        collectionExerciseRepository.findById(refusalDTO.getCollectionExerciseId());
    if (!collexOpt.isPresent()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Collection exercise not found");
    }

    // Check user is authorised to refuse a case for this survey
    userIdentity.checkUserPermission(
        jwtToken, collexOpt.get().getSurvey(), UserGroupAuthorisedActivityType.REFUSE_CASE);

    uacQidService.buildAndSendRefusalEvent(uacQidLink, caseToLink, newQidLink);

    return new ResponseEntity<>(HttpStatus.OK);
  }

//  @ExceptionHandler({CaseIdNotFoundException.class, QidNotFoundException.class})
//  public void handleCaseIdNotFoundAndInvalid(HttpServletResponse response) throws IOException {
//    response.sendError(NOT_FOUND.value());
//  }
}
