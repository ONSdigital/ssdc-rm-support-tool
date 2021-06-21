import React, { Component } from 'react';
import '@fontsource/roboto';
import { Button, Dialog, DialogContent, TextField, Paper } from '@material-ui/core';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableContainer from '@material-ui/core/TableContainer';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import { uuidv4 } from './common'

class Surveys extends Component {
  state = {
    surveys: [],
    createSurveyDialogDisplayed: false,
    validationError: false,
    newSurveyName: '',
    validationRulesValidationError: false,
    newSurveyValidationRules: ''
  }

  componentDidMount() {
    this.getSurveys()

    this.interval = setInterval(
      () => this.getSurveys(),
      1000
    )
  }

  componentWillUnmount() {
    clearInterval(this.interval)
  }

  getSurveys = async () => {
    const response = await fetch('/surveys')
    const survey_json = await response.json()

    this.setState({ surveys: survey_json._embedded.surveys })
  }

  openDialog = () => {
    this.setState({
      newSurveyName: '',
      validationError: false,
      validationRulesValidationError: false,
      newSurveyValidationRules: '',
      createSurveyDialogDisplayed: true
    })
  }

  closeDialog = () => {
    this.setState({ createSurveyDialogDisplayed: false })
  }

  onNewSurveyNameChange = (event) => {
    const resetValidation = !event.target.value.trim()
    this.setState({
      validationError: resetValidation,
      newSurveyName: event.target.value
    })
  }

  onNewSurveyValidationRulesChange = (event) => {
    const resetValidation = !event.target.value.trim()
    this.setState({
      validationRulesValidationError: resetValidation,
      newSurveyValidationRules: event.target.value
    })
  }

  onCreateSurvey = async () => {
    let validationFailed = false

    if (!this.state.newSurveyName.trim()) {
      this.setState({ validationError: true })
      validationFailed = true
    }

    if (!this.state.newSurveyValidationRules.trim()) {
      this.setState({ validationRulesValidationError: true })
      validationFailed = true
    }

    if (validationFailed) {
      return
    }

    const newSurvey = {
      id: uuidv4(),
      name: this.state.newSurveyName,
      sampleValidationRules: this.state.newSurveyValidationRules
    }

    await fetch('/surveys', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(newSurvey)
    })

    this.setState({ createSurveyDialogDisplayed: false })
  }

  render() {
    const surveyTableRows = this.state.surveys.map(survey => (
      <TableRow key={survey.name}>
        <TableCell component="th" scope="row">
          {survey.name}
        </TableCell>
        <TableCell align="right">
          <Button
            onClick={() => this.props.onOpenSurveyDetails(survey)}
            variant="contained">
            Open
          </Button>
        </TableCell>
      </TableRow>
    ))

    return (
      <div style={{ padding: 20 }}>
        <Button variant="contained" onClick={this.openDialog}>Create Survey</Button>
        <TableContainer component={Paper} style={{ marginTop: 20 }}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Survey Name</TableCell>
                <TableCell align="right">Action</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {surveyTableRows}
            </TableBody>
          </Table>
        </TableContainer>
        <Dialog open={this.state.createSurveyDialogDisplayed}>
          <DialogContent style={{ padding: 30 }}>
            <div>
              <div>
                <TextField
                  required
                  error={this.state.validationError}
                  id="standard-required"
                  label="Survey name"
                  onChange={this.onNewSurveyNameChange}
                  value={this.state.newSurveyName} />
                <TextField
                  required
                  fullWidth={true}
                  error={this.state.validationRulesValidationError}
                  id="standard-required"
                  label="Validation rules"
                  onChange={this.onNewSurveyValidationRulesChange}
                  value={this.state.newSurveyValidationRules} />
              </div>
              <div style={{ marginTop: 10 }}>
                <Button onClick={this.onCreateSurvey} variant="contained" style={{ margin: 10 }}>
                  Create survey
                </Button>
                <Button onClick={this.closeDialog} variant="contained" style={{ margin: 10 }}>
                  Cancel
                </Button>
              </div>
            </div>
          </DialogContent>
        </Dialog>
      </div>
    )
  }
}

export default Surveys