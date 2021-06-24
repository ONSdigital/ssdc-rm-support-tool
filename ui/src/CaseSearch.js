import React, { Component } from 'react';
import '@fontsource/roboto';
import {
  Button,
  Dialog,
  DialogContent,
  FormControl,
  InputLabel,
  MenuItem,
  Paper,
  Select,
  TextField,
  Typography
} from '@material-ui/core';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableContainer from '@material-ui/core/TableContainer';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import CaseDetails from './CaseDetails';


class CaseSearch extends Component {
  state = {
    cases: [],
    sampleColumns: [],
    searchDialogOpen: false,
    contains: '',
    containsValidationError: false,
    column: '',
    columnValidationError: false,
    showDetails: false,
    selectedCase: null
  }

  componentDidMount() {
    this.getSampleColumns()
  }

  getSampleColumns = async () => {
    const response = await fetch('/collectionExercises/' + this.props.collectionExerciseId + '/survey')
    const survey_json = await response.json()

    let columns = []
    survey_json.sampleValidationRules.forEach(rule => {
      columns.push(rule.columnName)
    })

    this.setState({ sampleColumns: columns })
  }

  onSearch = async () => {
    var failedValidation = false

    if (!this.state.contains.trim()) {
      this.setState({ containsValidationError: true })
      failedValidation = true
    }

    if (!this.state.column.trim()) {
      this.setState({ columnValidationError: true })
      failedValidation = true
    }

    if (failedValidation) {
      return
    }

    const response = await fetch('/cases/search/findBySampleContains?collexId=' + this.props.collectionExerciseId + '&key=' + this.state.column + '&value=' + this.state.contains)
    const search_result_json = await response.json()
    this.setState({
      cases: search_result_json._embedded.cases,
      searchDialogOpen: false
    })
  }

  onOpenSearchDialog = () => {
    this.setState({
      searchDialogOpen: true,
      contains: '',
      containsValidationError: false,
      column: '',
      columnValidationError: false,
      selectedCase: null
    })
  }

  onCloseDialog = () => {
    this.setState({ searchDialogOpen: false })
  }

  onContainsChange = (event) => {
    const resetValidation = !event.target.value.trim()
    this.setState({
      containsValidationError: resetValidation,
      contains: event.target.value
    })
  }

  onColumnChange = (event) => {
    const resetValidation = !event.target.value.trim()
    this.setState({
      columnValidationError: resetValidation,
      column: event.target.value
    })
  }

  onOpenCaseDetails = (caze) => {
    this.setState({
      showDetails: true,
      selectedCase: caze
    })
  }

  onCloseDetails = () => {
    this.setState({
      showDetails: false
    })
  }

  getCaseCells = (caze) => {
    let caseCells = this.state.sampleColumns.map(sampleColumn => (
      <TableCell>{caze.sample[sampleColumn]}</TableCell>
    ))

    caseCells.push((
      <TableCell>
        <Button
          onClick={() => this.onOpenCaseDetails(caze)}
          variant="contained">
          Open
        </Button>
      </TableCell>
    ))

    return caseCells
  }

  render() {
    const searchColumnMenuItems = this.state.sampleColumns.map((sampleColumn, index) => (
      <MenuItem key={sampleColumn} value={sampleColumn}>{sampleColumn}</MenuItem>
    ))

    let tableHeaderRows = this.state.sampleColumns.map((sampleColumn, index) => (
      <TableCell>{sampleColumn}</TableCell>
    ))

    tableHeaderRows.push((
      <TableCell>Action</TableCell>
    ))

    const caseTableRows = this.state.cases.map((caze, index) => (
      <TableRow key={index}>
        {this.getCaseCells(caze)}
      </TableRow>
    ))

    return (
      <div style={{ padding: 20 }}>
        <Typography variant="h4" color="inherit" style={{ marginBottom: 20 }}>
          Cases for Collection Exercise: {this.props.collectionExerciseName}
        </Typography>
        <Button variant="contained" onClick={this.onOpenSearchDialog}>Search</Button>
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
        <Dialog open={this.state.searchDialogOpen}>
          <DialogContent style={{ padding: 30 }}>
            <div>
              <div>
                <FormControl fullWidth={true}>
                  <InputLabel>Sample column</InputLabel>
                  <Select
                    onChange={this.onColumnChange}
                    error={this.state.columnValidationError}>
                    {searchColumnMenuItems}
                  </Select>
                </FormControl>
              </div>
              <TextField
                required
                fullWidth={true}
                style={{ marginTop: 20 }}
                error={this.state.containsValidationError}
                label="Contains"
                onChange={this.onContainsChange}
                value={this.state.contains} />
              <div style={{ marginTop: 10 }}>
                <Button onClick={this.onSearch} variant="contained" style={{ margin: 10 }}>
                  Search
                </Button>
                <Button onClick={this.onCloseDialog} variant="contained" style={{ margin: 10 }}>
                  Cancel
                </Button>
              </div>
            </div>
          </DialogContent>
        </Dialog>
        <CaseDetails showDetails={this.state.showDetails} onCloseDetails={this.onCloseDetails} case={this.state.selectedCase} />
      </div>
    )
  }
}

export default CaseSearch