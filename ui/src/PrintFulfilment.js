import React, {Component} from 'react';
import '@fontsource/roboto';
import {Button, Dialog, DialogContent, FormControl, InputLabel, MenuItem, Select} from '@material-ui/core';

class PrintFulfilment extends Component {
  state = {
    packCode: '',
    allowableFulfilmentPrintTemplates: [],
    packCodeValidationError: false,
    showDialog: false,
  }

  openDialog = () => {
    this.refreshDataFromBackend()
    this.setState({
      showDialog: true,
    })
  }

  closeDialog = () => {
    this.setState({
      packCode: '',
      allowableFulfilmentPrintTemplates: [],
      packCodeValidationError: false,
      showDialog: false,
    })
  }

  onPrintTemplateChange = (event) => {
    this.setState({packCode: event.target.value})
  }

  onCreate = async () => {
    if (!this.state.packCode) {
      this.setState({
        packCodeValidationError: true
      })

      return
    }

    const printFulfilment = {
      "packCode": this.state.packCode
    }

    const response = await fetch(`/cases/${this.props.caseId}/action/fulfilment`, {
      method: 'POST',
      headers: {'Content-Type': 'application/json'},
      body: JSON.stringify(printFulfilment)
    })

    if (response.ok) {
      this.closeDialog()
    }
  }

  // TODO: These next 2 functions are duplicated in SurveyDetails.js. Move to helper?
  getFulfilmentPrintTemplates = async () => {
    const response = await fetch('/surveys/' + this.props.surveyId + '/fulfilmentPrintTemplates')
    const printTemplatesJson = await response.json()
    const printTemplates = printTemplatesJson._embedded.fulfilmentSurveyPrintTemplates

    let templates = []

    for (let i = 0; i < printTemplates.length; i++) {
      const printTemplateUrl = new URL(printTemplates[i]._links.printTemplate.href)

      const printTemplateResponse = await fetch(printTemplateUrl.pathname)
      const printTemplateJson = await printTemplateResponse.json()
      const packCode = printTemplateJson._links.self.href.split('/')[4]

      templates.push(packCode)
    }

    return templates
  }

  getAllPrintTemplates = async () => {
    const response = await fetch('/printTemplates')
    const templateJson = await response.json()

    let templates = []

    for (let i = 0; i < templateJson._embedded.printTemplates.length; i++) {
      const packCode = templateJson._embedded.printTemplates[i]._links.self.href.split('/')[4]
      templates.push(packCode)
    }

    return templates;
  }

  refreshDataFromBackend = async () => {
    const allPrintTemplates = await this.getAllPrintTemplates()
    const fulfilmentPrintTemplates = await this.getFulfilmentPrintTemplates()
    let allowableFulfilmentPrintTemplates = []

    allPrintTemplates.forEach(packCode => {
      if (fulfilmentPrintTemplates.includes(packCode)) {
        allowableFulfilmentPrintTemplates.push(packCode)
      }
    })

    this.setState({
      allowableFulfilmentPrintTemplates: allowableFulfilmentPrintTemplates
    })
  }

  render() {
    const fulfilmentPrintTemplateMenuItems = this.state.allowableFulfilmentPrintTemplates.map(packCode =>
        <MenuItem key={packCode} value={packCode}>{packCode}</MenuItem>
    )

    return (
        <div>
            <Button
                onClick={this.openDialog}
                variant="contained">
              Request paper fulfilment
            </Button>
        <Dialog open={this.state.showDialog}>
          <DialogContent style={{padding: 30}}>
            <div>
              <FormControl
                  required
                  fullWidth={true}>
                <InputLabel>Print Template</InputLabel>
                <Select
                    onChange={this.onPrintTemplateChange}
                    value={this.state.packCode}
                    error={this.state.packCodeValidationError}>
                  {fulfilmentPrintTemplateMenuItems}
                </Select>
              </FormControl>
            </div>
            <div style={{marginTop: 10}}>
              <Button onClick={this.onCreate} variant="contained" style={{margin: 10}}>
                Request paper fulfilment
              </Button>
              <Button onClick={this.closeDialog} variant="contained" style={{margin: 10}}>
                Cancel
              </Button>
            </div>
          </DialogContent>
        </Dialog>
        </div>
    )
  }
}

export default PrintFulfilment