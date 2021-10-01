package uk.gov.ons.ssdc.supporttool.testhelper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.ons.ssdc.common.model.entity.CollectionExercise;
import uk.gov.ons.ssdc.common.model.entity.Survey;
import uk.gov.ons.ssdc.common.model.entity.User;
import uk.gov.ons.ssdc.common.model.entity.UserGroup;
import uk.gov.ons.ssdc.common.model.entity.UserGroupAuthorisedActivityType;
import uk.gov.ons.ssdc.common.model.entity.UserGroupMember;
import uk.gov.ons.ssdc.common.model.entity.UserGroupPermission;
import uk.gov.ons.ssdc.common.validation.ColumnValidator;
import uk.gov.ons.ssdc.supporttool.model.repository.CollectionExerciseRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.SurveyRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.UserGroupMemberRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.UserGroupPermissionRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.UserGroupRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.UserRepository;

@Component
@ActiveProfiles("test")
public class IntegrationTestHelper {
  private static final RestTemplate restTemplate = new RestTemplate();

  private final SurveyRepository surveyRepository;
  private final CollectionExerciseRepository collectionExerciseRepository;
  private final UserRepository userRepository;
  private final UserGroupRepository userGroupRepository;
  private final UserGroupMemberRepository userGroupMemberRepository;
  private final UserGroupPermissionRepository userGroupPermissionRepository;

  public IntegrationTestHelper(
      SurveyRepository surveyRepository,
      CollectionExerciseRepository collectionExerciseRepository,
      UserRepository userRepository,
      UserGroupRepository userGroupRepository,
      UserGroupMemberRepository userGroupMemberRepository,
      UserGroupPermissionRepository userGroupPermissionRepository) {
    this.surveyRepository = surveyRepository;
    this.collectionExerciseRepository = collectionExerciseRepository;
    this.userRepository = userRepository;
    this.userGroupRepository = userGroupRepository;
    this.userGroupMemberRepository = userGroupMemberRepository;
    this.userGroupPermissionRepository = userGroupPermissionRepository;
  }

  public void testGet(
      int port, UserGroupAuthorisedActivityType activity, BundleFunction bundleFunction) {
    BundleOfUsefulTestStuff bundle = getTestBundle();
    setUpTestUserPermission(activity);

    String url = String.format("http://localhost:%d/api/%s", port, bundleFunction.getUrl(bundle));
    ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
    assertThat(response.getStatusCodeValue()).isBetween(200, 299);

    deleteAllPermissions();

    try {
      restTemplate.getForEntity(url, String.class);
      fail("API call was not forbidden, but should have been");
    } catch (HttpClientErrorException expectedException) {
      assertThat(expectedException.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
  }

  private BundleOfUsefulTestStuff getTestBundle() {
    Survey survey = new Survey();
    survey.setId(UUID.randomUUID());
    survey.setName("Test");
    survey.setSampleWithHeaderRow(true);
    survey.setSampleValidationRules(new ColumnValidator[] {});
    survey.setSampleSeparator(',');
    survey = surveyRepository.saveAndFlush(survey);

    CollectionExercise collectionExercise = new CollectionExercise();
    collectionExercise.setId(UUID.randomUUID());
    collectionExercise.setSurvey(survey);
    collectionExercise.setName("Test");
    collectionExercise = collectionExerciseRepository.saveAndFlush(collectionExercise);

    BundleOfUsefulTestStuff bundle = new BundleOfUsefulTestStuff();
    bundle.setSurveyId(survey.getId());
    bundle.setCollexId(collectionExercise.getId());

    return bundle;
  }

  private void deleteAllPermissions() {
    userGroupPermissionRepository.deleteAllInBatch();
    userGroupMemberRepository.deleteAllInBatch();
    userGroupRepository.deleteAllInBatch();
    userRepository.deleteAllInBatch();
  }

  private void setUpTestUserPermission(UserGroupAuthorisedActivityType authorisedActivity) {
    deleteAllPermissions();

    User user = new User();
    user.setId(UUID.randomUUID());
    user.setEmail("integration-tests@testymctest.com");
    userRepository.saveAndFlush(user);

    UserGroup group = new UserGroup();
    group.setId(UUID.randomUUID());
    group.setName("Test group");
    userGroupRepository.saveAndFlush(group);

    UserGroupMember userGroupMember = new UserGroupMember();
    userGroupMember.setId(UUID.randomUUID());
    userGroupMember.setUser(user);
    userGroupMember.setGroup(group);
    userGroupMemberRepository.saveAndFlush(userGroupMember);

    UserGroupPermission permission = new UserGroupPermission();
    permission.setId(UUID.randomUUID());
    permission.setAuthorisedActivity(authorisedActivity);
    permission.setGroup(group);
    userGroupPermissionRepository.saveAndFlush(permission);
  }
}
