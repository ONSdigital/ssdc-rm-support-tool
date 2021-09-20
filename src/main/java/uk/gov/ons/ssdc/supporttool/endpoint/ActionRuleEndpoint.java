package uk.gov.ons.ssdc.supporttool.endpoint;

import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.ons.ssdc.common.model.entity.ActionRule;
import uk.gov.ons.ssdc.common.model.entity.CollectionExercise;
import uk.gov.ons.ssdc.common.model.entity.PrintTemplate;
import uk.gov.ons.ssdc.common.model.entity.UserGroupAuthorisedActivityType;
import uk.gov.ons.ssdc.supporttool.model.dto.messaging.ActionRuleDTO;
import uk.gov.ons.ssdc.supporttool.model.repository.ActionRuleRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.CollectionExerciseRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.PrintTemplateRepository;
import uk.gov.ons.ssdc.supporttool.security.UserIdentity;

@RestController
@RequestMapping(value = "/api/actionRules")
public class ActionRuleEndpoint {

  private final ActionRuleRepository actionRuleRepository;
  private final UserIdentity userIdentity;
  private final CollectionExerciseRepository collectionExerciseRepository;
  private final PrintTemplateRepository printTemplateRepository;

  public ActionRuleEndpoint(
      ActionRuleRepository actionRuleRepository,
      UserIdentity userIdentity,
      CollectionExerciseRepository collectionExerciseRepository,
      PrintTemplateRepository printTemplateRepository) {
    this.actionRuleRepository = actionRuleRepository;
    this.userIdentity = userIdentity;
    this.collectionExerciseRepository = collectionExerciseRepository;
    this.printTemplateRepository = printTemplateRepository;
  }

  @PostMapping
  @Transactional
  public ResponseEntity<UUID> insertActionRules(
      @RequestBody() ActionRuleDTO actionRuleDTO,
      @Value("#{request.getAttribute('userEmail')}") String createdBy) {

    Optional<CollectionExercise> collexExercise =
        collectionExerciseRepository.findById(actionRuleDTO.getCollectionExerciseId());

    if (!collexExercise.isPresent()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Collection exercise not found");
    }

    userIdentity.checkUserPermission(
        createdBy,
        collexExercise.get().getSurvey(),
        UserGroupAuthorisedActivityType.CREATE_PRINT_ACTION_RULE);

    PrintTemplate printTemplate =
        printTemplateRepository
            .findById(actionRuleDTO.getPackCode())
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Print template not found"));

    ActionRule actionRule = new ActionRule();
    actionRule.setId(UUID.randomUUID());
    actionRule.setClassifiers(actionRuleDTO.getClassifiers());
    actionRule.setPrintTemplate(printTemplate);
    actionRule.setCollectionExercise(collexExercise.get());
    actionRule.setType(actionRuleDTO.getType());
    actionRule.setTriggerDateTime(actionRuleDTO.getTriggerDateTime());
    actionRule.setCreatedBy(createdBy);

    actionRuleRepository.saveAndFlush(actionRule);

    return new ResponseEntity<>(actionRule.getId(), HttpStatus.CREATED);
  }
}
