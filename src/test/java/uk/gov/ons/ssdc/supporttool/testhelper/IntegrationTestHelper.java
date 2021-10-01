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
import uk.gov.ons.ssdc.common.model.entity.Case;
import uk.gov.ons.ssdc.common.model.entity.CollectionExercise;
import uk.gov.ons.ssdc.common.model.entity.PrintTemplate;
import uk.gov.ons.ssdc.common.model.entity.SmsTemplate;
import uk.gov.ons.ssdc.common.model.entity.Survey;
import uk.gov.ons.ssdc.common.model.entity.UacQidLink;
import uk.gov.ons.ssdc.common.model.entity.User;
import uk.gov.ons.ssdc.common.model.entity.UserGroup;
import uk.gov.ons.ssdc.common.model.entity.UserGroupAuthorisedActivityType;
import uk.gov.ons.ssdc.common.model.entity.UserGroupMember;
import uk.gov.ons.ssdc.common.model.entity.UserGroupPermission;
import uk.gov.ons.ssdc.common.validation.ColumnValidator;
import uk.gov.ons.ssdc.common.validation.Rule;
import uk.gov.ons.ssdc.supporttool.model.repository.CaseRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.CollectionExerciseRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.PrintTemplateRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.SmsTemplateRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.SurveyRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.UacQidLinkRepository;
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
  private final PrintTemplateRepository printTemplateRepository;
  private final SmsTemplateRepository smsTemplateRepository;
  private final CaseRepository caseRepository;
  private final UacQidLinkRepository uacQidLinkRepository;

  private final UserRepository userRepository;
  private final UserGroupRepository userGroupRepository;
  private final UserGroupMemberRepository userGroupMemberRepository;
  private final UserGroupPermissionRepository userGroupPermissionRepository;

  public IntegrationTestHelper(
      SurveyRepository surveyRepository,
      CollectionExerciseRepository collectionExerciseRepository,
      PrintTemplateRepository printTemplateRepository,
      SmsTemplateRepository smsTemplateRepository,
      CaseRepository caseRepository,
      UacQidLinkRepository uacQidLinkRepository,
      UserRepository userRepository,
      UserGroupRepository userGroupRepository,
      UserGroupMemberRepository userGroupMemberRepository,
      UserGroupPermissionRepository userGroupPermissionRepository) {
    this.surveyRepository = surveyRepository;
    this.collectionExerciseRepository = collectionExerciseRepository;
    this.printTemplateRepository = printTemplateRepository;
    this.smsTemplateRepository = smsTemplateRepository;
    this.caseRepository = caseRepository;
    this.uacQidLinkRepository = uacQidLinkRepository;
    this.userRepository = userRepository;
    this.userGroupRepository = userGroupRepository;
    this.userGroupMemberRepository = userGroupMemberRepository;
    this.userGroupPermissionRepository = userGroupPermissionRepository;
  }

  public void testGet(
      int port, UserGroupAuthorisedActivityType activity, BundleUrlGetter bundleUrlGetter) {
    setUpTestUserPermission(activity);
    BundleOfUsefulTestStuff bundle = getTestBundle();

    String url = String.format("http://localhost:%d/api/%s", port, bundleUrlGetter.getUrl(bundle));
    ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
    assertThat(response.getStatusCode()).as("GET is OK").isEqualTo(HttpStatus.OK);

    deleteAllPermissions();
    restoreDummyUserAndOtherGubbins(bundle); // Restore the user etc so that user tests still work

    try {
      restTemplate.getForEntity(url, String.class);
      fail("GET API call was not forbidden, but should have been");
    } catch (HttpClientErrorException expectedException) {
      assertThat(expectedException.getStatusCode())
          .as("GET is FORBIDDEN")
          .isEqualTo(HttpStatus.FORBIDDEN);
    }
  }

  public void testPost(
      int port,
      UserGroupAuthorisedActivityType activity,
      BundleUrlGetter bundleUrlGetter,
      BundlePostObjectGetter bundlePostObjectGetter) {
    setUpTestUserPermission(activity);
    BundleOfUsefulTestStuff bundle = getTestBundle();

    String url = String.format("http://localhost:%d/api/%s", port, bundleUrlGetter.getUrl(bundle));
    Object objectToPost = bundlePostObjectGetter.getObject(bundle);
    ResponseEntity<String> response = restTemplate.postForEntity(url, objectToPost, String.class);
    assertThat(response.getStatusCode()).as("POST is CREATED").isEqualTo(HttpStatus.CREATED);

    deleteAllPermissions();
    restoreDummyUserAndOtherGubbins(bundle); // Restore the user etc so that user tests still work

    try {
      restTemplate.postForEntity(url, objectToPost, String.class);
      fail("POST API call was not forbidden, but should have been");
    } catch (HttpClientErrorException expectedException) {
      assertThat(expectedException.getStatusCode())
          .as("POST is FORBIDDEN")
          .isEqualTo(HttpStatus.FORBIDDEN);
    }
  }

  public void testDelete(
      int port, UserGroupAuthorisedActivityType activity, BundleUrlGetter bundleUrlGetter) {
    setUpTestUserPermission(activity);
    BundleOfUsefulTestStuff bundle = getTestBundle();

    String url = String.format("http://localhost:%d/api/%s", port, bundleUrlGetter.getUrl(bundle));
    restTemplate.delete(url);

    deleteAllPermissions();
    restoreDummyUserAndOtherGubbins(bundle); // Restore the user etc so that user tests still work

    try {
      restTemplate.delete(url);
      fail("DELETE API call was not forbidden, but should have been");
    } catch (HttpClientErrorException expectedException) {
      assertThat(expectedException.getStatusCode())
          .as("DELETE is FORBIDDEN")
          .isEqualTo(HttpStatus.FORBIDDEN);
    }
  }

  private BundleOfUsefulTestStuff getTestBundle() {
    Survey survey = new Survey();
    survey.setId(UUID.randomUUID());
    survey.setName("Test");
    survey.setSampleWithHeaderRow(true);
    survey.setSampleValidationRules(
        new ColumnValidator[] {
          new ColumnValidator("foo", false, new Rule[] {}),
          new ColumnValidator("bar", false, new Rule[] {}),
          new ColumnValidator("testPhoneNumber", true, new Rule[] {})
        });
    survey.setSampleSeparator(',');
    survey = surveyRepository.saveAndFlush(survey);

    CollectionExercise collectionExercise = new CollectionExercise();
    collectionExercise.setId(UUID.randomUUID());
    collectionExercise.setSurvey(survey);
    collectionExercise.setName("Test");
    collectionExercise = collectionExerciseRepository.saveAndFlush(collectionExercise);

    Case caze = new Case();
    caze.setId(UUID.randomUUID());
    caze.setCollectionExercise(collectionExercise);
    caze = caseRepository.saveAndFlush(caze);

    UacQidLink uacQidLink = new UacQidLink();
    uacQidLink.setId(UUID.randomUUID());
    uacQidLink.setQid("TEST_QID_" + UUID.randomUUID());
    uacQidLink.setUac("TEST_UAC_" + UUID.randomUUID());
    uacQidLink.setCaze(caze);
    uacQidLink = uacQidLinkRepository.saveAndFlush(uacQidLink);

    PrintTemplate printTemplate = new PrintTemplate();
    printTemplate.setPackCode("TEST_PRINT_PACK_CODE_" + UUID.randomUUID());
    printTemplate.setTemplate(new String[] {"foo", "bar"});
    printTemplate.setPrintSupplier("SUPPLIER_A");
    printTemplate = printTemplateRepository.saveAndFlush(printTemplate);

    SmsTemplate smsTemplate = new SmsTemplate();
    smsTemplate.setPackCode("TEST_SMS_PACK_CODE_" + UUID.randomUUID());
    smsTemplate.setTemplate(new String[] {"foo", "bar"});
    smsTemplate.setNotifyTemplateId(UUID.randomUUID());
    smsTemplate = smsTemplateRepository.saveAndFlush(smsTemplate);

    User user = setupDummyUser(UUID.randomUUID());
    UserGroup group = setupDummyGroup(UUID.randomUUID());
    UserGroupMember userGroupMember = setupDummyGroupMember(UUID.randomUUID(), user, group);
    UserGroupPermission userGroupPermission = setupDummyGroupPermission(UUID.randomUUID(), group);

    BundleOfUsefulTestStuff bundle = new BundleOfUsefulTestStuff();
    bundle.setSurveyId(survey.getId());
    bundle.setCollexId(collectionExercise.getId());
    bundle.setCaseId(caze.getId());
    bundle.setQid(uacQidLink.getQid());
    bundle.setPrintTemplatePackCode(printTemplate.getPackCode());
    bundle.setSmsTemplatePackCode(smsTemplate.getPackCode());
    bundle.setUserId(user.getId());
    bundle.setGroupId(group.getId());
    bundle.setGroupMemberId(userGroupMember.getId());
    bundle.setGroupPermissionId(userGroupPermission.getId());

    return bundle;
  }

  private void restoreDummyUserAndOtherGubbins(BundleOfUsefulTestStuff bundle) {
    User user = setupDummyUser(bundle.getUserId());
    UserGroup group = setupDummyGroup(bundle.getGroupId());
    setupDummyGroupMember(bundle.getGroupMemberId(), user, group);
    setupDummyGroupPermission(bundle.getGroupPermissionId(), group);
  }

  private User setupDummyUser(UUID userId) {
    User user = new User();
    user.setId(userId);
    user.setEmail("TEST_USER_" + UUID.randomUUID());
    user = userRepository.saveAndFlush(user);
    return user;
  }

  private UserGroup setupDummyGroup(UUID groupId) {
    UserGroup group = new UserGroup();
    group.setId(groupId);
    group.setName("Test");
    group = userGroupRepository.saveAndFlush(group);
    return group;
  }

  private UserGroupMember setupDummyGroupMember(UUID groupMemberId, User user, UserGroup group) {
    UserGroupMember groupMember = new UserGroupMember();
    groupMember.setId(groupMemberId);
    groupMember.setGroup(group);
    groupMember.setUser(user);
    groupMember = userGroupMemberRepository.saveAndFlush(groupMember);
    return groupMember;
  }

  private UserGroupPermission setupDummyGroupPermission(UUID groupPermissionId, UserGroup group) {
    UserGroupPermission permission = new UserGroupPermission();
    permission.setId(groupPermissionId);
    permission.setGroup(group);
    permission.setAuthorisedActivity(UserGroupAuthorisedActivityType.LOAD_SAMPLE);
    permission = userGroupPermissionRepository.saveAndFlush(permission);
    return permission;
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
