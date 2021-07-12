import React, {Component} from 'react';
import '@fontsource/roboto';
import {Button, Dialog, DialogContent, FormControl, InputLabel, MenuItem, Select, TextField} from '@material-ui/core';

class Refusal extends Component {
  state = {
    type: '',
    agentId: '',
    callId: '',
    typeValidationError: false,
    showDialog: false,
  }

  openDialog = () => {
    this.setState({
      showDialog: true,
    })
  }

  closeDialog = () => {
    this.setState({
      type: '',
      agentId: '',
      callId: '',
      typeValidationError: false,
      showDialog: false,
    })
  }

  onTypeChange = (event) => {
    this.setState({
      type: event.target.value,
      typeValidationError: false
    })
  }

  onAgentIdChange = (event) => {
    this.setState({
      agentId: event.target.value
    })
  }

  onCallIdChange = (event) => {
    this.setState({
      callId: event.target.value
    })
  }

  onCreate = async () => {
    if (!this.state.type) {
      this.setState({typeValidationError: true})

      return
    }

    const newRefusal = {
      "type": this.state.type,
      "agentId": this.state.agentId,
      "callId": this.state.callId,
    }

    const response = await fetch('/refusal/' + this.props.caseId, {
      method: 'PUT',
      headers: {'Content-Type': 'application/json'},
      body: JSON.stringify(newRefusal)
    })

    if (response.ok) {
      this.closeDialog()
    }
  }

  render() {
    return (
        <div>
          <Button
              onClick={this.openDialog}
              variant="contained">
            Refuse this case
          </Button>
          <Dialog open={this.state.showDialog}>
            <DialogContent style={{padding: 30}}>
              <div>
                <FormControl
                    required
                    fullWidth={true}>
                  <InputLabel>Refusal Type</InputLabel>
                  <Select
                      onChange={this.onTypeChange}
                      value={this.state.type}
                      error={this.state.typeValidationError}>
                    <MenuItem value={'EXTRAORDINARY_REFUSAL'}>EXTRAORDINARY REFUSAL</MenuItem>
                    <MenuItem value={'HARD_REFUSAL'}>HARD REFUSAL</MenuItem>
                  </Select>
                </FormControl>
                <TextField
                    fullWidth={true}
                    style={{marginTop: 20}}
                    label="Agent ID"
                    onChange={this.onAgentIdChange}
                    value={this.state.agentId}/>
                <TextField
                    fullWidth={true}
                    style={{marginTop: 20}}
                    label="Call ID"
                    onChange={this.onCallIdChange}
                    value={this.state.callId}/>
              </div>
              <div style={{marginTop: 10}}>
                <Button onClick={this.onCreate} variant="contained" style={{margin: 10}}>
                  Refuse this case
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

export default Refusal