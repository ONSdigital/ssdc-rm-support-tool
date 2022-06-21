package uk.gov.ons.ssdc.supporttool.endpoint;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.util.List;
import java.util.Map;
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
import uk.gov.ons.ssdc.common.model.entity.User;
import uk.gov.ons.ssdc.common.model.entity.UserGroupAuthorisedActivityType;
import uk.gov.ons.ssdc.supporttool.model.dto.ui.UserDto;
import uk.gov.ons.ssdc.supporttool.model.repository.UserGroupAdminRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.UserRepository;
import uk.gov.ons.ssdc.supporttool.security.UserIdentity;

@RestController
@RequestMapping(value = "/api/users")
public class UserEndpoint {
  private static final Logger log = LoggerFactory.getLogger(UserEndpoint.class);
  private final UserRepository userRepository;
  private final UserIdentity userIdentity;
  private final UserGroupAdminRepository userGroupAdminRepository;

  public UserEndpoint(
      UserRepository userRepository,
      UserIdentity userIdentity,
      UserGroupAdminRepository userGroupAdminRepository) {
    this.userRepository = userRepository;
    this.userIdentity = userIdentity;
    this.userGroupAdminRepository = userGroupAdminRepository;
  }

  @GetMapping("/{userId}")
  public UserDto getUser(
      @PathVariable(value = "userId") UUID userId,
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {
    userIdentity.checkGlobalUserPermission(userEmail, UserGroupAuthorisedActivityType.SUPER_USER);

    User user =
        userRepository
            .findById(userId)
            .orElseThrow(
                () -> {
                  log.warn("{} User not found", HttpStatus.BAD_REQUEST);
                  return new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found");
                });

    return mapDto(user);
  }

  @GetMapping
  public List<UserDto> getUsers(@Value("#{request.getAttribute('userEmail')}") String userEmail) {
    if (!userGroupAdminRepository.existsByUserEmailIgnoreCase(userEmail)) {
      // If you're not admin of a group, you have to be super user
      userIdentity.checkGlobalUserPermission(userEmail, UserGroupAuthorisedActivityType.SUPER_USER);
    }

    return userRepository.findAll().stream().map(this::mapDto).collect(Collectors.toList());
  }

  private UserDto mapDto(User user) {
    UserDto userDto = new UserDto();
    userDto.setId(user.getId());
    userDto.setEmail(user.getEmail());
    return userDto;
  }

  @PostMapping
  public ResponseEntity<?> createUser(
      @RequestBody UserDto userDto,
      @Value("#{request.getAttribute('userEmail')}") String userEmail) {
    userIdentity.checkGlobalUserPermission(userEmail, UserGroupAuthorisedActivityType.SUPER_USER);

    if (userRepository.findByEmailIgnoreCase(userDto.getEmail()).isPresent()) {
      return new ResponseEntity<>(Map.of("errors", "email already exists"), HttpStatus.BAD_REQUEST);
    }

    User user = new User();
    user.setId(UUID.randomUUID());
    user.setEmail(userDto.getEmail());

    userRepository.saveAndFlush(user);

    return new ResponseEntity<>(HttpStatus.CREATED);
  }
}
