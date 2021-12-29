export const getAllExportFileTemplates = async (authorisedActivities) => {
  // The caller should probably check this, but it's here as a belt-and-braces in case of badly behaved programmers
  if (!authorisedActivities.includes("LIST_EXPORT_FILE_TEMPLATES")) return [];

  const response = await fetch("/api/exportFileTemplates");
  const templateJson = await response.json();

  const templatePackCodes = templateJson.map((template) => template.packCode);

  return templatePackCodes;
};

export const getAllSmsTemplates = async (authorisedActivities) => {
  // The caller should probably check this, but it's here as a belt-and-braces in case of badly behaved programmers
  if (!authorisedActivities.includes("LIST_SMS_TEMPLATES")) return [];

  const response = await fetch("/api/smsTemplates");
  const templateJson = await response.json();

  const templatePackCodes = templateJson.map((template) => template.packCode);

  return templatePackCodes;
};

export const getAllEmailTemplates = async (authorisedActivities) => {
  // The caller should probably check this, but it's here as a belt-and-braces in case of badly behaved programmers
  if (!authorisedActivities.includes("LIST_EMAIL_TEMPLATES")) return [];

  const response = await fetch("/api/emailTemplates");
  const templateJson = await response.json();

  const templatePackCodes = templateJson.map((template) => template.packCode);

  return templatePackCodes;
};

export const getFulfilmentExportFileTemplates = async (
  authorisedActivities,
  surveyId
) => {
  // The caller should probably check this, but it's here as a belt-and-braces in case of badly behaved programmers
  if (
    !authorisedActivities.includes(
      "LIST_ALLOWED_EXPORT_FILE_TEMPLATES_ON_FULFILMENTS"
    )
  )
    return [];

  const response = await fetch(
    `/api/fulfilmentSurveyExportFileTemplates/?surveyId=${surveyId}`
  );
  const exportFileTemplatesJson = await response.json();

  return exportFileTemplatesJson;
};

export const getSmsFulfilmentTemplates = async (
  authorisedActivities,
  surveyId
) => {
  // The caller should probably check this, but it's here as a belt-and-braces in case of badly behaved programmers
  if (
    !authorisedActivities.includes("LIST_ALLOWED_SMS_TEMPLATES_ON_FULFILMENTS")
  )
    return [];

  const response = await fetch(
    `/api/fulfilmentSurveySmsTemplates/?surveyId=${surveyId}`
  );
  const smsFulfilmentTemplatesJson = await response.json();

  return smsFulfilmentTemplatesJson;
};

export const getEmailFulfilmentTemplates = async (
  authorisedActivities,
  surveyId
) => {
  // The caller should probably check this, but it's here as a belt-and-braces in case of badly behaved programmers
  if (
    !authorisedActivities.includes(
      "LIST_ALLOWED_EMAIL_TEMPLATES_ON_FULFILMENTS"
    )
  )
    return [];

  const response = await fetch(
    `/api/fulfilmentSurveyEmailTemplates/?surveyId=${surveyId}`
  );
  const emailFulfilmentTemplatesJson = await response.json();

  return emailFulfilmentTemplatesJson;
};

export const getActionRuleExportFileTemplates = async (
  authorisedActivities,
  surveyId
) => {
  // The caller should probably check this, but it's here as a belt-and-braces in case of badly behaved programmers
  if (
    !authorisedActivities.includes(
      "LIST_ALLOWED_EXPORT_FILE_TEMPLATES_ON_ACTION_RULES"
    )
  )
    return [];

  const response = await fetch(
    `/api/actionRuleSurveyExportFileTemplates/?surveyId=${surveyId}`
  );
  const exportFileTemplatesJson = await response.json();

  return exportFileTemplatesJson;
};

export const getActionRuleSmsTemplates = async (
  authorisedActivities,
  surveyId
) => {
  // The caller should probably check this, but it's here as a belt-and-braces in case of badly behaved programmers
  if (
    !authorisedActivities.includes("LIST_ALLOWED_SMS_TEMPLATES_ON_ACTION_RULES")
  )
    return [];

  const response = await fetch(
    `/api/actionRuleSurveySmsTemplates/?surveyId=${surveyId}`
  );
  const smsTemplatesJson = await response.json();

  return smsTemplatesJson;
};

export const getActionRuleEmailTemplates = async (
  authorisedActivities,
  surveyId
) => {
  // The caller should probably check this, but it's here as a belt-and-braces in case of badly behaved programmers
  if (
    !authorisedActivities.includes(
      "LIST_ALLOWED_EMAIL_TEMPLATES_ON_ACTION_RULES"
    )
  )
    return [];

  const response = await fetch(
    `/api/actionRuleSurveyEmailTemplates/?surveyId=${surveyId}`
  );
  const emailTemplatesJson = await response.json();

  return emailTemplatesJson;
};

export const getSensitiveSampleColumns = async (
  authorisedActivities,
  surveyId
) => {
  // The caller should probably check this, but it's here as a belt-and-braces in case of badly behaved programmers
  if (!authorisedActivities.includes("VIEW_SURVEY")) return [];

  const response = await fetch(`/api/surveys/${surveyId}`);
  if (!response.ok) {
    return;
  }

  const surveyJson = await response.json();
  const sensitiveColumns = surveyJson.sampleValidationRules
    .filter((rule) => rule.sensitive)
    .map((rule) => rule.columnName);

  return sensitiveColumns;
};

export const getSampleColumns = async (authorisedActivities, surveyId) => {
  if (!authorisedActivities.includes("VIEW_SURVEY")) return [];

  const response = await fetch(`/api/surveys/${surveyId}`);
  if (!response.ok) {
    return;
  }

  const surveyJson = await response.json();
  const sampleColumns = surveyJson.sampleValidationRules
    .filter((rule) => !rule.sensitive)
    .map((rule) => rule.columnName);

  return sampleColumns;
};

// This is not efficent, but it's a prototype app.  Could probably cache them too.
export const getAuthorisedActivities = async () => {
  const authResponse = await fetch("/api/auth");

  // TODO: We need more elegant error handling throughout the whole application, but this will at least protect temporarily
  if (!authResponse.ok) {
    return;
  }

  const authorisedActivities = await authResponse.json();

  return authorisedActivities;
};


// Could we have the interval thing running here? And register call back functions
// for any interested components,  probably not.

