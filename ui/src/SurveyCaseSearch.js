import React, { Component } from 'react';
import '@fontsource/roboto';
import { Button, Paper, Typography } from '@material-ui/core';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableContainer from '@material-ui/core/TableContainer';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import SurveySimpleSearchInput from './SurveySimpleSearchInput'
import SurveySampleSearch from './SurveySampleSearch'


class SurveyCaseSearch extends Component {
  state = {
    sampleColumns: [],
    caseSearchResults: [],
    noCasesFoundMsg: '',
  }

  componentDidMount() {
    this.getSampleColumns()
  }

  onSearchExecuteAndPopulateList = async (searchUrl) => {
    const response = await fetch(searchUrl)

    // TODO: We need more elegant error handling throughout the whole application, but this will at least protect temporarily
    if (!response.ok) {
      alert('Error: ' + response.state)
      return
    }

    const matchedCasesJson = await response.json()

    this.setState({ caseSearchResults: matchedCasesJson })

    if (this.state.caseSearchResults.length === 0) {
      this.setState({ noCasesFoundMsg: 'No cases found matching search' })
    }
  }

  checkWhitespace = (valueToValidate) => {
    return valueToValidate.trim();
  }

  isNumeric = (str) => {
    return /^\+?\d+$/.test(str)
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
      <TableCell key={0}>
        <Button
          onClick={() => this.props.onOpenCaseDetails(caseId)}
          variant="contained">
          {caze.caseRef}
        </Button>
      </TableCell>
    ))
    caseCells.push(<TableCell key={1}>{caze.collectionExerciseName}</TableCell>)
    caseCells.push(this.state.sampleColumns.map((sampleColumn, index) => (
      <TableCell key={index + 2}>{caze.sample[sampleColumn]}</TableCell>
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
        <Typography variant="h4" color="inherit" style={{ marginBottom: 10 }}>
          Survey: {this.props.surveyName}
        </Typography>

        <SurveySampleSearch surveyId={this.props.surveyId}
          onSearchExecuteAndPopulateList={this.onSearchExecuteAndPopulateList}
          searchTermValidator={this.checkWhitespace}/>

        <SurveySimpleSearchInput surveyId={this.props.surveyId}
          onSearchExecuteAndPopulateList={this.onSearchExecuteAndPopulateList}
          searchTermValidator={this.isNumeric}
          urlpathName='caseRef'
          displayText='Search By Case Ref'/>

        <SurveySimpleSearchInput surveyId={this.props.surveyId}
          onSearchExecuteAndPopulateList={this.onSearchExecuteAndPopulateList}
          searchTermValidator={this.isNumeric}
          urlpathName='qid'
          displayText='Search By Qid'/>

        {
          (this.state.caseSearchResults.length > 0) &&
          < TableContainer component={Paper} style={{ marginTop: 20 }}>
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
        }
        {
          (this.state.caseSearchResults.length === 0) &&
          <p>{this.state.noCasesFoundMsg}</p>
        }
      </div>

    )
  }

}

export default SurveyCaseSearch
