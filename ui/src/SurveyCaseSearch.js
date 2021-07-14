import React, { Component } from 'react';
import '@fontsource/roboto';
import { Button, FormControl, InputLabel, MenuItem, Paper, Select, TextField, Typography } from '@material-ui/core';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableContainer from '@material-ui/core/TableContainer';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import SurveySimpleSearchInput from './SurveySimpleSearchInput'


const NOT_REFUSED = "NOT REFUSED";
const SEARCH_FIELD_WIDTH = 200;
const SEARCH_BUTTON_WIDTH = 200;
const BOOL_FILTER_STYLE = { minWidth: 100, marginLeft: 20, marginRight: 20 };

class SurveyCaseSearch extends Component {
  state = {
    sampleColumns: [],
    caseSearchResults: [],
    searchTerm: '',
    caseRef: '',
    qid: '',
    noCasesFoundMsg: '',
    collectionExercises: [],
    selectedCollectionExercise: '',
    refusalTypes: [],
    selectedRefusalFilter: '',
    selectedReceiptedFilter: '',
    selectedInvalidFilter: '',
    selectedLaunchedFilter: '',
  }

  componentDidMount() {
    this.getSampleColumns()
    this.getCollectionExercises()
    this.getRefusalTypes()
  }

  getCollectionExercises = async () => {
    const response = await fetch('/surveys/' + this.props.surveyId + '/collectionExercises')
    const collexJson = await response.json()

    this.setState({ collectionExercises: collexJson._embedded.collectionExercises })
  }

  getRefusalTypes = async () => {
    const response = await fetch('/searchInSurvey/refusalTypes')
    const refusalJson = await response.json()

    refusalJson.push(NOT_REFUSED)

    this.setState({ refusalTypes: refusalJson })
  }

  onSearchChange = (event) => {
    this.setState({
      searchTerm: event.target.value
    })
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

  onSearch = async () => {
    if (!this.checkWhitespace(this.state.searchTerm)) {
      this.setState({ searchTermFailedValidation: true })
      return
    }
    this.setState({ searchTermFailedValidation: false })
    let searchUrl = 'searchInSurvey/' + this.props.surveyId + '?searchTerm=' + this.state.searchTerm

    if (this.state.selectedCollectionExercise) {
      searchUrl += '&collexId=' + this.state.selectedCollectionExercise
    }

    if (this.state.selectedRefusalFilter) {
      searchUrl += '&refusal=' + this.state.selectedRefusalFilter
    }

    if (this.state.selectedReceiptedFilter) {
      searchUrl += '&receipted=' + this.state.selectedReceiptedFilter
    }

    if (this.state.selectedInvalidFilter) {
      searchUrl += '&invalid=' + this.state.selectedInvalidFilter
    }

    if (this.state.selectedLaunchedFilter) {
      searchUrl += '&launched=' + this.state.selectedLaunchedFilter
    }

    this.onSearchExecuteAndPopulateList(searchUrl)
  }

  checkWhitespace = (valueToValidate) => {
    return valueToValidate.trim();
  }

  isNumeric = (str) => {
    return /^\+?\d+$/.test(str)
  }

  onFilterCollectionExercise = (event) => {
    this.setState({
      selectedCollectionExercise: event.target.value
    })
  }

  onFilterRefusal = (event) => {
    this.setState({
      selectedRefusalFilter: event.target.value
    })
  }
  onFilterReceipted = (event) => {
    this.setState({
      selectedReceiptedFilter: event.target.value
    })
  }

  onFilterInvalid = (event) => {
    this.setState({
      selectedInvalidFilter: event.target.value
    })
  }

  onFilterLaunched = (event) => {
    this.setState({
      selectedLaunchedFilter: event.target.value
    })
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

    const noFilterMenuItem = <MenuItem key={'NO FILTER'} value={''}>{'NO FILTER'}</MenuItem>
    let collectionExerciseMenuItems = []
    collectionExerciseMenuItems.push(noFilterMenuItem)
    collectionExerciseMenuItems.push(this.state.collectionExercises.map(collex => (
      <MenuItem key={collex.name} value={collex._links.self.href.split("/")[4]}>{collex.name}</MenuItem>
    )))

    const refusalMenuItems = []
    refusalMenuItems.push(noFilterMenuItem)
    refusalMenuItems.push(this.state.refusalTypes.map(refusalType => (
      <MenuItem key={refusalType} value={refusalType}>{refusalType}</MenuItem>
    )))


    const trueOrFalseFilterMenuItems = []
    trueOrFalseFilterMenuItems.push(noFilterMenuItem)
    trueOrFalseFilterMenuItems.push([<MenuItem key={'true'} value={'true'}>{'TRUE'}</MenuItem>,
    <MenuItem key={'false'} value={'false'}>{'FALSE'}</MenuItem>])

    return (
      <div style={{ padding: 20 }}>
        <div id="sampleSearchDiv">
          <div id="searchTxtDiv" style={{ margin: 10 }}>
            <Typography variant="h4" color="inherit" style={{ marginBottom: 10 }}>
              Survey: {this.props.surveyName}
            </Typography>

            <TextField
              required
              style={{ minWidth: SEARCH_FIELD_WIDTH }}
              error={this.state.searchTermFailedValidation}
              label="Search All Sample Data"
              onChange={this.onSearchChange}
              value={this.state.searchTerm} />
            <Button onClick={this.onSearch} variant="contained"
              style={{ margin: 10, minWidth: SEARCH_BUTTON_WIDTH }}>
              Search Sample Data
            </Button>
          </div>
          <div id="searchSampleFiltersDiv">
            <FormControl style={{ minWidth: 200, marginLeft: 20, marginRight: 20 }}>
              <InputLabel>Collection Exercise</InputLabel>
              <Select
                onChange={this.onFilterCollectionExercise}
                value={this.state.selectedCollectionExercise}>
                {collectionExerciseMenuItems}
              </Select>
            </FormControl>

            <FormControl style={{ minWidth: 200, marginLeft: 20, marginRight: 20 }}>
              <InputLabel>Refused</InputLabel>
              <Select
                onChange={this.onFilterRefusal}
                value={this.state.selectedRefusalFilter}>
                {refusalMenuItems}
              </Select>
            </FormControl>

            <FormControl style={BOOL_FILTER_STYLE}>
              <InputLabel>Receipted</InputLabel>
              <Select
                onChange={this.onFilterReceipted}
                value={this.state.selectedReceiptedFilter}>
                {trueOrFalseFilterMenuItems}
              </Select>
            </FormControl>

            <FormControl style={BOOL_FILTER_STYLE}>
              <InputLabel>Invalid</InputLabel>
              <Select
                onChange={this.onFilterInvalid}
                value={this.state.selectedInvalidFilter}>
                {trueOrFalseFilterMenuItems}
              </Select>
            </FormControl>

            <FormControl style={BOOL_FILTER_STYLE}>
              <InputLabel>Launched</InputLabel>
              <Select
                onChange={this.onFilterLaunched}
                value={this.state.selectedLaunchedFilter}>
                {trueOrFalseFilterMenuItems}
              </Select>
            </FormControl>
          </div>
        </div>

        <SurveySimpleSearchInput surveyId={this.props.surveyId}
          onSearchExecuteAndPopulateList={this.onSearchExecuteAndPopulateList}
          isNumeric={this.isNumeric}
          urlpathName='caseRef'
          displayText='Search By Case Ref'>
        </SurveySimpleSearchInput>

        <SurveySimpleSearchInput surveyId={this.props.surveyId}
          onSearchExecuteAndPopulateList={this.onSearchExecuteAndPopulateList}
          isNumeric={this.isNumeric}
          urlpathName='qid'
          displayText='Search By Qid'>
        </SurveySimpleSearchInput>

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
      </div >

    )
  }

}

export default SurveyCaseSearch
