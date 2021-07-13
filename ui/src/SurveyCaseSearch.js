import React, { Component } from 'react';
import '@fontsource/roboto';
import { Button, Paper, TextField, Typography } from '@material-ui/core';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableContainer from '@material-ui/core/TableContainer';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';


class SurveyCaseSearch extends Component {
  state = {
    sampleColumns: [],
    caseSearchResults: [],
    searchTerm: '',
    caseRef: '',
  }

  componentDidMount() {
    this.getSampleColumns()
  }

  onSearchChange = (event) => {
    this.setState({
      searchTerm: event.target.value
    })
  }

  onCaseRefChange = (event) => {
    this.setState({
      caseRef: event.target.value
    })
  }



  onSearch = async () => {
    let failedValidation
    if (!this.state.searchTerm.trim()) {
      this.setState({ containsValidationError: true })
      failedValidation = true
    }

    if (failedValidation) {
      return
    }

    const response = await fetch('searchInSurvey/' + this.props.surveyId + '?searchTerm=' + this.state.searchTerm)

    // TODO: We need more elegant error handling throughout the whole application, but this will at least protect temporarily
    if (!response.ok) {
      return
    }

    const matchedCasesJson = await response.json()

    this.setState({ caseSearchResults: matchedCasesJson })
  }

  onCaseRefSearch = async () => {
    let failedValidation
    if (!this.state.searchTerm.trim()) {
      this.setState({ containsValidationError: true })
      failedValidation = true
    }

    if (failedValidation) {
      return
    }

    const response = await fetch('searchInSurvey/' + this.props.surveyId + '/caseRef/' + this.state.caseRef)

    // TODO: We need more elegant error handling throughout the whole application, but this will at least protect temporarily
    if (!response.ok) {
      return
    }

    const matchedCasesJson = await response.json()

    this.setState({ caseSearchResults: matchedCasesJson })
  }

  getSampleColumns = async () => {
    const response = await fetch('/surveys/' + this.props.surveyId)
    if (!response.ok) {
      return
    }

    const surveyJson = await response.json()
    let columns = []

    surveyJson.sampleValidationRules.forEach(rule => {
      columns.push(rule.columnName)
    })

    this.setState({ sampleColumns: columns })
  }

  getCaseCells = (caze) => {
    const caseId = caze.id
    let caseCells = []
    caseCells.push((
      <TableCell>
        <Button
          onClick={() => this.props.onOpenCaseDetails(caseId)}
          variant="contained">
          {caze.caseRef}
        </Button>
      </TableCell>
    ))
    caseCells.push(<TableCell>{caze.collectionExerciseName}</TableCell>)
    caseCells.push(this.state.sampleColumns.map(sampleColumn => (
      <TableCell>{caze.sample[sampleColumn]}</TableCell>
    )))

    return caseCells
  }

  getTableHeaderRows() {
    let tableHeaderRows = []
    tableHeaderRows.push((
      <TableCell key={0}>Case Ref</TableCell>
    ))

    tableHeaderRows.push((
      <TableCell key={1}>Collection Exercise</TableCell>
    ))

    tableHeaderRows.push(this.state.sampleColumns.map((sampleColumn, index) => (
      <TableCell key={index + 2}>{sampleColumn}</TableCell>
    )))

    return tableHeaderRows;
  }

  render() {
    const tableHeaderRows = this.getTableHeaderRows();

    const caseTableRows = this.state.caseSearchResults.map((caze, index) => (
      <TableRow key={index}>
        {this.getCaseCells(caze)}
      </TableRow>
    ))

    return (
      <div style={{ padding: 20 }}>
        <Typography variant="h4" color="inherit" style={{ marginBottom: 20 }}>
          Survey: {this.props.surveyName}
        </Typography>

        <TextField
          required
          style={{ marginTop: 20 }}
          error={this.state.containsValidationError}
          label="SearchTerm"
          onChange={this.onSearchChange}
          value={this.state.searchTerm} />
        <div style={{ marginTop: 10 }}>
          <Button onClick={this.onSearch} variant="contained" style={{ margin: 10 }}>
            Search Sample Data
          </Button>
        </div>

        <TextField
          required
          style={{ marginTop: 20 }}
          error={this.state.containsValidationError}
          label="caseRef search"
          onChange={this.onCaseRefChange}
          value={this.state.caseRef} />
        <div style={{ marginTop: 10 }}>
          <Button onClick={this.onCaseRefSearch} variant="contained" style={{ margin: 10 }}>
            Search By Case Ref
          </Button>
        </div>


        <TableContainer component={Paper} style={{ marginTop: 20 }}>
          <Table>
            <TableHead>
              <TableRow>
                {tableHeaderRows}
              </TableRow>
            </TableHead>
            <TableBody>
              {caseTableRows}
            </TableBody>
          </Table>
        </TableContainer>
      </div>
    )
  }

}

export default SurveyCaseSearch
