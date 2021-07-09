package uk.gov.ons.ssdc.supporttool.endpoint;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.ons.ssdc.supporttool.model.dto.RefusalDTO;
import uk.gov.ons.ssdc.supporttool.model.entity.Case;
import uk.gov.ons.ssdc.supporttool.model.entity.CollectionExercise;
import uk.gov.ons.ssdc.supporttool.model.entity.UserGroupAuthorisedActivityType;
import uk.gov.ons.ssdc.supporttool.model.repository.CaseRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.CollectionExerciseRepository;
import uk.gov.ons.ssdc.supporttool.security.UserIdentity;
import uk.gov.ons.ssdc.supporttool.service.CaseService;

@Controller
public class Refusal {

  private final CollectionExerciseRepository collectionExerciseRepository;
  private final CaseService caseService;
  private final UserIdentity userIdentity;

  @Autowired
  public Refusal(CaseRepository caseRepository,
      UserIdentity userIdentity,
      CollectionExerciseRepository collectionExerciseRepository,
      CaseService caseService) {
    this.userIdentity = userIdentity;
    this.collectionExerciseRepository = collectionExerciseRepository;
    this.caseService = caseService;
  }

  @PutMapping(value = "/refusal")
  public ResponseEntity<?> handleRefusal(
      @RequestBody RefusalDTO refusalDTO,
      @RequestHeader(required = false, value = "x-goog-iap-jwt-assertion") String jwtToken) {

    Case caze = caseService.getCaseByCaseId(refusalDTO.getCollectionCase().getCaseId());

    // Check that collex exists
    Optional<CollectionExercise> collexOpt =
        collectionExerciseRepository.findById(caze.getCollectionExercise().getId());
    if (!collexOpt.isPresent()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Collection exercise not found");
    }

    // Check user is authorised to refuse a case for this survey
    userIdentity.checkUserPermission(
        jwtToken, collexOpt.get().getSurvey(), UserGroupAuthorisedActivityType.REFUSE_CASE);

    caseService.buildAndSendRefusalEvent(refusalDTO, caze);

    return new ResponseEntity<>(HttpStatus.OK);
  }

//  @ExceptionHandler({CaseIdNotFoundException.class, QidNotFoundException.class})
//  public void handleCaseIdNotFoundAndInvalid(HttpServletResponse response) throws IOException {
//    response.sendError(NOT_FOUND.value());
//  }
}
