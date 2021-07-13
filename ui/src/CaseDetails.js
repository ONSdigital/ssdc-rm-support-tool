import React, { Component } from 'react';
import '@fontsource/roboto';
import { Typography, Paper, Button } from '@material-ui/core';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableContainer from '@material-ui/core/TableContainer';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';

class CaseDetails extends Component {
  state = {
    authorisedActivities: [],
    case: null,
    uacQidLinks: []
  }

  componentDidMount() {
    this.getAuthorisedActivities() // Only need to do this once; don't refresh it repeatedly as it changes infrequently
    this.getCase()
    this.getUacQidLinks()
  }

  getAuthorisedActivities = async () => {
    const response = await fetch('/auth?surveyId=' + this.props.surveyId)

    // TODO: We need more elegant error handling throughout the whole application, but this will at least protect temporarily
    if (!response.ok) {
      return
    }

    const authJson = await response.json()

    this.setState({ authorisedActivities: authJson })
  }

  getCase = async () => {
    const response = await fetch('/cases/' + this.props.caseId)
    const caseJson = await response.json()

    if (response.ok) {
      this.setState({ case: caseJson })
    }
  }

  getUacQidLinks = async () => {
    const response = await fetch('/cases/' + this.props.caseId + '/uacQidLinks')
    const uacQidLinksJson = await response.json()

    if (response.ok) {
      this.setState({ uacQidLinks: uacQidLinksJson._embedded.uacQidLinks })
    }
  }

  onDeactivate = async (qid) => {
    const response = await fetch('/deactivateUac/' + qid)

    if (response.ok) {
      // NOTE: UIs don't play nice with async stuff... we have no idea if/when the UAC will actually be dectivated... go figure
      await new Promise(r => setTimeout(r, 1000)) // This sleep ought to work... ish. There's no better way to do this

      // Now, refresh the UAC QID Link data so we can see that the UAC is deactivated
      this.getUacQidLinks()
    }
  }

  render() {
    var caseEvents

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
    }

    const uacQids = this.state.uacQidLinks.map((uacQidLink, index) => (
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
        <TableCell>
          {(this.state.authorisedActivities.includes('DEACTIVATE_UAC') && uacQidLink.active) &&
            <Button
              onClick={() => this.onDeactivate(uacQidLink.qid)}
              variant="contained">
              Deactivate
            </Button>
          }
        </TableCell>
      </TableRow>
    ))

    return (
      <div style={{ padding: 20 }}>
        <Typography variant="h4" color="inherit" style={{ marginBottom: 20 }}>
          Case Details
        </Typography>
        {this.state.case &&
          <div>
            <div>Case ref: {this.state.case.caseRef}</div>
            <div>Created at: {this.state.case.createdAt}</div>
            <div>Last updated at: {this.state.case.lastUpdatedAt}</div>
            <div>Receipted: {this.state.case.receiptReceived ? "Yes" : "No"}</div>
            <div>Refused: {this.state.case.refusalReceived ? this.state.case.refusalReceived : "No"}</div>
            <div>Invalid: {this.state.case.addressInvalid ? "Yes" : "No"}</div>
            <div>Launched EQ: {this.state.case.surveyLaunched ? "Yes" : "No"}</div>
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
                    <TableCell>Action</TableCell>
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