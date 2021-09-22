package uk.gov.ons.ssdc.supporttool.endpoint;

import static uk.gov.ons.ssdc.common.model.entity.UserGroupAuthorisedActivityType.CREATE_DEACTIVATE_UAC_ACTION_RULE;
import static uk.gov.ons.ssdc.common.model.entity.UserGroupAuthorisedActivityType.CREATE_FACE_TO_FACE_ACTION_RULE;
import static uk.gov.ons.ssdc.common.model.entity.UserGroupAuthorisedActivityType.CREATE_OUTBOUND_PHONE_ACTION_RULE;
import static uk.gov.ons.ssdc.common.model.entity.UserGroupAuthorisedActivityType.CREATE_PRINT_ACTION_RULE;
import static uk.gov.ons.ssdc.common.model.entity.UserGroupAuthorisedActivityType.CREATE_SMS_ACTION_RULE;

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
import uk.gov.ons.ssdc.common.model.entity.SmsTemplate;
import uk.gov.ons.ssdc.common.model.entity.UserGroupAuthorisedActivityType;
import uk.gov.ons.ssdc.supporttool.model.dto.messaging.ActionRuleDTO;
import uk.gov.ons.ssdc.supporttool.model.repository.ActionRuleRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.CollectionExerciseRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.PrintTemplateRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.SmsTemplateRepository;
import uk.gov.ons.ssdc.supporttool.security.UserIdentity;

@RestController
@RequestMapping(value = "/api/actionRules")
public class ActionRuleEndpoint {

  private final ActionRuleRepository actionRuleRepository;
  private final UserIdentity userIdentity;
  private final CollectionExerciseRepository collectionExerciseRepository;
  private final PrintTemplateRepository printTemplateRepository;
  private final SmsTemplateRepository smsTemplateRepository;

  public ActionRuleEndpoint(
      ActionRuleRepository actionRuleRepository,
      UserIdentity userIdentity,
      CollectionExerciseRepository collectionExerciseRepository,
      PrintTemplateRepository printTemplateRepository,
      SmsTemplateRepository smsTemplateRepository) {
    this.actionRuleRepository = actionRuleRepository;
    this.userIdentity = userIdentity;
    this.collectionExerciseRepository = collectionExerciseRepository;
    this.printTemplateRepository = printTemplateRepository;
    this.smsTemplateRepository = smsTemplateRepository;
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

    UserGroupAuthorisedActivityType userActivity;

    PrintTemplate printTemplate = null;
    SmsTemplate smsTemplate = null;
    switch (actionRuleDTO.getType()) {
      case PRINT:
        userActivity = CREATE_PRINT_ACTION_RULE;
        printTemplate =
            printTemplateRepository
                .findById(actionRuleDTO.getPackCode())
                .orElseThrow(
                    () ->
                        new ResponseStatusException(
                            HttpStatus.BAD_REQUEST, "Print template not found"));
        break;
      case OUTBOUND_TELEPHONE:
        userActivity = CREATE_OUTBOUND_PHONE_ACTION_RULE;
        break;
      case FACE_TO_FACE:
        userActivity = CREATE_FACE_TO_FACE_ACTION_RULE;
        break;
      case DEACTIVATE_UAC:
        userActivity = CREATE_DEACTIVATE_UAC_ACTION_RULE;
        break;
      case SMS:
        userActivity = CREATE_SMS_ACTION_RULE;
        smsTemplate =
            smsTemplateRepository
                .findById(actionRuleDTO.getPackCode())
                .orElseThrow(
                    () ->
                        new ResponseStatusException(
                            HttpStatus.BAD_REQUEST, "SMS template not found"));
        break;
      default:
        throw new IllegalStateException("Unexpected value: " + actionRuleDTO.getType());
    }

    userIdentity.checkUserPermission(createdBy, collexExercise.get().getSurvey(), userActivity);

    ActionRule actionRule = new ActionRule();
    actionRule.setId(UUID.randomUUID());
    actionRule.setClassifiers(actionRuleDTO.getClassifiers());
    actionRule.setPrintTemplate(printTemplate);
    actionRule.setCollectionExercise(collexExercise.get());
    actionRule.setType(actionRuleDTO.getType());
    actionRule.setTriggerDateTime(actionRuleDTO.getTriggerDateTime());
    actionRule.setCreatedBy(createdBy);
    actionRule.setSmsTemplate(smsTemplate);
    actionRule.setPhoneNumberColumn(actionRuleDTO.getPhoneNumberColumn());

    actionRuleRepository.saveAndFlush(actionRule);

    return new ResponseEntity<>(actionRule.getId(), HttpStatus.CREATED);
  }
}
