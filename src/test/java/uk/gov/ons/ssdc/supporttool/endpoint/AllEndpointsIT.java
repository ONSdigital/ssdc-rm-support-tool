package uk.gov.ons.ssdc.supporttool.endpoint;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.ons.ssdc.common.model.entity.ActionRuleType;
import uk.gov.ons.ssdc.common.model.entity.UserGroupAuthorisedActivityType;
import uk.gov.ons.ssdc.common.validation.ColumnValidator;
import uk.gov.ons.ssdc.common.validation.Rule;
import uk.gov.ons.ssdc.supporttool.model.dto.ui.ActionRuleDto;
import uk.gov.ons.ssdc.supporttool.model.dto.ui.AllowTemplateOnSurvey;
import uk.gov.ons.ssdc.supporttool.model.dto.ui.CollectionExerciseDto;
import uk.gov.ons.ssdc.supporttool.model.dto.ui.PrintTemplateDto;
import uk.gov.ons.ssdc.supporttool.model.dto.ui.SmsTemplateDto;
import uk.gov.ons.ssdc.supporttool.model.dto.ui.SurveyDto;
import uk.gov.ons.ssdc.supporttool.model.dto.ui.UserDto;
import uk.gov.ons.ssdc.supporttool.model.dto.ui.UserGroupDto;
import uk.gov.ons.ssdc.supporttool.model.dto.ui.UserGroupMemberDto;
import uk.gov.ons.ssdc.supporttool.model.dto.ui.UserGroupPermissionDto;
import uk.gov.ons.ssdc.supporttool.testhelper.IntegrationTestHelper;

/* The purpose of these tests is to check, in a quick & dirty way, the endpoints _work_ without
 * being too fussy. There are circa 140 endpoints which support the Support Tool UI, but it remains
 * a tool for developers and testers, so doesn't need to be tested to the same rigorous standards
 * as something which would be used by end-users... it would be too costly!
 *
 * However, one non-negotiable is **SECURITY** so we have to check that our API endpoints are secure
 * which we do with our role-based access control (RBAC) mechanism. To satisfy ourselves that all
 * our endpoints have been correctly secured, we call them with a single security permission:
 * namely the one and only one which _should_ be allowed. Then we call it again, without that
 * permission and check that we get a 403 FORBIDDEN response.
 *
 * The reason why all these tests are in one file is performance: if they were in separate files
 * then Spring Boot would have to start and stop for every endpoint, which would take 30 seconds, so
 * for 26+ endpoints, that would take 13 minutes... unacceptably long for a build cycle.
 *
 * If you want to test for extra stuff, it's the original author's opinion that you should do that
 * in a separate test, rather than bloat these tests. Keep your 'traditional' integration tests,
 * which do checks on database rows and pubsub messages, out of these tests, which are supposed to
 * be quick and easy to write.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class AllEndpointsIT {
  @Autowired private IntegrationTestHelper integrationTestHelper;

  @LocalServerPort private int port;

  private static final Map<String, String> TEST_COLLECTION_EXERCISE_METADATA =
      Map.of("TEST_COLLECTION_EXERCISE_METADATA", "TEST");

  @Value("${queueconfig.collection-exercise-update-event-topic}")
  private String collectionExerciseUpdateEventTopic;

  @Test
  public void testActionRuleEndpoints() {
    integrationTestHelper.testGet(
        port,
        UserGroupAuthorisedActivityType.LIST_ACTION_RULES,
        (bundle) -> String.format("actionRules/?collectionExercise=%s", bundle.getCollexId()));

    integrationTestHelper.testPost(
        port,
        UserGroupAuthorisedActivityType.CREATE_PRINT_ACTION_RULE,
        (bundle) -> "actionRules",
        (bundle) -> {
          ActionRuleDto actionRuleDto = new ActionRuleDto();
          actionRuleDto.setType(ActionRuleType.PRINT);
          actionRuleDto.setCollectionExerciseId(bundle.getCollexId());
          actionRuleDto.setTriggerDateTime(OffsetDateTime.now());
          actionRuleDto.setPackCode(bundle.getPrintTemplatePackCode());
          return actionRuleDto;
        });

    integrationTestHelper.testPost(
        port,
        UserGroupAuthorisedActivityType.CREATE_SMS_ACTION_RULE,
        (bundle) -> "actionRules",
        (bundle) -> {
          ActionRuleDto actionRuleDto = new ActionRuleDto();
          actionRuleDto.setType(ActionRuleType.SMS);
          actionRuleDto.setCollectionExerciseId(bundle.getCollexId());
          actionRuleDto.setTriggerDateTime(OffsetDateTime.now());
          actionRuleDto.setPackCode(bundle.getSmsTemplatePackCode());
          actionRuleDto.setPhoneNumberColumn("testPhoneNumber");
          return actionRuleDto;
        });

    integrationTestHelper.testPost(
        port,
        UserGroupAuthorisedActivityType.CREATE_OUTBOUND_PHONE_ACTION_RULE,
        (bundle) -> "actionRules",
        (bundle) -> {
          ActionRuleDto actionRuleDto = new ActionRuleDto();
          actionRuleDto.setType(ActionRuleType.OUTBOUND_TELEPHONE);
          actionRuleDto.setCollectionExerciseId(bundle.getCollexId());
          actionRuleDto.setTriggerDateTime(OffsetDateTime.now());
          return actionRuleDto;
        });

    integrationTestHelper.testPost(
        port,
        UserGroupAuthorisedActivityType.CREATE_FACE_TO_FACE_ACTION_RULE,
        (bundle) -> "actionRules",
        (bundle) -> {
          ActionRuleDto actionRuleDto = new ActionRuleDto();
          actionRuleDto.setType(ActionRuleType.FACE_TO_FACE);
          actionRuleDto.setCollectionExerciseId(bundle.getCollexId());
          actionRuleDto.setTriggerDateTime(OffsetDateTime.now());
          return actionRuleDto;
        });

    integrationTestHelper.testPost(
        port,
        UserGroupAuthorisedActivityType.CREATE_DEACTIVATE_UAC_ACTION_RULE,
        (bundle) -> "actionRules",
        (bundle) -> {
          ActionRuleDto actionRuleDto = new ActionRuleDto();
          actionRuleDto.setType(ActionRuleType.DEACTIVATE_UAC);
          actionRuleDto.setCollectionExerciseId(bundle.getCollexId());
          actionRuleDto.setTriggerDateTime(OffsetDateTime.now());
          return actionRuleDto;
        });
  }

  @Test
  public void testActionRuleSurveyPrintTemplateEndpoints() {
    integrationTestHelper.testGet(
        port,
        UserGroupAuthorisedActivityType.LIST_ALLOWED_PRINT_TEMPLATES_ON_ACTION_RULES,
        (bundle) ->
            String.format("actionRuleSurveyPrintTemplates/?surveyId=%s", bundle.getSurveyId()));

    integrationTestHelper.testPost(
        port,
        UserGroupAuthorisedActivityType.ALLOW_PRINT_TEMPLATE_ON_ACTION_RULE,
        (bundle) -> "actionRuleSurveyPrintTemplates",
        (bundle) -> {
          AllowTemplateOnSurvey allowTemplateOnSurvey = new AllowTemplateOnSurvey();
          allowTemplateOnSurvey.setSurveyId(bundle.getSurveyId());
          allowTemplateOnSurvey.setPackCode(bundle.getPrintTemplatePackCode());
          return allowTemplateOnSurvey;
        });
  }

  @Test
  public void testActionRuleSurveySmsTemplateEndpoints() {
    integrationTestHelper.testGet(
        port,
        UserGroupAuthorisedActivityType.LIST_ALLOWED_SMS_TEMPLATES_ON_ACTION_RULES,
        (bundle) ->
            String.format("actionRuleSurveySmsTemplates/?surveyId=%s", bundle.getSurveyId()));

    integrationTestHelper.testPost(
        port,
        UserGroupAuthorisedActivityType.ALLOW_SMS_TEMPLATE_ON_ACTION_RULE,
        (bundle) -> "actionRuleSurveySmsTemplates",
        (bundle) -> {
          AllowTemplateOnSurvey allowTemplateOnSurvey = new AllowTemplateOnSurvey();
          allowTemplateOnSurvey.setSurveyId(bundle.getSurveyId());
          allowTemplateOnSurvey.setPackCode(bundle.getSmsTemplatePackCode());
          return allowTemplateOnSurvey;
        });
  }

  @Test
  public void testCaseEndpoints() {
    integrationTestHelper.testGet(
        port,
        UserGroupAuthorisedActivityType.VIEW_CASE_DETAILS,
        (bundle) -> String.format("cases/%s", bundle.getCaseId()));
  }

  @Test
  public void testCollectionExerciseEndpoints() {
    integrationTestHelper.testGet(
        port,
        UserGroupAuthorisedActivityType.VIEW_COLLECTION_EXERCISE,
        (bundle) -> String.format("collectionExercises/%s", bundle.getCollexId()));

    integrationTestHelper.testGet(
        port,
        UserGroupAuthorisedActivityType.LIST_COLLECTION_EXERCISES,
        (bundle) -> String.format("collectionExercises/?surveyId=%s", bundle.getSurveyId()));

    integrationTestHelper.testPost(
        port,
        UserGroupAuthorisedActivityType.CREATE_COLLECTION_EXERCISE,
        (bundle) -> "collectionExercises",
        (bundle) -> {
          CollectionExerciseDto collectionExerciseDto = new CollectionExerciseDto();
          collectionExerciseDto.setSurveyId(bundle.getSurveyId());
          collectionExerciseDto.setName("Test");
          collectionExerciseDto.setReference("TEST_REFERENCE");
          collectionExerciseDto.setStartDate(OffsetDateTime.now());
          collectionExerciseDto.setEndDate(OffsetDateTime.now().plusDays(2));
          collectionExerciseDto.setMetadata(TEST_COLLECTION_EXERCISE_METADATA);
          return collectionExerciseDto;
        });
  }

  @Test
  public void testDeactivateUacEndpoints() {
    integrationTestHelper.testGet(
        port,
        UserGroupAuthorisedActivityType.DEACTIVATE_UAC,
        (bundle) -> String.format("deactivateUac/%s", bundle.getQid()));
  }

  @Test
  public void testFulfilmentSurveyPrintTemplateEndpoints() {
    integrationTestHelper.testGet(
        port,
        UserGroupAuthorisedActivityType.LIST_ALLOWED_PRINT_TEMPLATES_ON_FULFILMENTS,
        (bundle) ->
            String.format("fulfilmentSurveyPrintTemplates/?surveyId=%s", bundle.getSurveyId()));

    integrationTestHelper.testPost(
        port,
        UserGroupAuthorisedActivityType.ALLOW_PRINT_TEMPLATE_ON_FULFILMENT,
        (bundle) -> "fulfilmentSurveyPrintTemplates",
        (bundle) -> {
          AllowTemplateOnSurvey allowTemplateOnSurvey = new AllowTemplateOnSurvey();
          allowTemplateOnSurvey.setSurveyId(bundle.getSurveyId());
          allowTemplateOnSurvey.setPackCode(bundle.getPrintTemplatePackCode());
          return allowTemplateOnSurvey;
        });
  }

  @Test
  public void testFulfilmentSurveySmsTemplateEndpoints() {
    integrationTestHelper.testGet(
        port,
        UserGroupAuthorisedActivityType.LIST_ALLOWED_SMS_TEMPLATES_ON_FULFILMENTS,
        (bundle) ->
            String.format("fulfilmentSurveySmsTemplates/?surveyId=%s", bundle.getSurveyId()));

    integrationTestHelper.testPost(
        port,
        UserGroupAuthorisedActivityType.ALLOW_SMS_TEMPLATE_ON_FULFILMENT,
        (bundle) -> "fulfilmentSurveySmsTemplates",
        (bundle) -> {
          AllowTemplateOnSurvey allowTemplateOnSurvey = new AllowTemplateOnSurvey();
          allowTemplateOnSurvey.setSurveyId(bundle.getSurveyId());
          allowTemplateOnSurvey.setPackCode(bundle.getSmsTemplatePackCode());
          return allowTemplateOnSurvey;
        });
  }

  @Test
  public void testPrintSuppliersEndpoints() {
    integrationTestHelper.testGet(
        port, UserGroupAuthorisedActivityType.LIST_PRINT_SUPPLIERS, (bundle) -> "printsuppliers");
  }

  @Test
  public void testPrintTemplateEndpoints() {
    integrationTestHelper.testGet(
        port, UserGroupAuthorisedActivityType.LIST_PRINT_TEMPLATES, (bundle) -> "printTemplates");

    integrationTestHelper.testPost(
        port,
        UserGroupAuthorisedActivityType.CREATE_PRINT_TEMPLATE,
        (bundle) -> "printTemplates",
        (bundle) -> {
          PrintTemplateDto printTemplateDto = new PrintTemplateDto();
          printTemplateDto.setTemplate(new String[] {"foo"});
          printTemplateDto.setPrintSupplier("SUPPLIER_A");
          printTemplateDto.setPackCode("TEST_" + UUID.randomUUID());
          return printTemplateDto;
        });
  }

  @Test
  public void testSmsTemplateEndpoints() {
    integrationTestHelper.testGet(
        port, UserGroupAuthorisedActivityType.LIST_SMS_TEMPLATES, (bundle) -> "smsTemplates");

    integrationTestHelper.testPost(
        port,
        UserGroupAuthorisedActivityType.CREATE_SMS_TEMPLATE,
        (bundle) -> "smsTemplates",
        (bundle) -> {
          SmsTemplateDto smsTemplateDto = new SmsTemplateDto();
          smsTemplateDto.setTemplate(new String[] {"foo"});
          smsTemplateDto.setNotifyTemplateId(UUID.randomUUID());
          smsTemplateDto.setPackCode("TEST_" + UUID.randomUUID());
          return smsTemplateDto;
        });
  }

  @Test
  public void testSurveyEndpoints() {
    integrationTestHelper.testGet(
        port,
        UserGroupAuthorisedActivityType.VIEW_SURVEY,
        (bundle) -> String.format("surveys/%s", bundle.getSurveyId()));

    integrationTestHelper.testPost(
        port,
        UserGroupAuthorisedActivityType.CREATE_SURVEY,
        (bundle) -> "surveys",
        (bundle) -> {
          SurveyDto surveyDto = new SurveyDto();
          surveyDto.setName("Test");
          surveyDto.setSampleSeparator(',');
          surveyDto.setSampleValidationRules(
              new ColumnValidator[] {new ColumnValidator("foo", false, new Rule[] {})});
          return surveyDto;
        });
  }

  @Test
  public void testUserEndpoints() {
    integrationTestHelper.testGet(
        port,
        UserGroupAuthorisedActivityType.SUPER_USER,
        (bundle) -> String.format("users/%s", bundle.getUserId()));

    integrationTestHelper.testGet(
        port, UserGroupAuthorisedActivityType.SUPER_USER, (bundle) -> "users");

    integrationTestHelper.testPost(
        port,
        UserGroupAuthorisedActivityType.SUPER_USER,
        (bundle) -> "users",
        (bundle) -> {
          UserDto userDto = new UserDto();
          userDto.setEmail("TEST_EMAIL_" + UUID.randomUUID());
          return userDto;
        });
  }

  @Test
  public void testUserGroupEndpoints() {
    integrationTestHelper.testGet(
        port,
        UserGroupAuthorisedActivityType.SUPER_USER,
        (bundle) -> String.format("userGroups/%s", bundle.getGroupId()));

    integrationTestHelper.testGet(
        port, UserGroupAuthorisedActivityType.SUPER_USER, (bundle) -> "userGroups");

    integrationTestHelper.testPost(
        port,
        UserGroupAuthorisedActivityType.SUPER_USER,
        (bundle) -> "userGroups",
        (bundle) -> {
          UserGroupDto userGroupDto = new UserGroupDto();
          userGroupDto.setName("Test");
          return userGroupDto;
        });
  }

  @Test
  public void testUserGroupMemberEndpoints() {
    integrationTestHelper.testGet(
        port,
        UserGroupAuthorisedActivityType.SUPER_USER,
        (bundle) -> String.format("userGroupMembers/?userId=%s", bundle.getUserId()));

    integrationTestHelper.testPost(
        port,
        UserGroupAuthorisedActivityType.SUPER_USER,
        (bundle) -> "userGroupMembers",
        (bundle) -> {
          UserGroupMemberDto userGroupMemberDto = new UserGroupMemberDto();
          userGroupMemberDto.setUserId(bundle.getUserId());
          userGroupMemberDto.setGroupId(bundle.getGroupId());
          return userGroupMemberDto;
        });

    integrationTestHelper.testDelete(
        port,
        UserGroupAuthorisedActivityType.SUPER_USER,
        (bundle) -> String.format("userGroupMembers/%s", bundle.getGroupMemberId()));
  }

  @Test
  public void testUserGroupPermissionEndpoints() {
    integrationTestHelper.testGet(
        port,
        UserGroupAuthorisedActivityType.SUPER_USER,
        (bundle) -> String.format("userGroupPermissions/?groupId=%s", bundle.getGroupId()));

    integrationTestHelper.testPost(
        port,
        UserGroupAuthorisedActivityType.SUPER_USER,
        (bundle) -> "userGroupPermissions",
        (bundle) -> {
          UserGroupPermissionDto groupPermissionDto = new UserGroupPermissionDto();
          groupPermissionDto.setGroupId(bundle.getGroupId());
          groupPermissionDto.setSurveyId(bundle.getSurveyId());
          groupPermissionDto.setAuthorisedActivity(UserGroupAuthorisedActivityType.LOAD_SAMPLE);
          return groupPermissionDto;
        });

    integrationTestHelper.testDelete(
        port,
        UserGroupAuthorisedActivityType.SUPER_USER,
        (bundle) -> String.format("userGroupPermissions/%s", bundle.getGroupPermissionId()));
  }
}
