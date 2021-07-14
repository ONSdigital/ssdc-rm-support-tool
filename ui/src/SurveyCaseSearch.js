import React, {Component} from 'react';
import '@fontsource/roboto';
import {Button, FormControl, InputLabel, MenuItem, Paper, Select, TextField, Typography} from '@material-ui/core';
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
    qid: '',
    textFieldWidth: 400,
    SearchButtonWidth: 200,
    noCasesFoundMsg: '',
    collectionExercises: [],
    selectedCollectionExercise: '',
  }

  componentDidMount() {
    this.getSampleColumns()
    this.getCollectionExercises()
  }

  getCollectionExercises = async () => {
    const response = await fetch('/surveys/' + this.props.surveyId + '/collectionExercises')
    const collexJson = await response.json()

    this.setState({collectionExercises: collexJson._embedded.collectionExercises})
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

  onQidChange = (event) => {
    this.setState({
      qid: event.target.value
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

    this.setState({caseSearchResults: matchedCasesJson})

    if (this.state.caseSearchResults.length === 0) {
      this.setState({noCasesFoundMsg: 'No cases found matching search'})
    }
  }

  onSearch = async () => {
    if (!this.checkValidation(this.state.searchTerm)) {
      this.setState({searchTermFailedValidation: true})
      return
    }

    this.onSearchExecuteAndPopulateList('searchInSurvey/' + this.props.surveyId + '?searchTerm=' + this.state.searchTerm)
  }

  onCaseRefSearch = async () => {
    if (!this.checkValidation(this.state.caseRef)) {
      this.setState({caseRefSearchFailedValidation: true})
      return
    }

    this.onSearchExecuteAndPopulateList('searchInSurvey/' + this.props.surveyId + '/caseRef/' + this.state.caseRef)
  }

  onQidSearch = async () => {
    if (!this.checkValidation(this.state.qid)) {
      this.setState({qidSearchFailedValidation: true})
      return
    }

    this.onSearchExecuteAndPopulateList('searchInSurvey/' + this.props.surveyId + '/qid/' + this.state.qid)
  }

  checkValidation = (valueToValidate) => {
    return valueToValidate.trim();
  }

  onFilterCollectionExercise = (event) => {
    this.setState({
      selectedCollectionExercise: event.target.value
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

    this.setState({sampleColumns: columns})
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

    const defaultMenuItem = <MenuItem key={'Any'} value={''}>{'Any'}</MenuItem>
    let collectionExerciseMenuItems = []
    collectionExerciseMenuItems.push(defaultMenuItem)
    collectionExerciseMenuItems.push(this.state.collectionExercises.map(collex => (
        <MenuItem key={collex.name} value={collex._links.self.href.split("/")[4]}>{collex.name}</MenuItem>
    )))

    return (
        <div style={{padding: 20}}>
          <div style={{margin: 10}}>
            <Typography variant="h4" color="inherit" style={{marginBottom: 10}}>
              Survey: {this.props.surveyName}
            </Typography>

            <TextField
                required
                style={{minWidth: this.state.textFieldWidth}}
                error={this.state.searchTermFailedValidation}
                label="Search All Sample Data"
                onChange={this.onSearchChange}
                defaultValue={defaultMenuItem}
                value={this.state.searchTerm}/>
            <Button onClick={this.onSearch} variant="contained"
                    style={{margin: 10, minWidth: this.state.SearchButtonWidth}}>
              Search Sample Data
            </Button>

            <FormControl style={{minWidth: 200}}>
              <InputLabel>Collection Exercise</InputLabel>
              <Select
                  onChange={this.onFilterCollectionExercise}
                  value={this.state.selectedCollectionExercise}>
                {collectionExerciseMenuItems}
              </Select>
            </FormControl>
          </div>

          <div style={{margin: 10}}>
            <TextField
                required
                style={{minWidth: this.state.textFieldWidth}}
                error={this.state.caseRefSearchFailedValidation}
                label="caseRef search"
                onChange={this.onCaseRefChange}
                value={this.state.caseRef}/>

            <Button onClick={this.onCaseRefSearch} variant="contained"
                    style={{margin: 10, minWidth: this.state.SearchButtonWidth}}>
              Search By Case Ref
            </Button>
          </div>

          <div style={{margin: 10}}>
            <TextField
                required
                style={{minWidth: this.state.textFieldWidth}}
                error={this.state.qidSearchFailedValidation}
                label="qid search"
                onChange={this.onQidChange}
                value={this.state.qid}/>

            <Button onClick={this.onQidSearch} variant="contained"
                    style={{margin: 10, minWidth: this.state.SearchButtonWidth}}>
              Search By Qid
            </Button>
          </div>

          {(this.state.caseSearchResults.length > 0) &&
          < TableContainer component={Paper} style={{marginTop: 20}}>
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
          {(this.state.caseSearchResults.length === 0) &&
          <p>{this.state.noCasesFoundMsg}</p>
          }
        </div>

    )
  }

}

export default SurveyCaseSearch
