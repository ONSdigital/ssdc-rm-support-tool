package uk.gov.ons.ssdc.supporttool.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.ons.ssdc.common.model.entity.*;
import uk.gov.ons.ssdc.supporttool.model.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class IAPUserTest {

  @InjectMocks IAPUser underTest;
  @Mock UserRepository userRepository;

  @Test
  void testGetUserGroupPermissionForbidden() {
    // Given
    User user = new User();
    user.setId(UUID.randomUUID());
    user.setEmail("unit-tests@testymctest.com");

    UserGroup group = new UserGroup();
    group.setId(UUID.randomUUID());
    group.setName("Test group");

    UserGroupMember userGroupMember = new UserGroupMember();
    userGroupMember.setId(UUID.randomUUID());
    userGroupMember.setUser(user);
    userGroupMember.setGroup(group);

    UserGroupPermission permission = new UserGroupPermission();
    permission.setId(UUID.randomUUID());
    permission.setAuthorisedActivity(UserGroupAuthorisedActivityType.VIEW_COLLECTION_EXERCISE);
    permission.setGroup(group);

    group.setPermissions(List.of(permission));

    user.setMemberOf(List.of(userGroupMember));
    // When
    when(userRepository.findByEmailIgnoreCase(user.getEmail())).thenReturn(Optional.of(user));

    ResponseStatusException thrown =
        assertThrows(
            ResponseStatusException.class,
            () ->
                underTest.checkUserPermission(
                    "unit-tests@testymctest.com",
                    UUID.randomUUID(),
                    UserGroupAuthorisedActivityType.VIEW_SURVEY));
    // Then
    assertThat(thrown.getMessage())
        .isEqualTo(
            String.format(
                "%s \"User not authorised for activity %s\"",
                thrown.getStatusCode(), UserGroupAuthorisedActivityType.VIEW_SURVEY));
  }
}
