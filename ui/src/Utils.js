export const getAllPrintTemplates = async () => {
  const response = await fetch('/printTemplates')
  const templateJson = await response.json()

  let templates = []

  for (let i = 0; i < templateJson._embedded.printTemplates.length; i++) {
    const packCode = templateJson._embedded.printTemplates[i]._links.self.href.split('/')[4]
    templates.push(packCode)
  }

  return templates;
}