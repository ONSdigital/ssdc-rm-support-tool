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
import uk.gov.ons.ssdc.common.model.entity.User;
import uk.gov.ons.ssdc.common.model.entity.UserGroup;
import uk.gov.ons.ssdc.common.model.entity.UserGroupAuthorisedActivityType;
import uk.gov.ons.ssdc.common.model.entity.UserGroupMember;
import uk.gov.ons.ssdc.supporttool.model.dto.ui.UserGroupMemberDto;
import uk.gov.ons.ssdc.supporttool.model.repository.UserGroupMemberRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.UserGroupRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.UserRepository;
import uk.gov.ons.ssdc.supporttool.security.AuthUser;

@RestController
@RequestMapping(value = "/api/userGroupMembers")
public class UserGroupMemberEndpoint {
  private static final Logger log = LoggerFactory.getLogger(UserGroupMemberEndpoint.class);

  private final UserGroupMemberRepository userGroupMemberRepository;
  private final AuthUser authUser;
  private final UserRepository userRepository;
  private final UserGroupRepository userGroupRepository;

  public UserGroupMemberEndpoint(
      UserGroupMemberRepository userGroupMemberRepository,
      AuthUser authUser,
      UserRepository userRepository,
      UserGroupRepository userGroupRepository) {
    this.userGroupMemberRepository = userGroupMemberRepository;
    this.authUser = authUser;
    this.userRepository = userRepository;
    this.userGroupRepository = userGroupRepository;
  }

  @GetMapping
  public List<UserGroupMemberDto> findByUser(
      @RequestParam(value = "userId") UUID userId,
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {
    authUser.checkGlobalUserPermission(userEmail, UserGroupAuthorisedActivityType.SUPER_USER);

    User user =
        userRepository
            .findById(userId)
            .orElseThrow(
                () -> {
                  log.atWarn()
                      .setMessage("Failed to find user group member, user not found")
                      .addKeyValue("userId", userId)
                      .addKeyValue("httpStatus", HttpStatus.BAD_REQUEST)
                      .addKeyValue("userEmail", userEmail)
                      .log();
                  return new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found");
                });

    return userGroupMemberRepository.findByUser(user).stream()
        .map(this::mapGroupMember)
        .collect(Collectors.toList());
  }

  @GetMapping("/findByGroup/{groupId}")
  public List<UserGroupMemberDto> findByGroup(
      @PathVariable(value = "groupId") UUID groupId,
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {
    UserGroup group =
        userGroupRepository
            .findById(groupId)
            .orElseThrow(
                () -> {
                  log.atWarn()
                      .setMessage("Failed to find user group member, group not found")
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

    return userGroupMemberRepository.findByGroup(group).stream()
        .map(this::mapGroupMember)
        .collect(Collectors.toList());
  }

  @PostMapping
  public ResponseEntity<Void> addUserToGroup(
      @RequestBody UserGroupMemberDto userGroupMemberDto,
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {
    UserGroup group =
        userGroupRepository
            .findById(userGroupMemberDto.getGroupId())
            .orElseThrow(
                () -> {
                  log.atWarn()
                      .setMessage("Failed to add user to group, group not found")
                      .addKeyValue("groupId", userGroupMemberDto.getGroupId())
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

    User user =
        userRepository
            .findById(userGroupMemberDto.getUserId())
            .orElseThrow(
                () -> {
                  log.atWarn()
                      .setMessage("Failed to add user to group, user not found")
                      .addKeyValue("userId", userGroupMemberDto.getUserId())
                      .addKeyValue("httpStatus", HttpStatus.BAD_REQUEST)
                      .addKeyValue("userEmail", userEmail)
                      .log();
                  return new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found");
                });

    if (user.getMemberOf().stream()
        .anyMatch(userGroupMember -> userGroupMember.getGroup() == group)) {
      log.atWarn()
          .setMessage("Failed to add user to group, user is already a member of this group")
          .addKeyValue("userId", user.getId())
          .addKeyValue("groupId", group.getId())
          .addKeyValue("groupName", group.getName())
          .addKeyValue("userEmail", user.getEmail())
          .addKeyValue("httpStatus", HttpStatus.CONFLICT)
          .addKeyValue("userEmail", userEmail)
          .log();
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "User is already a member of this group");
    }

    UserGroupMember userGroupMember = new UserGroupMember();
    userGroupMember.setId(UUID.randomUUID());
    userGroupMember.setUser(user);
    userGroupMember.setGroup(group);

    userGroupMemberRepository.saveAndFlush(userGroupMember);

    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @DeleteMapping("/{groupMemberId}")
  public void removeUserFromGroup(
      @PathVariable(value = "groupMemberId") UUID groupMemberId,
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {
    UserGroupMember userGroupMember =
        userGroupMemberRepository
            .findById(groupMemberId)
            .orElseThrow(
                () -> {
                  log.atWarn()
                      .setMessage("Failed to remove user from group, group membership not found")
                      .addKeyValue("groupMemberId", groupMemberId)
                      .addKeyValue("httpStatus", HttpStatus.BAD_REQUEST)
                      .addKeyValue("userEmail", userEmail)
                      .log();
                  return new ResponseStatusException(
                      HttpStatus.BAD_REQUEST, "Group membership not found");
                });

    if (userGroupMember.getGroup().getAdmins().stream()
        .noneMatch(groupAdmin -> groupAdmin.getUser().getEmail().equalsIgnoreCase(userEmail))) {
      // If you're not admin of this group, you have to be super user
      authUser.checkGlobalUserPermission(userEmail, UserGroupAuthorisedActivityType.SUPER_USER);
    }

    userGroupMemberRepository.delete(userGroupMember);
  }

  private UserGroupMemberDto mapGroupMember(UserGroupMember member) {
    UserGroupMemberDto userGroupMemberDto = new UserGroupMemberDto();
    userGroupMemberDto.setId(member.getId());
    userGroupMemberDto.setGroupId(member.getGroup().getId());
    userGroupMemberDto.setUserId(member.getUser().getId());
    userGroupMemberDto.setUserEmail(member.getUser().getEmail());
    userGroupMemberDto.setGroupName(member.getGroup().getName());
    userGroupMemberDto.setGroupDescription(member.getGroup().getDescription());
    return userGroupMemberDto;
  }
}
