export const getAllPrintTemplates = async (authorisedActivities) => {
  // The caller should probably check this, but it's here as a belt-and-braces in case of badly behaved programmers
  if (!authorisedActivities.includes("LIST_PRINT_TEMPLATES")) return [];

  const response = await fetch("/api/printTemplates");
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

export const getFulfilmentPrintTemplates = async (
  authorisedActivities,
  surveyId
) => {
  // The caller should probably check this, but it's here as a belt-and-braces in case of badly behaved programmers
  if (
    !authorisedActivities.includes(
      "LIST_ALLOWED_PRINT_TEMPLATES_ON_FULFILMENTS"
    )
  )
    return [];

  const response = await fetch(
    `/api/fulfilmentSurveyPrintTemplates/?surveyId=${surveyId}`
  );
  const printTemplatesJson = await response.json();

  return printTemplatesJson;
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

export const getActionRulePrintTemplates = async (
  authorisedActivities,
  surveyId
) => {
  // The caller should probably check this, but it's here as a belt-and-braces in case of badly behaved programmers
  if (
    !authorisedActivities.includes(
      "LIST_ALLOWED_PRINT_TEMPLATES_ON_ACTION_RULES"
    )
  )
    return [];

  const response = await fetch(
    `/api/actionRuleSurveyPrintTemplates/?surveyId=${surveyId}`
  );
  const printTemplatesJson = await response.json();

  return printTemplatesJson;
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
