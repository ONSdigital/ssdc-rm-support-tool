package uk.gov.ons.ssdc.supporttool.endpoint;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.ons.ssdc.common.model.entity.Survey;
import uk.gov.ons.ssdc.common.model.entity.UserGroup;
import uk.gov.ons.ssdc.common.model.entity.UserGroupAuthorisedActivityType;
import uk.gov.ons.ssdc.common.model.entity.UserGroupPermission;
import uk.gov.ons.ssdc.supporttool.model.dto.ui.UserGroupPermissionDto;
import uk.gov.ons.ssdc.supporttool.model.repository.SurveyRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.UserGroupPermissionRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.UserGroupRepository;
import uk.gov.ons.ssdc.supporttool.security.AuthUser;

@RestController
@RequestMapping(value = "/api/userGroupPermissions")
public class UserGroupPermissionEndpoint {
  private static final Logger log = LoggerFactory.getLogger(UserGroupPermissionEndpoint.class);

  private final UserGroupPermissionRepository userGroupPermissionRepository;
  private final AuthUser authUser;
  private final UserGroupRepository userGroupRepository;
  private final SurveyRepository surveyRepository;

  public UserGroupPermissionEndpoint(
      UserGroupPermissionRepository userGroupPermissionRepository,
      AuthUser authUser,
      UserGroupRepository userGroupRepository,
      SurveyRepository surveyRepository) {
    this.userGroupPermissionRepository = userGroupPermissionRepository;
    this.authUser = authUser;
    this.userGroupRepository = userGroupRepository;
    this.surveyRepository = surveyRepository;
  }

  @GetMapping
  public List<UserGroupPermissionDto> findByGroup(
      @RequestParam(value = "groupId") UUID groupId,
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {
    authUser.checkGlobalUserPermission(userEmail, UserGroupAuthorisedActivityType.SUPER_USER);

    UserGroup group =
        userGroupRepository
            .findById(groupId)
            .orElseThrow(
                () -> {
                  log.atWarn()
                      .setMessage("Failed to find user group permissiosn, group not found")
                      .addKeyValue("groupId", groupId)
                      .addKeyValue("userEmail", userEmail)
                      .addKeyValue("httpStatus", HttpStatus.BAD_REQUEST)
                      .log();
                  ;
                  return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Group not found");
                });

    return userGroupPermissionRepository.findByGroup(group).stream()
        .map(
            permission -> {
              UserGroupPermissionDto userGroupPermissionDto = new UserGroupPermissionDto();
              userGroupPermissionDto.setGroupId(permission.getGroup().getId());
              userGroupPermissionDto.setId(permission.getId());
              userGroupPermissionDto.setAuthorisedActivity(permission.getAuthorisedActivity());

              if (permission.getSurvey() != null) {
                userGroupPermissionDto.setSurveyId(permission.getSurvey().getId());
                userGroupPermissionDto.setSurveyName(permission.getSurvey().getName());
              }

              return userGroupPermissionDto;
            })
        .collect(Collectors.toList());
  }

  @PostMapping
  public ResponseEntity<Void> addPermissionToGroup(
      @RequestBody UserGroupPermissionDto userGroupPermissionDto,
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {
    authUser.checkGlobalUserPermission(userEmail, UserGroupAuthorisedActivityType.SUPER_USER);

    UserGroup group =
        userGroupRepository
            .findById(userGroupPermissionDto.getGroupId())
            .orElseThrow(
                () -> {
                  log.atWarn()
                      .setMessage("Failed to add permission to group, group not found")
                      .addKeyValue("groupId", userGroupPermissionDto.getGroupId())
                      .addKeyValue("userEmail", userEmail)
                      .addKeyValue("httpStatus", HttpStatus.BAD_REQUEST)
                      .log();
                  return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Group not found");
                });

    Survey survey = null;
    if (userGroupPermissionDto.getSurveyId() != null) {
      if (userGroupPermissionDto.getAuthorisedActivity().isGlobal()) {
        // Not allowed to use a global permission on a specific survey... doesn't work; nonsensical!
        log.atWarn()
            .setMessage("Failed to add permission to group, global permissions must be global")
            .addKeyValue(
                "authorisedActivity", userGroupPermissionDto.getAuthorisedActivity().name())
            .addKeyValue("httpStatus", HttpStatus.BAD_REQUEST)
            .addKeyValue("userEmail", userEmail)
            .log();
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, "Global permissions must be global");
      }

      survey =
          surveyRepository
              .findById(userGroupPermissionDto.getSurveyId())
              .orElseThrow(
                  () -> {
                    log.atWarn()
                        .setMessage("Failed to add permission to group, survey not found")
                        .addKeyValue("surveyId", userGroupPermissionDto.getSurveyId())
                        .addKeyValue("httpStatus", HttpStatus.BAD_REQUEST)
                        .addKeyValue("userEmail", userEmail)
                        .log();
                    return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Survey not found");
                  });
    }

    for (UserGroupPermission existingPermission : group.getPermissions()) {
      // Check if both the activity and survey are equal to any existing permission on this group
      // Note: Either survey can be null, so the survey comparison must be null safe on both values
      if (existingPermission
              .getAuthorisedActivity()
              .equals(userGroupPermissionDto.getAuthorisedActivity())
          && ((survey == null && existingPermission.getSurvey() == null)
              || (existingPermission.getSurvey() != null
                  && existingPermission.getSurvey().equals(survey)))) {
        log.atWarn()
            .setMessage("Failed to add permission to group, permission already exists")
            .addKeyValue("authorisedActivity", existingPermission.getAuthorisedActivity())
            .addKeyValue("surveyName", survey.getName())
            .addKeyValue("userEmail", userEmail)
            .addKeyValue("httpStatus", HttpStatus.CONFLICT)
            .log();
        throw new ResponseStatusException(HttpStatus.CONFLICT, "Permission already exists");
      }
    }

    UserGroupPermission userGroupPermission = new UserGroupPermission();
    userGroupPermission.setId(UUID.randomUUID());
    userGroupPermission.setGroup(group);
    userGroupPermission.setAuthorisedActivity(userGroupPermissionDto.getAuthorisedActivity());
    userGroupPermission.setSurvey(survey);

    userGroupPermissionRepository.saveAndFlush(userGroupPermission);

    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @DeleteMapping("/{groupPermissionId}")
  public void revokePermissionFromGroup(
      @PathVariable(value = "groupPermissionId") UUID groupPermissionId,
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {
    authUser.checkGlobalUserPermission(userEmail, UserGroupAuthorisedActivityType.SUPER_USER);

    userGroupPermissionRepository.deleteById(groupPermissionId);
  }
}
