package uk.gov.ons.ssdc.supporttool.endpoint;

import static uk.gov.ons.ssdc.common.model.entity.UserGroupAuthorisedActivityType.CREATE_DEACTIVATE_UAC_ACTION_RULE;
import static uk.gov.ons.ssdc.common.model.entity.UserGroupAuthorisedActivityType.CREATE_EMAIL_ACTION_RULE;
import static uk.gov.ons.ssdc.common.model.entity.UserGroupAuthorisedActivityType.CREATE_EQ_FLUSH_ACTION_RULE;
import static uk.gov.ons.ssdc.common.model.entity.UserGroupAuthorisedActivityType.CREATE_EXPORT_FILE_ACTION_RULE;
import static uk.gov.ons.ssdc.common.model.entity.UserGroupAuthorisedActivityType.CREATE_FACE_TO_FACE_ACTION_RULE;
import static uk.gov.ons.ssdc.common.model.entity.UserGroupAuthorisedActivityType.CREATE_OUTBOUND_PHONE_ACTION_RULE;
import static uk.gov.ons.ssdc.common.model.entity.UserGroupAuthorisedActivityType.CREATE_SMS_ACTION_RULE;
import static uk.gov.ons.ssdc.supporttool.utility.ColumnHelper.getSurveyColumns;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.ons.ssdc.common.model.entity.ActionRule;
import uk.gov.ons.ssdc.common.model.entity.ActionRuleType;
import uk.gov.ons.ssdc.common.model.entity.CollectionExercise;
import uk.gov.ons.ssdc.common.model.entity.EmailTemplate;
import uk.gov.ons.ssdc.common.model.entity.ExportFileTemplate;
import uk.gov.ons.ssdc.common.model.entity.SmsTemplate;
import uk.gov.ons.ssdc.common.model.entity.UserGroupAuthorisedActivityType;
import uk.gov.ons.ssdc.supporttool.model.dto.ui.ActionRuleDto;
import uk.gov.ons.ssdc.supporttool.model.repository.ActionRuleRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.CollectionExerciseRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.EmailTemplateRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.ExportFileTemplateRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.SmsTemplateRepository;
import uk.gov.ons.ssdc.supporttool.security.UserIdentity;

@RestController
@RequestMapping(value = "/api/actionRules")
public class ActionRuleEndpoint {

  private static final Logger log = LoggerFactory.getLogger(ActionRuleEndpoint.class);
  private final ActionRuleRepository actionRuleRepository;
  private final UserIdentity userIdentity;
  private final CollectionExerciseRepository collectionExerciseRepository;
  private final ExportFileTemplateRepository exportFileTemplateRepository;
  private final SmsTemplateRepository smsTemplateRepository;
  private final EmailTemplateRepository emailTemplateRepository;

  public ActionRuleEndpoint(
      ActionRuleRepository actionRuleRepository,
      UserIdentity userIdentity,
      CollectionExerciseRepository collectionExerciseRepository,
      ExportFileTemplateRepository exportFileTemplateRepository,
      SmsTemplateRepository smsTemplateRepository,
      EmailTemplateRepository emailTemplateRepository) {
    this.actionRuleRepository = actionRuleRepository;
    this.userIdentity = userIdentity;
    this.collectionExerciseRepository = collectionExerciseRepository;
    this.exportFileTemplateRepository = exportFileTemplateRepository;
    this.smsTemplateRepository = smsTemplateRepository;
    this.emailTemplateRepository = emailTemplateRepository;
  }

  @GetMapping
  public List<ActionRuleDto> findActionRulesByCollex(
      @RequestParam(value = "collectionExercise") UUID collectionExerciseId,
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {

    CollectionExercise collectionExercise =
        collectionExerciseRepository
            .findById(collectionExerciseId)
            .orElseThrow(
                () -> {
                  log.with("collectionExerciseId", collectionExerciseId)
                      .with("httpStatus", HttpStatus.BAD_REQUEST)
                      .with("userEmail", userEmail)
                      .warn("Failed to find action rule, Collection exercise not found");
                  return new ResponseStatusException(
                      HttpStatus.BAD_REQUEST, "Collection exercise not found");
                });

    userIdentity.checkUserPermission(
        userEmail,
        collectionExercise.getSurvey(),
        UserGroupAuthorisedActivityType.LIST_ACTION_RULES);

    List<ActionRule> actionRules =
        actionRuleRepository.findByCollectionExercise(collectionExercise);

    List<ActionRuleDto> actionRuleDtos =
        actionRules.stream()
            .map(
                actionRule -> {
                  ActionRuleDto actionRuleDTO = new ActionRuleDto();
                  actionRuleDTO.setClassifiers(actionRule.getClassifiers());

                  if (actionRule.getType() == ActionRuleType.EXPORT_FILE) {
                    actionRuleDTO.setPackCode(actionRule.getExportFileTemplate().getPackCode());
                  } else if (actionRule.getType() == ActionRuleType.SMS) {
                    actionRuleDTO.setPackCode(actionRule.getSmsTemplate().getPackCode());
                  } else if (actionRule.getType() == ActionRuleType.EMAIL) {
                    actionRuleDTO.setPackCode(actionRule.getEmailTemplate().getPackCode());
                  }

                  actionRuleDTO.setActionRuleId(actionRule.getId());
                  actionRuleDTO.setType(actionRule.getType());
                  actionRuleDTO.setCollectionExerciseId(actionRule.getCollectionExercise().getId());
                  actionRuleDTO.setPhoneNumberColumn(actionRule.getPhoneNumberColumn());
                  actionRuleDTO.setEmailColumn(actionRule.getEmailColumn());
                  actionRuleDTO.setTriggerDateTime(actionRule.getTriggerDateTime());
                  actionRuleDTO.setHasTriggered(actionRule.isHasTriggered());
                  actionRuleDTO.setUacMetadata(actionRule.getUacMetadata());
                  return actionRuleDTO;
                })
            .collect(Collectors.toList());

    return actionRuleDtos;
  }

  @PostMapping
  @Transactional
  public ResponseEntity<UUID> insertActionRules(
      @RequestBody() ActionRuleDto actionRuleDTO,
      @Value("#{request.getAttribute('userEmail')}") String createdBy) {

    CollectionExercise collectionExercise =
        getCollectionExercise(actionRuleDTO.getCollectionExerciseId(), createdBy);
    UserGroupAuthorisedActivityType userActivity;

    ExportFileTemplate exportFileTemplate = null;
    SmsTemplate smsTemplate = null;
    EmailTemplate emailTemplate = null;
    switch (actionRuleDTO.getType()) {
      case EXPORT_FILE:
        userActivity = CREATE_EXPORT_FILE_ACTION_RULE;
        exportFileTemplate =
            exportFileTemplateRepository
                .findById(actionRuleDTO.getPackCode())
                .orElseThrow(
                    () -> {
                      log.with("packcode", actionRuleDTO.getPackCode())
                          .with("httpStatus", HttpStatus.BAD_REQUEST)
                          .with("userEmail", createdBy)
                          .warn("Failed to insert action rule, export file template not found");
                      return new ResponseStatusException(
                          HttpStatus.BAD_REQUEST, "Export file template not found");
                    });
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
                    () -> {
                      log.with("packcode", actionRuleDTO.getPackCode())
                          .with("httpStatus", HttpStatus.BAD_REQUEST)
                          .with("userEmail", createdBy)
                          .warn("Failed to insert action rule, SMS template not found");
                      return new ResponseStatusException(
                          HttpStatus.BAD_REQUEST,
                          "Failed to insert action rule, SMS template not found");
                    });
        if (!getSurveyColumns(collectionExercise.getSurvey(), true)
            .contains(actionRuleDTO.getPhoneNumberColumn())) {
          log.with("requestedColumn", actionRuleDTO.getPhoneNumberColumn())
              .with("httpStatus", HttpStatus.BAD_REQUEST)
              .with("userEmail", createdBy)
              .with("survey", actionRuleDTO)
              .warn("Failed to insert action rule, phone number column does not exist");
          throw new ResponseStatusException(
              HttpStatus.BAD_REQUEST, "Phone number column does not exist");
        }
        break;
      case EMAIL:
        userActivity = CREATE_EMAIL_ACTION_RULE;
        emailTemplate =
            emailTemplateRepository
                .findById(actionRuleDTO.getPackCode())
                .orElseThrow(
                    () -> {
                      log.with("packcode", actionRuleDTO.getPackCode())
                          .with("httpStatus", HttpStatus.BAD_REQUEST)
                          .with("userEmail", createdBy)
                          .warn("Failed to insert action rule, email template not found");
                      return new ResponseStatusException(
                          HttpStatus.BAD_REQUEST, "Email template not found");
                    });
        if (!getSurveyColumns(collectionExercise.getSurvey(), true)
            .contains(actionRuleDTO.getEmailColumn())) {
          log.with("requestedColumn", actionRuleDTO.getEmailColumn())
              .with("httpStatus", HttpStatus.BAD_REQUEST)
              .with("userEmail", createdBy)
              .warn("Failed to insert action rule, email column does not exist");
          throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email column does not exist");
        }
        break;
      case EQ_FLUSH:
        userActivity = CREATE_EQ_FLUSH_ACTION_RULE;
        break;
      default:
        throw new IllegalStateException("Unexpected value: " + actionRuleDTO.getType());
    }

    userIdentity.checkUserPermission(createdBy, collectionExercise.getSurvey(), userActivity);

    ActionRule actionRule = new ActionRule();
    actionRule.setId(UUID.randomUUID());
    actionRule.setClassifiers(actionRuleDTO.getClassifiers());
    actionRule.setExportFileTemplate(exportFileTemplate);
    actionRule.setCollectionExercise(collectionExercise);
    actionRule.setType(actionRuleDTO.getType());
    actionRule.setTriggerDateTime(actionRuleDTO.getTriggerDateTime());
    actionRule.setCreatedBy(createdBy);
    actionRule.setSmsTemplate(smsTemplate);
    actionRule.setPhoneNumberColumn(actionRuleDTO.getPhoneNumberColumn());
    actionRule.setEmailTemplate(emailTemplate);
    actionRule.setEmailColumn(actionRuleDTO.getEmailColumn());
    actionRule.setUacMetadata(actionRuleDTO.getUacMetadata());

    actionRuleRepository.saveAndFlush(actionRule);

    return new ResponseEntity<>(actionRule.getId(), HttpStatus.CREATED);
  }

  /*
   *  Updates triggerDateTime
   */
  @PutMapping
  @Transactional
  public ResponseEntity<UUID> updateActionRules(
      @RequestBody() ActionRuleDto actionRuleDTO,
      @Value("#{request.getAttribute('userEmail')}") String createdBy) {

    CollectionExercise collectionExercise =
        getCollectionExercise(actionRuleDTO.getCollectionExerciseId(), createdBy);

    UserGroupAuthorisedActivityType userActivity =
        switch (actionRuleDTO.getType()) {
          case EXPORT_FILE -> CREATE_EXPORT_FILE_ACTION_RULE;
          case OUTBOUND_TELEPHONE -> CREATE_OUTBOUND_PHONE_ACTION_RULE;
          case FACE_TO_FACE -> CREATE_FACE_TO_FACE_ACTION_RULE;
          case DEACTIVATE_UAC -> CREATE_DEACTIVATE_UAC_ACTION_RULE;
          case SMS -> CREATE_SMS_ACTION_RULE;
          case EMAIL -> CREATE_EMAIL_ACTION_RULE;
          case EQ_FLUSH -> CREATE_EQ_FLUSH_ACTION_RULE;
          default -> throw new IllegalStateException(
              "Unexpected value: " + actionRuleDTO.getType());
        };

    userIdentity.checkUserPermission(createdBy, collectionExercise.getSurvey(), userActivity);

    ActionRule actionRule =
        collectionExercise.getActionRules().stream()
            .filter(action -> action.getId().equals(actionRuleDTO.getActionRuleId()))
            .findAny()
            .orElseThrow(
                () -> {
                  log.with("actionRuleId", actionRuleDTO.getActionRuleId())
                      .with("collectionExerciseId", actionRuleDTO.getCollectionExerciseId())
                      .with("httpStatus", HttpStatus.NOT_FOUND)
                      .with("userEmail", createdBy)
                      .warn("Failed to update action rule, action rule not found");
                  return new ResponseStatusException(
                      HttpStatus.NOT_FOUND, "Action Rule Id not found");
                });

    actionRule.setTriggerDateTime(actionRuleDTO.getTriggerDateTime());

    actionRuleRepository.saveAndFlush(actionRule);

    return new ResponseEntity<>(actionRule.getId(), HttpStatus.OK);
  }

  private CollectionExercise getCollectionExercise(UUID uuid, String createdBy)
      throws ResponseStatusException {
    return collectionExerciseRepository
        .findById(uuid)
        .orElseThrow(
            () -> {
              log.with("collectionExerciseId", uuid)
                  .with("httpStatus", HttpStatus.NOT_FOUND)
                  .with("userEmail", createdBy)
                  .warn("Failed to edit action rules, collection exercise not found");
              return new ResponseStatusException(
                  HttpStatus.NOT_FOUND, "Collection exercise not found");
            });
  }
}
