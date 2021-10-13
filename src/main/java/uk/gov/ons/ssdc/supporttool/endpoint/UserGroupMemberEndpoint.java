package uk.gov.ons.ssdc.supporttool.endpoint;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
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
import uk.gov.ons.ssdc.supporttool.security.UserIdentity;

@RestController
@RequestMapping(value = "/api/userGroupMembers")
public class UserGroupMemberEndpoint {
  private final UserGroupMemberRepository userGroupMemberRepository;
  private final UserIdentity userIdentity;
  private final UserRepository userRepository;
  private final UserGroupRepository userGroupRepository;

  public UserGroupMemberEndpoint(
      UserGroupMemberRepository userGroupMemberRepository,
      UserIdentity userIdentity,
      UserRepository userRepository,
      UserGroupRepository userGroupRepository) {
    this.userGroupMemberRepository = userGroupMemberRepository;
    this.userIdentity = userIdentity;
    this.userRepository = userRepository;
    this.userGroupRepository = userGroupRepository;
  }

  @GetMapping
  public List<UserGroupMemberDto> findByUser(
      @RequestParam(value = "userId") UUID userId,
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {
    userIdentity.checkGlobalUserPermission(userEmail, UserGroupAuthorisedActivityType.SUPER_USER);

    User user =
        userRepository
            .findById(userId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found"));

    return userGroupMemberRepository.findByUser(user).stream()
        .map(
            member -> {
              UserGroupMemberDto userGroupMemberDto = new UserGroupMemberDto();
              userGroupMemberDto.setId(member.getId());
              userGroupMemberDto.setGroupId(member.getGroup().getId());
              userGroupMemberDto.setUserId(userId);
              userGroupMemberDto.setGroupName(member.getGroup().getName());
              return userGroupMemberDto;
            })
        .collect(Collectors.toList());
  }

  @PostMapping
  public ResponseEntity<Void> addUserToGroup(
      @RequestBody UserGroupMemberDto userGroupMemberDto,
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {
    userIdentity.checkGlobalUserPermission(userEmail, UserGroupAuthorisedActivityType.SUPER_USER);

    User user =
        userRepository
            .findById(userGroupMemberDto.getUserId())
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found"));

    UserGroup group =
        userGroupRepository
            .findById(userGroupMemberDto.getGroupId())
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Group not found"));

    if (user.getMemberOf().stream()
        .anyMatch(userGroupMember -> userGroupMember.getGroup() == group)) {
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
    userIdentity.checkGlobalUserPermission(userEmail, UserGroupAuthorisedActivityType.SUPER_USER);

    userGroupMemberRepository.deleteById(groupMemberId);
  }
}
