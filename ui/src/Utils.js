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
  for (const smsTemplate of templateJson._embedded.smsTemplates) {
    const packCode = smsTemplate._links.self.href.split("/").pop();
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

export const getSmsFulfilmentTemplates = async (surveyId) => {
  const response = await fetch(`/api/surveys/${surveyId}/smsTemplates`);
  const smsFulfilmentTemplatesJson = await response.json();
  const smsFulfilmentTemplates =
    smsFulfilmentTemplatesJson._embedded.fulfilmentSurveySmsTemplates;

  let templates = [];

  for (const smsTemplate of smsFulfilmentTemplates) {
    const smsFulfilmentTemplateUrl = new URL(
      smsTemplate._links.smsTemplate.href
    );

    const smsFulfilmentTemplateResponse = await fetch(
      smsFulfilmentTemplateUrl.pathname
    );
    const smsFulfilmentTemplateJson =
      await smsFulfilmentTemplateResponse.json();
    const packCode = smsFulfilmentTemplateJson._links.self.href
      .split("/")
      .pop();

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

export const getActionRuleSmsTemplates = async (surveyId) => {
  const response = await fetch(
    `/api/surveys/${surveyId}/actionRuleSmsTemplates`
  );
  const smsTemplatesJson = await response.json();
  const smsTemplates = smsTemplatesJson._embedded.actionRuleSurveySmsTemplates;

  let templates = [];

  for (let i = 0; i < smsTemplates.length; i++) {
    const print_template_url = new URL(smsTemplates[i]._links.smsTemplate.href);

    const smsTemplateResponse = await fetch(print_template_url.pathname);
    const smsTemplateJson = await smsTemplateResponse.json();
    const packCode = smsTemplateJson._links.self.href.split("/").pop();

    templates.push(packCode);
  }

  return templates;
};
