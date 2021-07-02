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
    actionRules: [],
    printSuppliers: [],
    createActionRulesDialogDisplayed: false,
    printSupplierValidationError: false,
    packCodeValidationError: false,
    templateValidationError: false,
    actionRuleTypeValidationError: false,
    newActionRulePrintSupplier: '',
    newActionRulePackCode: '',
    newActionRuleClassifiers: '',
    newActionRuleTemplate: '',
    newActionRuleType: ''
  }

  componentDidMount() {
    this.getActionRules()
    this.getPrintSuppliers()

    this.interval = setInterval(
      () => this.getActionRules(),
      1000
    )
  }

  getPrintSuppliers = async () => {
    const response = await fetch('/printsuppliers')
    const supplier_json = await response.json()

    this.setState({ printSuppliers: supplier_json })
  }

  componentWillUnmount() {
    clearInterval(this.interval)
  }

  getActionRules = async () => {
    const response = await fetch('/collectionExercises/' + this.props.collectionExerciseId + '/actionRules')
    const woc_json = await response.json()

    this.setState({ actionRules: woc_json._embedded.actionRules })
  }

  openDialog = () => {
    this.setState({
      newActionRuleType: '',
      actionRuleTypeValidationError: false,
      newActionRulePrintSupplier: '',
      printSupplierValidationError: false,
      newActionRulePackCode: '',
      packCodeValidationError: false,
      newActionRuleClassifiers: '',
      newActionRuleTemplate: '',
      templateValidationError: false,
      createActionRulesDialogDisplayed: true,
      newActionRuleTriggerDate: this.getTimeNowForDateTimePicker()
    })
  }

  closeDialog = () => {
    this.setState({ createActionRulesDialogDisplayed: false })
  }

  onNewActionRulePrintSupplierChange = (event) => {
    const resetValidation = !event.target.value.trim()
    this.setState({
      printSupplierValidationError: resetValidation,
      newActionRulePrintSupplier: event.target.value
    })
  }

  onNewActionRulePackCodeChange = (event) => {
    const resetValidation = !event.target.value.trim()
    this.setState({
      packCodeValidationError: resetValidation,
      newActionRulePackCode: event.target.value
    })
  }

  onNewActionRuleClassifiersChange = (event) => {
    this.setState({
      newActionRuleClassifiers: event.target.value
    })
  }

  onNewActionRuleTemplateChange = (event) => {
    const resetValidation = !event.target.value.trim()
    this.setState({
      templateValidationError: resetValidation,
      newActionRuleTemplate: event.target.value
    })
  }

  onNewActionRuleTriggerDateChange = (event) => {
    this.setState({ newActionRuleTriggerDate: event.target.value })
  }

  onNewActionRuleTypeChange = (event) => {
    this.setState({
      newActionRuleType: event.target.value,
      actionTypeValidationError: false
    })
  }

  onCreateActionRule = async () => {
    var failedValidation = false

    if (!this.state.newActionRuleType.trim()) {
      this.setState({ actionRuleTypeValidationError: true })
      failedValidation = true
    }

    if (!this.state.newActionRulePrintSupplier.trim()) {
      this.setState({ printSupplierValidationError: true })
      failedValidation = true
    }

    if (!this.state.newActionRulePackCode.trim()) {
      this.setState({ packCodeValidationError: true })
      failedValidation = true
    }

    if (!this.state.newActionRuleTemplate.trim()) {
      this.setState({ templateValidationError: true })
      failedValidation = true
    } else {
      try {
        const parsedJson = JSON.parse(this.state.newActionRuleTemplate)
        if (!Array.isArray(parsedJson)) {
          this.setState({ templateValidationError: true })
          failedValidation = true
        } else {
          const validTemplateItems = [
            'ADDRESS_LINE1', 'ADDRESS_LINE2', 'ADDRESS_LINE3', 'TOWN_NAME', 'POSTCODE', '__uac__', '__qid__', '__caseref__']
          parsedJson.forEach(
            item => {
              if (!validTemplateItems.includes(item)) {
                this.setState({ templateValidationError: true })
                failedValidation = true
              }
            })
        }
      } catch (err) {
        this.setState({ templateValidationError: true })
        failedValidation = true
      }
    }

    if (failedValidation) {
      return
    }

    const newActionRule = {
      id: uuidv4(),
      type: this.state.newActionRuleType,
      triggerDateTime: new Date(this.state.newActionRuleTriggerDate).toISOString(),
      hasTriggered: false,
      classifiers: this.state.newActionRuleClassifiers,
      template: JSON.parse(this.state.newActionRuleTemplate),
      packCode: this.state.newActionRulePackCode,
      printSupplier: this.state.newActionRulePrintSupplier,
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
    const actionRuleTableRows = this.state.actionRules.map((woc, index) => (
      <TableRow key={index}>
        <TableCell component="th" scope="row">
          {woc.type}
        </TableCell>
        <TableCell component="th" scope="row">
          {woc.triggerDateTime}
        </TableCell>
        <TableCell component="th" scope="row">
          {woc.hasTriggered ? "YES" : "NO"}
        </TableCell>
        <TableCell component="th" scope="row">
          {woc.classifiers}
        </TableCell>
        <TableCell component="th" scope="row">
          {JSON.stringify(woc.template)}
        </TableCell>
        <TableCell component="th" scope="row">
          {woc.printSupplier}
        </TableCell>
        <TableCell component="th" scope="row">
          {woc.packCode}
        </TableCell>
      </TableRow>
    ))

    const printSupplierMenuItems = this.state.printSuppliers.map(supplier => (
      <MenuItem value={supplier}>{supplier}</MenuItem>
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
                <TableCell>Template</TableCell>
                <TableCell>Print Supplier</TableCell>
                <TableCell>Pack Code</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {actionRuleTableRows}
            </TableBody>
          </Table>
        </TableContainer>
        <SampleUpload collectionExerciseId={this.props.collectionExerciseId} />
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
                <FormControl
                  required
                  fullWidth={true}>
                  <InputLabel>Print Supplier</InputLabel>
                  <Select
                    onChange={this.onNewActionRulePrintSupplierChange}
                    error={this.state.printSupplierValidationError}>
                    {printSupplierMenuItems}
                  </Select>
                </FormControl>
                <TextField
                  required
                  fullWidth={true}
                  style={{ marginTop: 20 }}
                  error={this.state.packCodeValidationError}
                  label="Pack Code"
                  onChange={this.onNewActionRulePackCodeChange}
                  value={this.state.newActionRulePackCode} />
                <TextField
                  fullWidth={true}
                  style={{ marginTop: 20 }}
                  error={this.state.classifiersValidationError}
                  label="Classifiers"
                  onChange={this.onNewActionRuleClassifiersChange}
                  value={this.state.newActionRuleClassifiers} />
                <TextField
                  required
                  fullWidth={true}
                  style={{ marginTop: 20 }}
                  error={this.state.templateValidationError}
                  label="Template"
                  onChange={this.onNewActionRuleTemplateChange}
                  value={this.state.newActionRuleTemplate} />
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