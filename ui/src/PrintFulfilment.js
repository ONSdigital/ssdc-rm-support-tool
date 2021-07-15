import React, {Component} from 'react';
import '@fontsource/roboto';
import {Button, Dialog, DialogContent, FormControl, InputLabel, MenuItem, Select} from '@material-ui/core';
import {getAllPrintTemplates, getFulfilmentPrintTemplates} from "./Utils";

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
  

  // TODO: Need to handle errors from Promises
  refreshDataFromBackend = async () => {

    const fulfilmentPrintTemplates = await getFulfilmentPrintTemplates(this.props.surveyId)
        .then((fulfilmentTemplates) => {
          return fulfilmentTemplates
        })

    this.setState({
      allowableFulfilmentPrintTemplates: fulfilmentPrintTemplates
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