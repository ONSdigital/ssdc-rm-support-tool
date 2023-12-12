package uk.gov.ons.ssdc.supporttool.testhelper;

import java.util.UUID;
import lombok.Data;

@Data
public class BundleOfUsefulTestStuff {
  private UUID surveyId;
  private UUID collexId;
  private UUID caseId;
  private UUID actionRuleId;
  private String qid;
  private String exportFileTemplatePackCode;
  private String smsTemplatePackCode;
  private String emailTemplatePackCode;
  private UUID userId;
  private UUID groupId;
  private UUID groupMemberId;
  private UUID groupAdminId;
  private UUID groupPermissionId;
  private UUID secondGroupId;
}
