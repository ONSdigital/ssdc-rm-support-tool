export const getAllPrintTemplates = async () => {
  const response = await fetch("/api/printTemplates");
  const templateJson = await response.json();

  let templates = [];

  for (let i = 0; i < templateJson._embedded.printTemplates.length; i++) {
    const packCode = templateJson._embedded.printTemplates[i]._links.self.href
      .split("/")
      .pop();
    templates.push(packCode);
  }

  return templates;
};
export const getAllSmsTemplates = async () => {
  const response = await fetch("/api/smsTemplates");
  const templateJson = await response.json();

  let templates = [];
  for (let i = 0; i < templateJson._embedded.smsTemplates.length; i++) {
    const packCode = templateJson._embedded.smsTemplates[i]._links.self.href
      .split("/")
      .pop();
    templates.push(packCode);
  }

  return templates;
};

export const getFulfilmentPrintTemplates = async (surveyId) => {
  const response = await fetch(
    `/api/surveys/${surveyId}/fulfilmentPrintTemplates`
  );
  const printTemplatesJson = await response.json();
  const printTemplates =
    printTemplatesJson._embedded.fulfilmentSurveyPrintTemplates;

  let templates = [];

  for (let i = 0; i < printTemplates.length; i++) {
    const printTemplateUrl = new URL(
      printTemplates[i]._links.printTemplate.href
    );

    const printTemplateResponse = await fetch(printTemplateUrl.pathname);
    const printTemplateJson = await printTemplateResponse.json();
    const packCode = printTemplateJson._links.self.href.split("/").pop();

    templates.push(packCode);
  }

  return templates;
};

export const getSmsPrintTemplates = async (surveyId) => {
  const response = await fetch(`/api/surveys/${surveyId}/smsTemplates`);
  const printTemplatesJson = await response.json();
  const printTemplates =
    printTemplatesJson._embedded.fulfilmentSurveySmsTemplates;

  let templates = [];

  for (let i = 0; i < printTemplates.length; i++) {
    const printTemplateUrl = new URL(printTemplates[i]._links.smsTemplate.href);

    const printTemplateResponse = await fetch(printTemplateUrl.pathname);
    const printTemplateJson = await printTemplateResponse.json();
    const packCode = printTemplateJson._links.self.href.split("/").pop();

    templates.push(packCode);
  }

  return templates;
};

export const getActionRulePrintTemplates = async (surveyId) => {
  const response = await fetch(
    `/api/surveys/${surveyId}/actionRulePrintTemplates`
  );
  const printTemplatesJson = await response.json();
  const printTemplates =
    printTemplatesJson._embedded.actionRuleSurveyPrintTemplates;

  let templates = [];

  for (let i = 0; i < printTemplates.length; i++) {
    const print_template_url = new URL(
      printTemplates[i]._links.printTemplate.href
    );

    const printTemplateResponse = await fetch(print_template_url.pathname);
    const printTemplateJson = await printTemplateResponse.json();
    const packCode = printTemplateJson._links.self.href.split("/").pop();

    templates.push(packCode);
  }

  return templates;
};
