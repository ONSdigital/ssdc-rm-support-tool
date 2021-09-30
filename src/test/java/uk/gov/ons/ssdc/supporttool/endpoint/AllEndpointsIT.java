package uk.gov.ons.ssdc.supporttool.endpoint;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
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

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class AllEndpointsIT {
  @Autowired private IntegrationTestHelper integrationTestHelper;

  @LocalServerPort private int port;

  @Test
  public void testActionRuleEndpoints() {
    integrationTestHelper.testGet(
        port,
        UserGroupAuthorisedActivityType.LIST_ACTION_RULES,
        (bundle) -> String.format("actionRules/?collectionExercise=%s", bundle.getCollexId()));

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
  }
}
