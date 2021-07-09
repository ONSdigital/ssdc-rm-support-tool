import React, { Component } from 'react';
import '@fontsource/roboto';
import { Button,  Paper, Typography } from '@material-ui/core';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableContainer from '@material-ui/core/TableContainer';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';


class SurveyCaseSearch extends Component {
  state = {
    matchedCases: [],
  }

  componentDidMount() {
    // this.getAuthorisedActivities() // Only need to do this once; don't refresh it repeatedly as it changes infrequently
    // this.refreshDataFromBackend()

    // this.interval = setInterval(
    //   () => this.refreshDataFromBackend(),
    //   1000
    // )
    this.getCasesFromSearchTerm()
  }

  getCasesFromSearchTerm = async () => {
    const response = await fetch('cases/search?surveyId=' + this.props.surveyId + '&searchTerm=NW')

       // TODO: We need more elegant error handling throughout the whole application, but this will at least protect temporarily
      if (!response.ok) {
        return
      }

      const matchedCasesJson = await response.json()

      this.setState( { matchedCases: matchedCasesJson})

  }


  render() {
    const matchedCasesTableRows = this.state.matchedCases.map(caze => (
      <TableRow key={caze.caseRef}>
        <TableCell component="th" scope="row">
          {caze.caseRef}
        </TableCell>
        <TableCell align="right">
          {caze.sample}
        </TableCell>
      </TableRow>
    ))

    return (
      <div style={{ padding: 20 }}>
        <Typography variant="h4" color="inherit" style={{ marginBottom: 20 }}>
          Survey: {this.props.surveyName}
        </Typography>
        <Button variant="contained" onClick={this.openDialog}>Create Collection Exercise</Button>
        <TableContainer component={Paper} style={{ marginTop: 20 }}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Case Ref</TableCell>
                <TableCell align="right">Sample Data</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {matchedCasesTableRows}
            </TableBody>
          </Table>
        </TableContainer>
      </div>
    )
  }
}

export default SurveyCaseSearch