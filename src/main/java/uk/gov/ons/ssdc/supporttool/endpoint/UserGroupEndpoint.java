package uk.gov.ons.ssdc.supporttool.endpoint;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.ons.ssdc.common.model.entity.UserGroup;
import uk.gov.ons.ssdc.common.model.entity.UserGroupAdmin;
import uk.gov.ons.ssdc.common.model.entity.UserGroupAuthorisedActivityType;
import uk.gov.ons.ssdc.supporttool.model.dto.ui.UserGroupDto;
import uk.gov.ons.ssdc.supporttool.model.repository.UserGroupAdminRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.UserGroupRepository;
import uk.gov.ons.ssdc.supporttool.security.AuthUser;

@RestController
@RequestMapping(value = "/api/userGroups")
public class UserGroupEndpoint {
  private static final Logger log = LoggerFactory.getLogger(UserGroupEndpoint.class);

  private final UserGroupRepository userGroupRepository;
  private final AuthUser authUser;
  private final UserGroupAdminRepository userGroupAdminRepository;

  public UserGroupEndpoint(
      UserGroupRepository userGroupRepository,
      AuthUser authUser,
      UserGroupAdminRepository userGroupAdminRepository) {
    this.userGroupRepository = userGroupRepository;
    this.authUser = authUser;
    this.userGroupAdminRepository = userGroupAdminRepository;
  }

  @GetMapping("/{groupId}")
  public UserGroupDto getUserGroup(
      @PathVariable(value = "groupId") UUID groupId,
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {
    UserGroup group =
        userGroupRepository
            .findById(groupId)
            .orElseThrow(
                () -> {
                  log.atWarn()
                      .setMessage("Failed to get user group, group not found")
                      .addKeyValue("groupId", groupId)
                      .addKeyValue("httpStatus", HttpStatus.BAD_REQUEST)
                      .addKeyValue("userEmail", userEmail)
                      .log();
                  return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Group not found");
                });

    if (group.getAdmins().stream()
        .noneMatch(groupAdmin -> groupAdmin.getUser().getEmail().equalsIgnoreCase(userEmail))) {
      // If you're not admin of this group, you have to be super user
      authUser.checkGlobalUserPermission(userEmail, UserGroupAuthorisedActivityType.SUPER_USER);
    }

    return mapDto(group);
  }

  @GetMapping
  public List<UserGroupDto> getUserGroups(
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {
    authUser.checkGlobalUserPermission(userEmail, UserGroupAuthorisedActivityType.SUPER_USER);

    return userGroupRepository.findAll().stream().map(this::mapDto).collect(Collectors.toList());
  }

  @GetMapping("/thisUserAdminGroups")
  public List<UserGroupDto> getUserAdminGroups(
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {
    return userGroupAdminRepository.findByUserEmailIgnoreCase(userEmail).stream()
        .map(UserGroupAdmin::getGroup)
        .map(this::mapDto)
        .collect(Collectors.toList());
  }

  private UserGroupDto mapDto(UserGroup group) {
    UserGroupDto userGroupDto = new UserGroupDto();
    userGroupDto.setId(group.getId());
    userGroupDto.setName(group.getName());
    userGroupDto.setDescription(group.getDescription());
    return userGroupDto;
  }

  @PostMapping
  public ResponseEntity<Void> createGroup(
      @RequestBody UserGroupDto userGroupDto,
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {
    authUser.checkGlobalUserPermission(userEmail, UserGroupAuthorisedActivityType.SUPER_USER);

    if (userGroupRepository.existsByName(userGroupDto.getName())) {
      log.atWarn()
          .setMessage("Failed to create group, group name already exists")
          .addKeyValue("groupId", userGroupDto.getId())
          .addKeyValue("groupName", userGroupDto.getName())
          .addKeyValue("userEmail", userEmail)
          .addKeyValue("httpStatus", HttpStatus.CONFLICT)
          .log();
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Group name already exists");
    }

    UserGroup userGroup = new UserGroup();
    userGroup.setId(UUID.randomUUID());
    userGroup.setName(userGroupDto.getName());
    userGroup.setDescription(userGroupDto.getDescription());
    userGroupRepository.saveAndFlush(userGroup);

    return new ResponseEntity<>(HttpStatus.CREATED);
  }
}
