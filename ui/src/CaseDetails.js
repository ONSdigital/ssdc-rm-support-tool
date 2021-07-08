import React, { Component } from 'react';
import '@fontsource/roboto';
import {
  Typography,
  Paper,
  Button,
  Dialog,
  DialogContent,
  FormControl,
  InputLabel,
  Select,
  MenuItem, TextField
} from '@material-ui/core';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableContainer from '@material-ui/core/TableContainer';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import Tablelvl2Context from "@material-ui/core/Table/Tablelvl2Context";
import {uuidv4} from "./common";

class CaseDetails extends Component {
  state = {
    case: null,
    createRefusalDialogDisplayed: false,
    newRefusalType: '',
    newRefusalAgentId: '',
    newRefusalCallId: '',
    refusalTypeValidationError: false,
  }

  componentDidMount() {
    this.getCase()
  }

  getCase = async () => {
    const response = await fetch('/cases/' + this.props.caseId)
    const caseJson = await response.json()

    if (response.ok) {
      this.setState({ case: caseJson })
    }
  }

  openRefusalDialogue = () => {
    this.setState({
      createRefusalDialogDisplayed: true,
      newRefusalType: '',
      newRefusalAgentId: '',
      newRefusalCallId: '',
      refusalTypeValidationError: false,
      refusalAgentIdValidationError: false,
    })
  }

  closeDialog = () => {
    this.setState({ createRefusalDialogDisplayed: false })
  }

  onNewRefusalTypeChange = (event) => {
    this.setState({
      newRefusalType: event.target.value,
      refusalTypeValidationError: false
    })
  }

  onNewRefusalAgentIdChange = (event) => {
    this.setState({
      newRefusalAgentId: event.target.value
    })
  }

  onNewRefusalCallIdChange = (event) => {
    this.setState({
      newRefusalCallId: event.target.value
    })
  }

  onCreateRefusal = async () => {
    var failedValidation = false

    if (!this.state.newRefusalType) {
      this.setState({ refusalTypeValidationError: true })
      failedValidation = true
    }

    if (failedValidation) {
      return
    }

    const newRefusal = {
      type: this.state.newRefusalType,
      agentId: this.state.agentId,
      callId: this.state.callId,
      collectionCase: this.state.case
    }

    const response = await fetch('/actionRules', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(newActionRule)
    })

    if (response.ok) {
      this.setState({ createActionRulesDialogDisplayed: false })
    }
  }


  render() {
    var caseEvents
    var uacQids

    if (this.state.case) {
      caseEvents = this.state.case.events.map((event, index) => (
        <TableRow key={index}>
          <TableCell component="th" scope="row">
            {event.eventDate}
          </TableCell>
          <TableCell component="th" scope="row">
            {event.eventDescription}
          </TableCell>
          <TableCell component="th" scope="row">
            {event.eventSource}
          </TableCell>
        </TableRow>
      ))

      uacQids = this.state.case.uacQidLinks.map((uacQidLink, index) => (
        <TableRow key={index}>
          <TableCell component="th" scope="row">
            {uacQidLink.qid}
          </TableCell>
          <TableCell component="th" scope="row">
            {uacQidLink.createdAt}
          </TableCell>
          <TableCell component="th" scope="row">
            {uacQidLink.lastUpdatedAt}
          </TableCell>
          <TableCell component="th" scope="row">
            {uacQidLink.active ? 'Yes' : 'No'}
          </TableCell>
        </TableRow>
      ))
    }

    return (
      <div style={{ padding: 20 }}>
        <Typography variant="h4" color="inherit" style={{ marginBottom: 20 }}>
          Case Details
        </Typography>
        {this.state.case &&
          <div>
            <TableContainer component={Paper} style={{ marginTop: 20 }}>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>Details</TableCell>
                    <TableCell align="right">Action</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  <TableCell component="th" scope="row">
                    <div>Case ref: {this.state.case.caseRef}</div>
                    <div>Created at: {this.state.case.createdAt}</div>
                    <div>Last updated at: {this.state.case.lastUpdatedAt}</div>
                    <div>Receipted: {this.state.case.receiptReceived ? "Yes" : "No"}</div>
                    <div>Refused: {this.state.case.refusalReceived ? this.state.case.refusalReceived : "No"}</div>
                    <div>Invalid: {this.state.case.addressInvalid ? "Yes" : "No"}</div>
                    <div>Launched EQ: {this.state.case.surveyLaunched ? "Yes" : "No"}</div>
                  </TableCell>
                  <TableCell align="right">
                    <div>
                    <Button
                        onClick={this.openRefusalDialogue}
                        variant="contained">
                      Refuse this case
                    </Button>
                    </div>
                    <div>
                    <Button
                        // onClick={() => this.props.onOpenSurveyDetails(survey)}
                        variant="contained">
                      Invalidate this case
                    </Button>
                    </div>
                    <div>
                      <Button
                          // onClick={() => this.props.onOpenSurveyDetails(survey)}
                          variant="contained">
                        Request paper fulfilment
                      </Button>
                    </div>
                  </TableCell>
                </TableBody>
              </Table>
            </TableContainer>
            <Dialog open={this.state.createRefusalDialogDisplayed}>
              <DialogContent style={{ padding: 30 }}>
                <div>
                  <div>
                    <FormControl
                        required
                        fullWidth={true}>
                      <InputLabel>Refusal Type</InputLabel>
                      <Select
                          onChange={this.onNewRefusalTypeChange}
                          value={this.state.newRefusalType}
                          error={this.state.refusalTypeValidationError}>
                        <MenuItem value={'EXTRAORDINARY_REFUSAL'}>EXTRAORDINARY REFUSAL</MenuItem>
                        <MenuItem value={'HARD_REFUSAL'}>HARD REFUSAL</MenuItem>
                      </Select>
                    </FormControl>
                    <TextField
                        fullWidth={true}
                        style={{ marginTop: 20 }}
                        label="Agent ID"
                        onChange={this.onNewRefusalAgentIdChange}
                        value={this.state.newRefusalAgentId} />
                    <TextField
                        fullWidth={true}
                        style={{ marginTop: 20 }}
                        label="Call ID"
                        onChange={this.onNewRefusalCallIdChange}
                        value={this.state.newRefusalCallId} />
                  </div>
                  <div style={{ marginTop: 10 }}>
                    <Button onClick={this.onCreateRefusal} variant="contained" style={{ margin: 10 }}>
                      Refuse this case
                    </Button>
                    <Button onClick={this.closeDialog} variant="contained" style={{ margin: 10 }}>
                      Cancel
                    </Button>
                  </div>
                </div>
              </DialogContent>
            </Dialog>






            <TableContainer component={Paper} style={{ marginTop: 20 }}>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>Date</TableCell>
                    <TableCell>Description</TableCell>
                    <TableCell>Source</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {caseEvents}
                </TableBody>
              </Table>
            </TableContainer>
            <TableContainer component={Paper} style={{ marginTop: 20 }}>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>QID</TableCell>
                    <TableCell>Created At</TableCell>
                    <TableCell>Last Updated At</TableCell>
                    <TableCell>Active</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {uacQids}
                </TableBody>
              </Table>
            </TableContainer>
          </div>
        }
      </div>
    )
  }
}

export default CaseDetails



// <Dialog open={this.props.showDetails} onClose={this.props.onClickAway}>
//     <DialogContent style={{ padding: 30 }}>
// <div>
// {this.props.case &&
//       <div>
//         <div>Case ref: {this.props.case.caseRef}</div>
//         <div>Created at: {this.props.case.createdAt}</div>
//         <div>Last updated at: {this.props.case.lastUpdatedAt}</div>
//         <div>Receipted: {this.props.case.receiptReceived ? "Yes" : "No"}</div>
//         <div>Refused: {this.props.case.refusalReceived ? this.props.case.refusalReceived : "No"}</div>
//         <div>Invalid: {this.props.case.addressInvalid ? "Yes" : "No"}</div>
//         <div>Launched EQ: {this.props.case.surveyLaunched ? "Yes" : "No"}</div>
//       </div>
// }
// <Button onClick={this.props.onCloseDetails} variant="contained" style={{ margin: 10 }}>
//   Close
// </Button>
// </div>
// </DialogContent>
// </Dialog>