import React, {Component} from 'react';
import '@fontsource/roboto';
import {Button, Dialog, DialogContent, TextField} from '@material-ui/core';

class InvalidAddress extends Component {
  state = {
    reason: '',
    notes: '',
    reasonValidationError: false,
    showDialog: false,
  }

  openDialog = () => {
    this.setState({
      showDialog: true,
    })
  }

  closeDialog = () => {
    this.setState({
      reason: '',
      notes: '',
      reasonValidationError: false,
      showDialog: false,
    })
  }

  onReasonChange = (event) => {
    this.setState({
      reason: event.target.value,
      reasonValidationError: false
    })
  }

  onNotesChange = (event) => {
    this.setState({
      notes: event.target.value
    })
  }

  onCreate = async () => {
    if (!this.state.reason) {
      this.setState({reasonValidationError: true})

      return
    }

    const invalidAddress = {
      "reason": this.state.reason,
      "notes": this.state.notes,
    }

    const response = await fetch(`/cases/${this.props.caseId}/action/invalid-address`, {
      method: 'POST',
      headers: {'Content-Type': 'application/json'},
      body: JSON.stringify(invalidAddress)
    })

    if (response.ok) {
      this.closeDialog()
    }
  }

  render() {
    return (
        <div>
          <Button
              style={{marginTop: 10, marginBottom: 10}}
              onClick={this.openDialog}
              variant="contained">
            Invalidate this case
          </Button>
          <Dialog open={this.state.showDialog}>
            <DialogContent style={{padding: 30}}>
              <div>
                <TextField
                    required
                    inputProps={{style: {textTransform: 'uppercase'}}}
                    fullWidth={true}
                    style={{marginTop: 20}}
                    label="Reason"
                    onChange={this.onReasonChange}
                    error={this.state.reasonValidationError}
                    value={this.state.reason}/>
                <TextField
                    fullWidth={true}
                    style={{marginTop: 20}}
                    label="Notes"
                    onChange={this.onNotesChange}
                    value={this.state.notes}/>
              </div>
              <div style={{marginTop: 10}}>
                <Button onClick={this.onCreate} variant="contained" style={{margin: 10}}>
                  Invalidate this case
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

export default InvalidAddress