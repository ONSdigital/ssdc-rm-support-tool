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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.ons.ssdc.common.model.entity.User;
import uk.gov.ons.ssdc.common.model.entity.UserGroup;
import uk.gov.ons.ssdc.common.model.entity.UserGroupAdmin;
import uk.gov.ons.ssdc.common.model.entity.UserGroupAuthorisedActivityType;
import uk.gov.ons.ssdc.supporttool.model.dto.ui.UserGroupAdminDto;
import uk.gov.ons.ssdc.supporttool.model.repository.UserGroupAdminRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.UserGroupRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.UserRepository;
import uk.gov.ons.ssdc.supporttool.security.AuthUser;

@RestController
@RequestMapping(value = "/api/userGroupAdmins")
public class UserGroupAdminEndpoint {
  private static final Logger log = LoggerFactory.getLogger(UserGroupAdminEndpoint.class);
  private final UserGroupAdminRepository userGroupAdminRepository;
  private final AuthUser authUser;
  private final UserRepository userRepository;
  private final UserGroupRepository userGroupRepository;

  public UserGroupAdminEndpoint(
      UserGroupAdminRepository userGroupAdminRepository,
      AuthUser authUser,
      UserRepository userRepository,
      UserGroupRepository userGroupRepository) {
    this.userGroupAdminRepository = userGroupAdminRepository;
    this.authUser = authUser;
    this.userRepository = userRepository;
    this.userGroupRepository = userGroupRepository;
  }

  @GetMapping("/findByGroup/{groupId}")
  public List<UserGroupAdminDto> findByGroup(
      @PathVariable(value = "groupId") UUID groupId,
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {
    authUser.checkGlobalUserPermission(userEmail, UserGroupAuthorisedActivityType.SUPER_USER);

    return userGroupAdminRepository.findByGroupId(groupId).stream()
        .map(
            admin -> {
              UserGroupAdminDto userGroupAdminDto = new UserGroupAdminDto();
              userGroupAdminDto.setId(admin.getId());
              userGroupAdminDto.setGroupId(admin.getGroup().getId());
              userGroupAdminDto.setUserId(admin.getUser().getId());
              userGroupAdminDto.setUserEmail(admin.getUser().getEmail());
              return userGroupAdminDto;
            })
        .collect(Collectors.toList());
  }

  @PostMapping
  public ResponseEntity<Void> addUserToGroupAdmins(
      @RequestBody UserGroupAdminDto userGroupAdminDto,
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {
    authUser.checkGlobalUserPermission(userEmail, UserGroupAuthorisedActivityType.SUPER_USER);

    User user =
        userRepository
            .findById(userGroupAdminDto.getUserId())
            .orElseThrow(
                () -> {
                  log.atWarn()
                      .setMessage("Failed to add user group admin, user not found")
                      .addKeyValue("userId", userGroupAdminDto.getUserId())
                      .addKeyValue("userEmail", userEmail)
                      .addKeyValue("httpStatus", HttpStatus.BAD_REQUEST)
                      .log();
                  return new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found");
                });

    UserGroup group =
        userGroupRepository
            .findById(userGroupAdminDto.getGroupId())
            .orElseThrow(
                () -> {
                  log.atWarn()
                      .setMessage("Failed to add user group admin, group not found")
                      .addKeyValue("groupId", userGroupAdminDto.getGroupId())
                      .addKeyValue("userEmail", userEmail)
                      .addKeyValue("httpStatus", HttpStatus.BAD_REQUEST)
                      .log();
                  return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Group not found");
                });

    if (user.getAdminOf().stream().anyMatch(userGroupAdmin -> userGroupAdmin.getGroup() == group)) {
      log.atWarn()
          .setMessage("Failed to add user group admin, user is already an admin of this group")
          .addKeyValue("userId", user.getId())
          .addKeyValue("groupId", group.getId())
          .addKeyValue("groupName", group.getName())
          .addKeyValue("userEmail", user.getEmail())
          .addKeyValue("httpStatus", HttpStatus.CONFLICT)
          .addKeyValue("userEmail", userEmail)
          .log();
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "User is already an admin of this group");
    }

    UserGroupAdmin userGroupAdmin = new UserGroupAdmin();
    userGroupAdmin.setId(UUID.randomUUID());
    userGroupAdmin.setUser(user);
    userGroupAdmin.setGroup(group);

    userGroupAdminRepository.saveAndFlush(userGroupAdmin);

    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @DeleteMapping("/{groupAdminId}")
  public void removeAdminFromGroup(
      @PathVariable(value = "groupAdminId") UUID groupAdminId,
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {
    authUser.checkGlobalUserPermission(userEmail, UserGroupAuthorisedActivityType.SUPER_USER);

    userGroupAdminRepository.deleteById(groupAdminId);
  }
}
