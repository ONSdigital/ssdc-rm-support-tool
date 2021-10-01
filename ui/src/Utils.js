export const getAllPrintTemplates = async (authorisedActivities) => {
  const response = await fetch("/api/printTemplates");
  const templateJson = await response.json();

  const templatePackCodes = templateJson.map((template) => template.packCode);

  return templatePackCodes;
};

export const getAllSmsTemplates = async (authorisedActivities) => {
  const response = await fetch("/api/smsTemplates");
  const templateJson = await response.json();

  const templatePackCodes = templateJson.map((template) => template.packCode);

  return templatePackCodes;
};

export const getFulfilmentPrintTemplates = async (
  authorisedActivities,
  surveyId
) => {
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
