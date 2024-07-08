package uk.gov.ons.ssdc.supporttool.testhelper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.ons.ssdc.common.model.entity.ActionRule;
import uk.gov.ons.ssdc.common.model.entity.ActionRuleStatus;
import uk.gov.ons.ssdc.common.model.entity.ActionRuleType;
import uk.gov.ons.ssdc.common.model.entity.Case;
import uk.gov.ons.ssdc.common.model.entity.CollectionExercise;
import uk.gov.ons.ssdc.common.model.entity.CollectionInstrumentSelectionRule;
import uk.gov.ons.ssdc.common.model.entity.EmailTemplate;
import uk.gov.ons.ssdc.common.model.entity.ExportFileTemplate;
import uk.gov.ons.ssdc.common.model.entity.SmsTemplate;
import uk.gov.ons.ssdc.common.model.entity.Survey;
import uk.gov.ons.ssdc.common.model.entity.UacQidLink;
import uk.gov.ons.ssdc.common.model.entity.User;
import uk.gov.ons.ssdc.common.model.entity.UserGroup;
import uk.gov.ons.ssdc.common.model.entity.UserGroupAdmin;
import uk.gov.ons.ssdc.common.model.entity.UserGroupAuthorisedActivityType;
import uk.gov.ons.ssdc.common.model.entity.UserGroupMember;
import uk.gov.ons.ssdc.common.model.entity.UserGroupPermission;
import uk.gov.ons.ssdc.common.validation.ColumnValidator;
import uk.gov.ons.ssdc.common.validation.Rule;
import uk.gov.ons.ssdc.supporttool.model.repository.ActionRuleRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.CaseRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.CollectionExerciseRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.EmailTemplateRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.ExportFileTemplateRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.SmsTemplateRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.SurveyRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.UacQidLinkRepository;
import uk.gov.ons.ssdc.supporttool.model.repository.UserGroupAdminRepository;
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

  private final ActionRuleRepository actionRuleRepository;

  private final ExportFileTemplateRepository exportFileTemplateRepository;
  private final SmsTemplateRepository smsTemplateRepository;
  private final EmailTemplateRepository emailTemplateRepository;
  private final CaseRepository caseRepository;
  private final UacQidLinkRepository uacQidLinkRepository;

  private final UserRepository userRepository;
  private final UserGroupRepository userGroupRepository;
  private final UserGroupMemberRepository userGroupMemberRepository;
  private final UserGroupAdminRepository userGroupAdminRepository;
  private final UserGroupPermissionRepository userGroupPermissionRepository;

  private static final Map<String, String> TEST_COLLECTION_EXERCISE_UPDATE_METADATA =
      Map.of("TEST_COLLECTION_EXERCISE_UPDATE_METADATA", "TEST");

  public IntegrationTestHelper(
      SurveyRepository surveyRepository,
      CollectionExerciseRepository collectionExerciseRepository,
      ActionRuleRepository actionRuleRepository,
      ExportFileTemplateRepository exportFileTemplateRepository,
      SmsTemplateRepository smsTemplateRepository,
      EmailTemplateRepository emailTemplateRepository,
      CaseRepository caseRepository,
      UacQidLinkRepository uacQidLinkRepository,
      UserRepository userRepository,
      UserGroupRepository userGroupRepository,
      UserGroupMemberRepository userGroupMemberRepository,
      UserGroupAdminRepository userGroupAdminRepository,
      UserGroupPermissionRepository userGroupPermissionRepository) {
    this.surveyRepository = surveyRepository;
    this.collectionExerciseRepository = collectionExerciseRepository;
    this.actionRuleRepository = actionRuleRepository;
    this.exportFileTemplateRepository = exportFileTemplateRepository;
    this.smsTemplateRepository = smsTemplateRepository;
    this.emailTemplateRepository = emailTemplateRepository;
    this.caseRepository = caseRepository;
    this.uacQidLinkRepository = uacQidLinkRepository;
    this.userRepository = userRepository;
    this.userGroupRepository = userGroupRepository;
    this.userGroupMemberRepository = userGroupMemberRepository;
    this.userGroupAdminRepository = userGroupAdminRepository;
    this.userGroupPermissionRepository = userGroupPermissionRepository;
  }

  public void testGet(
      int port, UserGroupAuthorisedActivityType activity, BundleUrlGetter bundleUrlGetter) {
    if (activity != null) {
      setUpTestUserPermission(activity);
    }

    BundleOfUsefulTestStuff bundle = getTestBundle();

    String url = String.format("http://localhost:%d/api/%s", port, bundleUrlGetter.getUrl(bundle));
    ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
    assertThat(response.getStatusCode()).as("GET is OK").isEqualTo(HttpStatus.OK);
    assertThat(response.getHeaders().get("Referrer-Policy").get(0)).isEqualTo("no-referrer");
    assertThat(response.getHeaders().get("Content-Security-Policy").get(0))
        .isEqualTo(
            "default-src 'self'; manifest-src https://cdn.ons.gov.uk/ ; style-src 'self' 'unsafe-inline' ; upgrade-insecure-requests; block-all-mixed-content");
    assertThat(response.getHeaders().get("Strict-Transport-Security").get(0))
        .isEqualTo("max-age=31536000 ; includeSubDomains");
    assertThat(response.getHeaders().get("X-Frame-Options").get(0)).isEqualTo("DENY");
    assertThat(response.getHeaders().get("X-Content-Type-Options").get(0)).isEqualTo("nosniff");
    assertThat(response.getHeaders().get("Permissions-Policy").get(0))
        .isEqualTo(
            "accelerometer=(),autoplay=(),camera=(),display-capture=(),document-domain=(),encrypted-media=(),fullscreen=(),geolocation=(),gyroscope=(),magnetometer=(),microphone=(),midi=(),payment=(),picture-in-picture=(),publickey-credentials-get=(),screen-wake-lock=(),sync-xhr=(self),usb=(),xr-spatial-tracking=()");

    if (activity != null) {
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

  public void testPut(
      int port,
      UserGroupAuthorisedActivityType activity,
      BundleUrlGetter bundleUrlGetter,
      BundlePostObjectGetter bundlePostObjectGetter) {
    setUpTestUserPermission(activity);
    BundleOfUsefulTestStuff bundle = getTestBundle();

    String url = String.format("http://localhost:%d/api/%s", port, bundleUrlGetter.getUrl(bundle));
    Object objectToPost = bundlePostObjectGetter.getObject(bundle);

    restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(objectToPost), String.class);

    deleteAllPermissions();
    restoreDummyUserAndOtherGubbins(bundle); // Restore the user etc so that user tests still work
    try {
      restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(objectToPost), String.class);
      fail("PUT API call was not forbidden, but should have been");
    } catch (HttpClientErrorException expectedException) {
      assertThat(expectedException.getStatusCode())
          .as("PUT is FORBIDDEN")
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
          new ColumnValidator("testPhoneNumber", true, new Rule[] {}),
          new ColumnValidator("testEmail", true, new Rule[] {})
        });
    survey.setSampleSeparator(',');
    survey.setSampleDefinitionUrl("http://foo.bar");
    survey = surveyRepository.saveAndFlush(survey);

    CollectionExercise collectionExercise = new CollectionExercise();
    collectionExercise.setId(UUID.randomUUID());
    collectionExercise.setSurvey(survey);
    collectionExercise.setName("Test");
    collectionExercise.setReference("TEST_REFERENCE");
    collectionExercise.setStartDate(OffsetDateTime.now());
    collectionExercise.setEndDate(OffsetDateTime.now().plusDays(2));
    collectionExercise.setMetadata(TEST_COLLECTION_EXERCISE_UPDATE_METADATA);
    collectionExercise.setCollectionInstrumentSelectionRules(
        new CollectionInstrumentSelectionRule[] {
          new CollectionInstrumentSelectionRule(0, null, "test instrument", null)
        });

    collectionExercise = collectionExerciseRepository.saveAndFlush(collectionExercise);

    Case caze = new Case();
    caze.setId(UUID.randomUUID());
    caze.setCollectionExercise(collectionExercise);
    caze = caseRepository.saveAndFlush(caze);

    UacQidLink uacQidLink = new UacQidLink();
    uacQidLink.setId(UUID.randomUUID());
    uacQidLink.setQid("TEST_QID_" + UUID.randomUUID());
    uacQidLink.setUac("TEST_UAC_" + UUID.randomUUID());
    uacQidLink.setUacHash("test fake hash");
    uacQidLink.setCaze(caze);
    uacQidLink.setCollectionInstrumentUrl("test instrument url");
    uacQidLink = uacQidLinkRepository.saveAndFlush(uacQidLink);

    ExportFileTemplate exportFileTemplate = new ExportFileTemplate();
    exportFileTemplate.setPackCode("TEST_PRINT_PACK_CODE_" + UUID.randomUUID());
    exportFileTemplate.setTemplate(new String[] {"foo", "bar"});
    exportFileTemplate.setExportFileDestination("test_supplier");
    exportFileTemplate.setDescription("Test description");
    exportFileTemplate = exportFileTemplateRepository.saveAndFlush(exportFileTemplate);

    SmsTemplate smsTemplate = new SmsTemplate();
    smsTemplate.setPackCode("TEST_SMS_PACK_CODE_" + UUID.randomUUID());
    smsTemplate.setTemplate(new String[] {"foo", "bar"});
    smsTemplate.setNotifyTemplateId(UUID.randomUUID());
    smsTemplate.setDescription("Test description");
    smsTemplate.setNotifyServiceRef("test_service");
    smsTemplate = smsTemplateRepository.saveAndFlush(smsTemplate);

    EmailTemplate emailTemplate = new EmailTemplate();
    emailTemplate.setPackCode("TEST_EMAIL_PACK_CODE_" + UUID.randomUUID());
    emailTemplate.setTemplate(new String[] {"foo", "bar"});
    emailTemplate.setNotifyTemplateId(UUID.randomUUID());
    emailTemplate.setDescription("Test description");
    emailTemplate.setNotifyServiceRef("test_service");
    emailTemplate = emailTemplateRepository.saveAndFlush(emailTemplate);

    User user = setupDummyUser(UUID.randomUUID());
    UserGroup group = setupDummyGroup(UUID.randomUUID());
    UserGroup secondGroup = setupDummyGroup(UUID.randomUUID());
    UserGroupMember userGroupMember = setupDummyGroupMember(UUID.randomUUID(), user, group);
    UserGroupAdmin userGroupAdmin = setupDummyGroupAdmin(UUID.randomUUID(), user, group);
    UserGroupPermission userGroupPermission = setupDummyGroupPermission(UUID.randomUUID(), group);

    ActionRule actionRule = new ActionRule();
    actionRule.setId(UUID.randomUUID());
    actionRule.setCollectionExercise(collectionExercise);
    actionRule.setType(ActionRuleType.EMAIL);
    actionRule.setClassifiers("sample ->> 'ORG_SIZE' = 'HUGE'");
    actionRule.setTriggerDateTime(OffsetDateTime.now());
    actionRule.setCreatedBy("TEST_USER");
    actionRule.setEmailTemplate(emailTemplate);
    actionRule.setEmailColumn("emailAddress");
    actionRule.setActionRuleStatus(ActionRuleStatus.SCHEDULED);

    actionRuleRepository.saveAndFlush(actionRule);

    collectionExercise.setActionRules(List.of(actionRule));

    BundleOfUsefulTestStuff bundle = new BundleOfUsefulTestStuff();
    bundle.setSurveyId(survey.getId());
    bundle.setCollexId(collectionExercise.getId());
    bundle.setCaseId(caze.getId());
    bundle.setQid(uacQidLink.getQid());
    bundle.setExportFileTemplatePackCode(exportFileTemplate.getPackCode());
    bundle.setSmsTemplatePackCode(smsTemplate.getPackCode());
    bundle.setEmailTemplatePackCode(emailTemplate.getPackCode());
    bundle.setUserId(user.getId());
    bundle.setGroupId(group.getId());
    bundle.setGroupMemberId(userGroupMember.getId());
    bundle.setGroupAdminId(userGroupAdmin.getId());
    bundle.setGroupPermissionId(userGroupPermission.getId());
    bundle.setSecondGroupId(secondGroup.getId());
    bundle.setActionRuleId(collectionExercise.getActionRules().get(0).getId());

    return bundle;
  }

  private void restoreDummyUserAndOtherGubbins(BundleOfUsefulTestStuff bundle) {
    User user = setupDummyUser(bundle.getUserId());
    UserGroup group = setupDummyGroup(bundle.getGroupId());
    setupDummyGroup(bundle.getSecondGroupId());
    setupDummyGroupMember(bundle.getGroupMemberId(), user, group);
    setupDummyGroupAdmin(bundle.getGroupAdminId(), user, group);
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
    group.setName("TEST_GROUP_" + groupId);
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

  private UserGroupAdmin setupDummyGroupAdmin(UUID groupAdminId, User user, UserGroup group) {
    UserGroupAdmin groupAdmin = new UserGroupAdmin();
    groupAdmin.setId(groupAdminId);
    groupAdmin.setGroup(group);
    groupAdmin.setUser(user);
    groupAdmin = userGroupAdminRepository.saveAndFlush(groupAdmin);
    return groupAdmin;
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
    userGroupAdminRepository.deleteAllInBatch();
    userGroupRepository.deleteAllInBatch();
    userRepository.deleteAllInBatch();
  }

  public void setUpTestUserPermission(UserGroupAuthorisedActivityType authorisedActivity) {
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
