import React, { Component } from 'react';
import '@fontsource/roboto';
import { Button,  Paper, Typography, TextField } from '@material-ui/core';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableContainer from '@material-ui/core/TableContainer';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';


class SurveyCaseSearch extends Component {
  state = {
    matchedCases: [],
    searchTerm: '',
  }

  componentDidMount() {
    // this.getAuthorisedActivities() // Only need to do this once; don't refresh it repeatedly as it changes infrequently
    // this.refreshDataFromBackend()

    // this.interval = setInterval(
    //   () => this.refreshDataFromBackend(),
    //   1000
    // )
  }

  // getCasesFromSearchTerm = async () => {
  //   const response = await fetch('cases/search?surveyId=' + this.props.surveyId + '&searchTerm=NW')

  //      // TODO: We need more elegant error handling throughout the whole application, but this will at least protect temporarily
  //     if (!response.ok) {
  //       return
  //     }

  //     const matchedCasesJson = await response.json()

  //     this.setState( { matchedCases: matchedCasesJson})
  // }

  onSearchChange = (event) => {
    this.setState({
      searchTerm: event.target.value
    })
  }

  onSearch = async () => {

    // if (!this.state.searchTerm.trim()) {
    //   this.setState({ containsValidationError: true })
    //   failedValidation = true
    // }

    // if (!this.state.column.trim()) {
    //   this.setState({ columnValidationError: true })
    //   failedValidation = true
    // }

    // if (failedValidation) {
    //   return
    // }

    const response = await fetch('cases/search?surveyId=' + this.props.surveyId + '&searchTerm=' + this.state.searchTerm)

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
        {/* <TableCell align="right">
          {caze.sample}
        </TableCell> */}
      </TableRow>
    ))

    return (
      <div style={{ padding: 20 }}>
        <Typography variant="h4" color="inherit" style={{ marginBottom: 20 }}>
          Survey: {this.props.surveyName}
        </Typography>

        <TextField
                required
                fullWidth={true}
                style={{ marginTop: 20 }}
                error={this.state.containsValidationError}
                label="SearchTerm"
                onChange={this.onSearchChange}
                value={this.state.searchTerm} />
          <div style={{ marginTop: 10 }}>
            <Button onClick={this.onSearch} variant="contained" style={{ margin: 10 }}>
              Search
            </Button>
          </div>

        <TableContainer component={Paper} style={{ marginTop: 20 }}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Case Ref</TableCell>
                {/* <TableCell align="right">Sample Data</TableCell> */}
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