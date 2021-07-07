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
import { uuidv4 } from './common'
import SampleUpload from "./SampleUpload";


class CollectionExerciseDetails extends Component {
  state = {
    authorisedActivities: [],
    actionRules: [],
    packCodes: [],
    printTemplateHrefToPackCodeMap: new Map(),
    createActionRulesDialogDisplayed: false,
    packCodeValidationError: false,
    actionRuleTypeValidationError: false,
    newActionRulePackCode: '',
    newActionRuleClassifiers: '',
    newActionRuleType: ''
  }

  componentDidMount() {
    this.getAuthorisedActivities() // Only need to do this once; don't refresh it repeatedly as it changes infrequently
    this.getActionRules()
    this.getPackCodes()

    this.interval = setInterval(
      () => this.getActionRules(),
      1000
    )
  }

  componentWillUnmount() {
    clearInterval(this.interval)
  }

  getAuthorisedActivities = async () => {
    const response = await fetch('/auth?surveyId=' + this.props.surveyId)

    // TODO: We need more elegant error handling throughout the whole application, but this will at least protect temporarily
    if (!response.ok) {
      return
    }

    const authJson = await response.json()

    this.setState({ authorisedActivities: authJson })
  }

  getActionRules = async () => {
    const response = await fetch('/collectionExercises/' + this.props.collectionExerciseId + '/actionRules')
    const actionRuleJson = await response.json()
    const actionRules = actionRuleJson._embedded.actionRules

    let printTemplateHrefToPackCodeMap = new Map()

    for (let i = 0; i < actionRules.length; i++) {
      if (actionRules[i].type === 'PRINT') {
        const printTemplateUrl = new URL(actionRules[i]._links.printTemplate.href)
        const printTemplateResponse = await fetch(printTemplateUrl.pathname)
        const printTemplateJson = await printTemplateResponse.json()
        const packCode = printTemplateJson._links.self.href.split('/')[4]

        printTemplateHrefToPackCodeMap.set(actionRules[i]._links.printTemplate.href, packCode)
      }
    }

    this.setState({
      actionRules: actionRules,
      printTemplateHrefToPackCodeMap: printTemplateHrefToPackCodeMap
    })
  }

  getPackCodes = async () => {
    const response = await fetch('/surveys/' + this.props.surveyId + '/actionRulePrintTemplates')
    const printTemplatesJson = await response.json()
    const printTemplates = printTemplatesJson._embedded.actionRuleSurveyPrintTemplates

    let packCodes = []

    for (let i = 0; i < printTemplates.length; i++) {
      const printTemplateUrl = new URL(printTemplates[i]._links.printTemplate.href)

      const printTemplateResponse = await fetch(printTemplateUrl.pathname)
      const printTemplateJson = await printTemplateResponse.json()
      const packCode = printTemplateJson._links.self.href.split('/')[4]

      packCodes.push(packCode)
    }

    this.setState({ packCodes: packCodes })
  }

  openDialog = () => {
    this.setState({
      newActionRuleType: '',
      actionRuleTypeValidationError: false,
      newActionRulePackCode: '',
      packCodeValidationError: false,
      newActionRuleClassifiers: '',
      createActionRulesDialogDisplayed: true,
      newActionRuleTriggerDate: this.getTimeNowForDateTimePicker()
    })
  }

  closeDialog = () => {
    this.setState({ createActionRulesDialogDisplayed: false })
  }

  onNewActionRulePackCodeChange = (event) => {
    this.setState({
      packCodeValidationError: false,
      newActionRulePackCode: event.target.value
    })
  }

  onNewActionRuleClassifiersChange = (event) => {
    this.setState({
      newActionRuleClassifiers: event.target.value
    })
  }

  onNewActionRuleTriggerDateChange = (event) => {
    this.setState({ newActionRuleTriggerDate: event.target.value })
  }

  onNewActionRuleTypeChange = (event) => {
    this.setState({
      newActionRuleType: event.target.value,
      actionRuleTypeValidationError: false
    })
  }

  onCreateActionRule = async () => {
    var failedValidation = false

    if (!this.state.newActionRuleType) {
      this.setState({ actionRuleTypeValidationError: true })
      failedValidation = true
    }

    if (!this.state.newActionRulePackCode && this.state.newActionRuleType === 'PRINT') {
      this.setState({ packCodeValidationError: true })
      failedValidation = true
    }

    if (failedValidation) {
      return
    }

    let printTemplate = null
    if (this.state.newActionRuleType === 'PRINT') {
      printTemplate = 'printTemplates/' + this.state.newActionRulePackCode
    }

    const newActionRule = {
      id: uuidv4(),
      type: this.state.newActionRuleType,
      triggerDateTime: new Date(this.state.newActionRuleTriggerDate).toISOString(),
      hasTriggered: false,
      classifiers: this.state.newActionRuleClassifiers,
      printTemplate: printTemplate,
      collectionExercise: 'collectionExercises/' + this.props.collectionExerciseId
    }

    const response = await fetch('/actionRules', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(newActionRule)
    })

    if (response.ok) {
      this.setState({ createActionRulesDialogDisplayed: false })
    }
  }

  getTimeNowForDateTimePicker = () => {
    var dateNow = new Date()
    dateNow.setMinutes(dateNow.getMinutes() - dateNow.getTimezoneOffset())
    return dateNow.toJSON().slice(0, 16)
  }

  render() {
    const actionRuleTableRows = this.state.actionRules.map((actionRule, index) => {
      let packCode = ''

      if (actionRule.type === 'PRINT') {
        packCode = this.state.printTemplateHrefToPackCodeMap.get(actionRule._links.printTemplate.href)
      }

      return (
        <TableRow key={index}>
          <TableCell component="th" scope="row">
            {actionRule.type}
          </TableCell>
          <TableCell component="th" scope="row">
            {actionRule.triggerDateTime}
          </TableCell>
          <TableCell component="th" scope="row">
            {actionRule.hasTriggered ? "YES" : "NO"}
          </TableCell>
          <TableCell component="th" scope="row">
            {actionRule.classifiers}
          </TableCell>
          <TableCell component="th" scope="row">
            {packCode}
          </TableCell>
        </TableRow>
      )
    })

    const packCodeMenuItems = this.state.packCodes.map(packCode => (
      <MenuItem key={packCode} value={packCode}>{packCode}</MenuItem>
    ))

    return (
      <div style={{ padding: 20 }}>
        <Typography variant="h4" color="inherit" style={{ marginBottom: 20 }}>
          Collection Exercise: {this.props.collectionExerciseName}
        </Typography>
        <div>
          <Button variant="contained" onClick={this.props.onOpenCaseSearch}>Search Cases</Button>
        </div>
        <div style={{ marginTop: 20 }}>
          <Button variant="contained" onClick={this.openDialog}>Create Action Rule</Button>
        </div>
        <TableContainer component={Paper} style={{ marginTop: 20 }}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Type</TableCell>
                <TableCell>Trigger date</TableCell>
                <TableCell>Has triggered?</TableCell>
                <TableCell>Classifiers</TableCell>
                <TableCell>Pack Code</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {actionRuleTableRows}
            </TableBody>
          </Table>
        </TableContainer>
        {(this.state.authorisedActivities.includes('LOAD_SAMPLE') || this.state.authorisedActivities.includes('VIEW_SAMPLE_LOAD_PROGRESS')) &&
          <SampleUpload collectionExerciseId={this.props.collectionExerciseId} />
        }
        <Dialog open={this.state.createActionRulesDialogDisplayed}>
          <DialogContent style={{ padding: 30 }}>
            <div>
              <div>
                <FormControl
                  required
                  fullWidth={true}>
                  <InputLabel>Type</InputLabel>
                  <Select
                    onChange={this.onNewActionRuleTypeChange}
                    value={this.state.newActionRuleType}
                    error={this.state.actionRuleTypeValidationError}>
                    <MenuItem value={'PRINT'}>PRINT</MenuItem>
                    <MenuItem value={'FACE_TO_FACE'}>FACE-TO-FACE</MenuItem>
                    <MenuItem value={'OUTBOUND_PHONE'}>OUTBOUND PHONE</MenuItem>
                    <MenuItem value={'DEACTIVATE_UAC'}>DEACTIVATE UAC</MenuItem>
                  </Select>
                </FormControl>
                {this.state.newActionRuleType === 'PRINT' &&
                  <FormControl
                    required
                    fullWidth={true}>
                    <InputLabel>Pack Code</InputLabel>
                    <Select
                      onChange={this.onNewActionRulePackCodeChange}
                      value={this.state.newActionRulePackCode}
                      error={this.state.packCodeValidationError}>
                      {packCodeMenuItems}
                    </Select>
                  </FormControl>
                }
                <TextField
                  fullWidth={true}
                  style={{ marginTop: 20 }}
                  error={this.state.classifiersValidationError}
                  label="Classifiers"
                  onChange={this.onNewActionRuleClassifiersChange}
                  value={this.state.newActionRuleClassifiers} />
                <TextField
                  label="Trigger Date"
                  type="datetime-local"
                  value={this.state.newActionRuleTriggerDate}
                  onChange={this.onNewActionRuleTriggerDateChange}
                  style={{ marginTop: 20 }}
                  InputLabelProps={{
                    shrink: true,
                  }} />
              </div>
              <div style={{ marginTop: 10 }}>
                <Button onClick={this.onCreateActionRule} variant="contained" style={{ margin: 10 }}>
                  Create action rule
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

export default CollectionExerciseDetails