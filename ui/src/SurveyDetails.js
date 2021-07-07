import React, { Component } from 'react';
import '@fontsource/roboto';
import { Button, Dialog, DialogContent, TextField, Paper, Typography, MenuItem, FormControl, InputLabel, Select } from '@material-ui/core';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableContainer from '@material-ui/core/TableContainer';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import { uuidv4 } from './common'

class SurveyDetails extends Component {
  state = {
    authorisedActivities: [],
    collectionExercises: [],
    createCollectionExerciseDialogDisplayed: false,
    validationError: false,
    newCollectionExerciseName: '',
    allowActionRulePrintTemplateDialogDisplayed: false,
    allowFulfilmentPrintTemplateDialogDisplayed: false,
    actionRulePrintTemplates: [],
    fulfilmentPrintTemplates: [],
    allowableActionRulePrintTemplates: [],
    allowableFulfilmentPrintTemplates: [],
    printTemplateToAllow: '',
    printTemplateValidationError: false,
  }

  componentDidMount() {
    this.getAuthorisedActivities() // Only need to do this once; don't refresh it repeatedly as it changes infrequently
    this.refreshDataFromBackend()

    this.interval = setInterval(
      () => this.refreshDataFromBackend(),
      1000
    )
  }

  componentWillUnmount() {
    clearInterval(this.interval)
  }

  getAuthorisedActivities = async () => {
    const response = await fetch('/auth')
    const authJson = await response.json()

    this.setState({ authorisedActivities: authJson })
  }

  refreshDataFromBackend = async () => {
    this.getCollectionExercises()
    const allPrintTemplates = await this.getAllPrintTemplates()
    const actionRulePrintTemplates = await this.getActionRulePrintTemplates()
    const fulfilmentPrintTemplates = await this.getFulfilmentPrintTemplates()
    let allowableActionRulePrintTemplates = []
    let allowableFulfilmentPrintTemplates = []

    allPrintTemplates.forEach(packCode => {
      if (!actionRulePrintTemplates.includes(packCode)) {
        allowableActionRulePrintTemplates.push(packCode)
      }

      if (!fulfilmentPrintTemplates.includes(packCode)) {
        allowableFulfilmentPrintTemplates.push(packCode)
      }
    })

    this.setState({
      actionRulePrintTemplates: actionRulePrintTemplates,
      fulfilmentPrintTemplates: fulfilmentPrintTemplates,
      allowableActionRulePrintTemplates: allowableActionRulePrintTemplates,
      allowableFulfilmentPrintTemplates: allowableFulfilmentPrintTemplates
    })
  }

  getCollectionExercises = async () => {
    const response = await fetch('/surveys/' + this.props.surveyId + '/collectionExercises')
    const collexJson = await response.json()

    this.setState({ collectionExercises: collexJson._embedded.collectionExercises })
  }

  getAllPrintTemplates = async () => {
    const response = await fetch('/printTemplates')
    const templateJson = await response.json()

    let templates = []

    for (let i = 0; i < templateJson._embedded.printTemplates.length; i++) {
      const packCode = templateJson._embedded.printTemplates[i]._links.self.href.split('/')[4]
      templates.push(packCode)
    }

    return templates;
  }

  getActionRulePrintTemplates = async () => {
    const response = await fetch('/surveys/' + this.props.surveyId + '/actionRulePrintTemplates')
    const printTemplatesJson = await response.json()
    const printTemplates = printTemplatesJson._embedded.actionRuleSurveyPrintTemplates

    let templates = []

    for (let i = 0; i < printTemplates.length; i++) {
      const print_template_url = new URL(printTemplates[i]._links.printTemplate.href)

      const printTemplateResponse = await fetch(print_template_url.pathname)
      const printTemplateJson = await printTemplateResponse.json()
      const packCode = printTemplateJson._links.self.href.split('/')[4]

      templates.push(packCode)
    }

    return templates
  }

  getFulfilmentPrintTemplates = async () => {
    const response = await fetch('/surveys/' + this.props.surveyId + '/fulfilmentPrintTemplates')
    const printTemplatesJson = await response.json()
    const printTemplates = printTemplatesJson._embedded.fulfilmentSurveyPrintTemplates

    let templates = []

    for (let i = 0; i < printTemplates.length; i++) {
      const printTemplateUrl = new URL(printTemplates[i]._links.printTemplate.href)

      const printTemplateResponse = await fetch(printTemplateUrl.pathname)
      const printTemplateJson = await printTemplateResponse.json()
      const packCode = printTemplateJson._links.self.href.split('/')[4]

      templates.push(packCode)
    }

    return templates
  }

  openDialog = () => {
    this.setState({
      newCollectionExerciseName: '',
      validationError: false,
      createCollectionExerciseDialogDisplayed: true
    })
  }

  closeDialog = () => {
    this.setState({ createCollectionExerciseDialogDisplayed: false })
  }

  onNewCollectionExerciseNameChange = (event) => {
    const resetValidation = !event.target.value.trim()
    this.setState({
      validationError: resetValidation,
      newCollectionExerciseName: event.target.value
    })
  }

  onCreateCollectionExercise = async () => {
    if (!this.state.newCollectionExerciseName.trim()) {
      this.setState({ validationError: true })
      return
    }

    const newCollectionExercise = {
      id: uuidv4(),
      name: this.state.newCollectionExerciseName,
      survey: 'surveys/' + this.props.surveyId
    }

    const response = await fetch('/collectionExercises', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(newCollectionExercise)
    })

    if (response.ok) {
      this.setState({ createCollectionExerciseDialogDisplayed: false })
    }
  }

  openActionRulePrintTemplateDialog = () => {
    this.setState({
      allowActionRulePrintTemplateDialogDisplayed: true,
      printTemplateToAllow: '',
      printTemplateValidationError: false
    })
  }

  openFulfilmentPrintTemplateDialog = () => {
    this.setState({
      allowFulfilmentPrintTemplateDialogDisplayed: true,
      printTemplateToAllow: '',
      printTemplateValidationError: false
    })
  }

  onAllowActionRulePrintTemplate = async () => {
    if (!this.state.printTemplateToAllow) {
      this.setState({
        printTemplateValidationError: true
      })

      return
    }

    const newAllowPrintTemplate = {
      id: uuidv4(),
      survey: '/surveys/' + this.props.surveyId,
      printTemplate: '/printTemplates/' + this.state.printTemplateToAllow
    }

    const response = await fetch('/actionRuleSurveyPrintTemplates', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(newAllowPrintTemplate)
    })

    if (response.ok) {
      this.setState({ allowActionRulePrintTemplateDialogDisplayed: false })
    }
  }

  onAllowFulfilmentPrintTemplate = async () => {
    if (!this.state.printTemplateToAllow) {
      this.setState({
        printTemplateValidationError: true
      })

      return
    }

    const newAllowPrintTemplate = {
      id: uuidv4(),
      survey: '/surveys/' + this.props.surveyId,
      printTemplate: '/printTemplates/' + this.state.printTemplateToAllow
    }

    const response = await fetch('/fulfilmentSurveyPrintTemplates', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(newAllowPrintTemplate)
    })

    if (response.ok) {
      this.setState({ allowFulfilmentPrintTemplateDialogDisplayed: false })
    }
  }

  closeAllowActionRulePrintTemplateDialog = () => {
    this.setState({ allowActionRulePrintTemplateDialogDisplayed: false })
  }

  closeAllowFulfilmentPrintTemplateDialog = () => {
    this.setState({ allowFulfilmentPrintTemplateDialogDisplayed: false })
  }

  onPrintTemplateChange = (event) => {
    this.setState({ printTemplateToAllow: event.target.value })
  }

  render() {
    const collectionExerciseTableRows = this.state.collectionExercises.map(collex => (
      <TableRow key={collex.name}>
        <TableCell component="th" scope="row">
          {collex.name}
        </TableCell>
        <TableCell align="right">
          <Button
            onClick={() => this.props.onOpenCollectionExercise(collex)}
            variant="contained">
            Open
          </Button>
        </TableCell>
      </TableRow>
    ))

    const actionRulePrintTemplateTableRows = this.state.actionRulePrintTemplates.map(printTemplate =>
      <TableRow key={printTemplate}>
        <TableCell component="th" scope="row">
          {printTemplate}
        </TableCell>
      </TableRow>
    )

    const fulfilmentPrintTemplateTableRows = this.state.fulfilmentPrintTemplates.map(printTemplate =>
      <TableRow key={printTemplate}>
        <TableCell component="th" scope="row">
          {printTemplate}
        </TableCell>
      </TableRow>
    )

    const actionRulePrintTemplateMenuItems = this.state.allowableActionRulePrintTemplates.map(packCode =>
      <MenuItem key={packCode} value={packCode}>{packCode}</MenuItem>
    )

    const fulfilmentPrintTemplateMenuItems = this.state.allowableFulfilmentPrintTemplates.map(packCode =>
      <MenuItem key={packCode} value={packCode}>{packCode}</MenuItem>
    )

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
                <TableCell>Collection Exercise Name</TableCell>
                <TableCell align="right">Action</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {collectionExerciseTableRows}
            </TableBody>
          </Table>
        </TableContainer>
        {this.state.authorisedActivities.includes('ALLOW_PRINT_TEMPLATE_ON_ACTION_RULE') &&
          <Button variant="contained" onClick={this.openActionRulePrintTemplateDialog} style={{ marginTop: 20 }}>Allow Print Template on Action Rule</Button>
        }
        {this.state.authorisedActivities.includes('LIST_ALLOWED_PRINT_TEMPLATES_ON_ACTION_RULES') &&
          <TableContainer component={Paper} style={{ marginTop: 20 }}>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Pack Code</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {actionRulePrintTemplateTableRows}
              </TableBody>
            </Table>
          </TableContainer>
        }
        {this.state.authorisedActivities.includes('ALLOW_PRINT_TEMPLATE_ON_FULFILMENT') &&
          <Button variant="contained" onClick={this.openFulfilmentPrintTemplateDialog} style={{ marginTop: 20 }}>Allow Print Template on Fulfilment</Button>
        }
        {this.state.authorisedActivities.includes('LIST_ALLOWED_PRINT_TEMPLATES_ON_FULFILMENTS') &&
          <TableContainer component={Paper} style={{ marginTop: 20 }}>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Pack Code</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {fulfilmentPrintTemplateTableRows}
              </TableBody>
            </Table>
          </TableContainer>
        }
        <Dialog open={this.state.createCollectionExerciseDialogDisplayed}>
          <DialogContent style={{ padding: 30 }}>
            <div>
              <div>
                <TextField
                  required
                  fullWidth={true}
                  error={this.state.validationError}
                  id="standard-required"
                  label="Collection exercise name"
                  onChange={this.onNewCollectionExerciseNameChange}
                  value={this.state.newCollectionExerciseName} />
              </div>
              <div style={{ marginTop: 10 }}>
                <Button onClick={this.onCreateCollectionExercise} variant="contained" style={{ margin: 10 }}>
                  Create collection exercise
                </Button>
                <Button onClick={this.closeDialog} variant="contained" style={{ margin: 10 }}>
                  Cancel
                </Button>
              </div>
            </div>
          </DialogContent>
        </Dialog>
        <Dialog open={this.state.allowActionRulePrintTemplateDialogDisplayed}>
          <DialogContent style={{ padding: 30 }}>
            <div>
              <div>
                <FormControl
                  required
                  fullWidth={true}>
                  <InputLabel>Print Template</InputLabel>
                  <Select
                    onChange={this.onPrintTemplateChange}
                    value={this.state.printTemplateToAllow}
                    error={this.state.printTemplateValidationError}>
                    {actionRulePrintTemplateMenuItems}
                  </Select>
                </FormControl>
              </div>
              <div style={{ marginTop: 10 }}>
                <Button onClick={this.onAllowActionRulePrintTemplate} variant="contained" style={{ margin: 10 }}>
                  Allow
                </Button>
                <Button onClick={this.closeAllowActionRulePrintTemplateDialog} variant="contained" style={{ margin: 10 }}>
                  Cancel
                </Button>
              </div>
            </div>
          </DialogContent>
        </Dialog>
        <Dialog open={this.state.allowFulfilmentPrintTemplateDialogDisplayed}>
          <DialogContent style={{ padding: 30 }}>
            <div>
              <div>
                <FormControl
                  required
                  fullWidth={true}>
                  <InputLabel>Print Template</InputLabel>
                  <Select
                    onChange={this.onPrintTemplateChange}
                    value={this.state.printTemplateToAllow}
                    error={this.state.printTemplateValidationError}>
                    {fulfilmentPrintTemplateMenuItems}
                  </Select>
                </FormControl>
              </div>
              <div style={{ marginTop: 10 }}>
                <Button onClick={this.onAllowFulfilmentPrintTemplate} variant="contained" style={{ margin: 10 }}>
                  Allow
                </Button>
                <Button onClick={this.closeAllowFulfilmentPrintTemplateDialog} variant="contained" style={{ margin: 10 }}>
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

export default SurveyDetails