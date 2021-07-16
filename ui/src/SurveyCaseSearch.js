import React, { Component } from 'react';
import '@fontsource/roboto';
import { Link, Paper, Typography } from '@material-ui/core';
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
  }

  componentDidMount() {
    this.getSampleColumns()
    this.setState({ caseSearchResults: this.props.caseSearchResults })
  }

  onSearchExecuteAndPopulateList = async (searchUrl, searchTerm, searchDesc) => {
    const response = await fetch(searchUrl)

    // TODO: We need more elegant error handling throughout the whole application, but this will at least protect temporarily
    if (!response.ok) {
      alert('Error: ' + response.state)
      return
    }

    const matchedCasesJson = await response.json()

    this.props.onCaseSearchResults(matchedCasesJson, searchTerm, searchDesc)
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
    const nonSensitiveColumns = surveyJson.sampleValidationRules.filter(rule => !rule.sensitive).map(rule => rule.columnName)

    this.setState({ sampleColumns: nonSensitiveColumns })
  }

  getCaseCells = (caze) => {
    const caseId = caze.id
    let caseCells = []
    caseCells.push((
      <TableCell key={0}>
        <Link
          onClick={() => this.props.onOpenCaseDetails(caseId)}>
          {caze.caseRef}
        </Link>
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

    const caseTableRows = this.props.caseSearchResults.map((caze, index) => (
      <TableRow key={index}>
        {this.getCaseCells(caze)}
      </TableRow>
    ))

    const borderStyles = {
      border: '1px solid black',
      marginTop: '10px'
    }

    return (
      <div style={{ padding: 20 }}>
        <Typography variant="h4" color="inherit" style={{ marginBottom: 10 }}>
          Survey: {this.props.surveyName}
        </Typography>

        <div style={borderStyles}>
          <SurveySampleSearch style={borderStyles} surveyId={this.props.surveyId}
            onSearchExecuteAndPopulateList={this.onSearchExecuteAndPopulateList}
            searchTermValidator={this.checkWhitespace}
            collectionExercises={this.props.collectionExercises}>
          </SurveySampleSearch>
        </div>

        <div style={borderStyles}>
          <SurveySimpleSearchInput style={borderStyles} surveyId={this.props.surveyId}
            onSearchExecuteAndPopulateList={this.onSearchExecuteAndPopulateList}
            searchTermValidator={this.isNumeric}
            urlpathName='caseRef'
            displayText='Search By Case Ref'
            searchDesc='Case Ref matching'
          />
        </div>

        <div style={borderStyles}>
          <SurveySimpleSearchInput style={borderStyles} surveyId={this.props.surveyId}
            onSearchExecuteAndPopulateList={this.onSearchExecuteAndPopulateList}
            searchTermValidator={this.isNumeric}
            urlpathName='qid'
            displayText='Search By Qid'
            searchDesc='cases linked to QID'
          />
        </div>

        {(this.props.caseSearchTerm) ?
          <Typography variant="h5" color="inherit" style={{ marginTop: 30, marginBottom: 10 }}>
            Results for {this.props.caseSearchDesc} "{this.props.caseSearchTerm}":
          </Typography> :
          <Typography variant="h5" color="inherit" style={{ marginTop: 30, marginBottom: 10 }}>
            Make a search
          </Typography>
        }
        {(this.props.caseSearchTerm && this.props.caseSearchResults.length > 0) &&
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
        }
        {(this.props.caseSearchTerm && !this.props.caseSearchResults.length > 0) &&
          <p>No cases found</p>
        }
      </div>

    )
  }

}

export default SurveyCaseSearch
