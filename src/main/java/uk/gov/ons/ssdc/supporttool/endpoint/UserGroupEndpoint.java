package uk.gov.ons.ssdc.supporttool.endpoint;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
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
import uk.gov.ons.ssdc.common.model.entity.UserGroupAuthorisedActivityType;
import uk.gov.ons.ssdc.supporttool.model.dto.ui.UserGroupDto;
import uk.gov.ons.ssdc.supporttool.model.repository.UserGroupRepository;
import uk.gov.ons.ssdc.supporttool.security.UserIdentity;

@RestController
@RequestMapping(value = "/api/userGroups")
public class UserGroupEndpoint {
  private final UserGroupRepository userGroupRepository;
  private final UserIdentity userIdentity;

  public UserGroupEndpoint(UserGroupRepository userGroupRepository, UserIdentity userIdentity) {
    this.userGroupRepository = userGroupRepository;
    this.userIdentity = userIdentity;
  }

  @GetMapping("/{groupId}")
  public UserGroupDto getUserGroup(
      @PathVariable(value = "groupId") UUID groupId,
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {
    userIdentity.checkGlobalUserPermission(userEmail, UserGroupAuthorisedActivityType.SUPER_USER);

    UserGroup group =
        userGroupRepository
            .findById(groupId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Group not found"));

    return mapDto(group);
  }

  @GetMapping
  public List<UserGroupDto> getUserGroups(
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {
    userIdentity.checkGlobalUserPermission(userEmail, UserGroupAuthorisedActivityType.SUPER_USER);

    return userGroupRepository.findAll().stream().map(this::mapDto).collect(Collectors.toList());
  }

  private UserGroupDto mapDto(UserGroup group) {
    UserGroupDto userGroupDto = new UserGroupDto();
    userGroupDto.setId(group.getId());
    userGroupDto.setName(group.getName());
    return userGroupDto;
  }

  @PostMapping
  public ResponseEntity<Void> createGroup(
      @RequestBody UserGroupDto userGroupDto,
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {
    userIdentity.checkGlobalUserPermission(userEmail, UserGroupAuthorisedActivityType.SUPER_USER);

    UserGroup userGroup = new UserGroup();
    userGroup.setId(UUID.randomUUID());
    userGroup.setName(userGroupDto.getName());
    userGroupRepository.saveAndFlush(userGroup);

    return new ResponseEntity<>(HttpStatus.CREATED);
  }
}
