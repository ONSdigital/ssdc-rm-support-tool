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
    waveOfContacts: [],
    printSuppliers: [],
    createWaveOfContactsDialogDisplayed: false,
    printSupplierValidationError: false,
    packCodeValidationError: false,
    classifiersValidationError: false,
    templateValidationError: false,
    newWaveOfContactPrintSupplier: '',
    newWaveOfContactPackCode: '',
    newWaveOfContactClassifiers: '',
    newWaveOfContactTemplate: '',
  }

  componentDidMount() {
    this.getWaveOfContacts()
    this.getPrintSuppliers()

    this.interval = setInterval(
      () => this.getWaveOfContacts(),
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

  getWaveOfContacts = async () => {
    const response = await fetch('/collectionExercises/' + this.props.collectionExerciseId + '/waveOfContacts')
    const woc_json = await response.json()

    this.setState({ waveOfContacts: woc_json._embedded.waveOfContacts })
  }

  openDialog = () => {
    this.setState({
      newWaveOfContactPrintSupplier: '',
      printSupplierValidationError: false,
      newWaveOfContactPackCode: '',
      packCodeValidationError: false,
      newWaveOfContactClassifiers: '',
      classifiersValidationError: false,
      newWaveOfContactTemplate: '',
      templateValidationError: false,
      createWaveOfContactsDialogDisplayed: true,
      newWaveOfContactTriggerDate: this.getTimeNowForDateTimePicker()
    })
  }

  closeDialog = () => {
    this.setState({ createWaveOfContactsDialogDisplayed: false })
  }

  onNewWaveOfContactPrintSupplierChange = (event) => {
    const resetValidation = !event.target.value.trim()
    this.setState({
      printSupplierValidationError: resetValidation,
      newWaveOfContactPrintSupplier: event.target.value
    })
  }

  onNewWaveOfContactPackCodeChange = (event) => {
    const resetValidation = !event.target.value.trim()
    this.setState({
      packCodeValidationError: resetValidation,
      newWaveOfContactPackCode: event.target.value
    })
  }

  onNewWaveOfContactClassifiersChange = (event) => {
    const resetValidation = !event.target.value.trim()
    this.setState({
      classifiersValidationError: resetValidation,
      newWaveOfContactClassifiers: event.target.value
    })
  }

  onNewWaveOfContactTemplateChange = (event) => {
    const resetValidation = !event.target.value.trim()
    this.setState({
      templateValidationError: resetValidation,
      newWaveOfContactTemplate: event.target.value
    })
  }

  onNewWaveOfTriggerDateChange = (event) => {
    this.setState({ newWaveOfContactTriggerDate: event.target.value })
  }

  onCreateWaveOfContact = async () => {
    var failedValidation = false

    if (!this.state.newWaveOfContactPrintSupplier.trim()) {
      this.setState({ printSupplierValidationError: true })
      failedValidation = true
    }

    if (!this.state.newWaveOfContactPackCode.trim()) {
      this.setState({ packCodeValidationError: true })
      failedValidation = true
    }

    if (!this.state.newWaveOfContactClassifiers.trim()) {
      this.setState({ classifiersValidationError: true })
      failedValidation = true
    }

    if (!this.state.newWaveOfContactTemplate.trim()) {
      this.setState({ templateValidationError: true })
      failedValidation = true
    } else {
      try {
        const parsedJson = JSON.parse(this.state.newWaveOfContactTemplate)
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

    const newWaveOfContact = {
      id: uuidv4(),
      type: 'PRINT',
      triggerDateTime: new Date(this.state.newWaveOfContactTriggerDate).toISOString(),
      hasTriggered: false,
      classifiers: this.state.newWaveOfContactClassifiers,
      template: JSON.parse(this.state.newWaveOfContactTemplate),
      packCode: this.state.newWaveOfContactPackCode,
      printSupplier: this.state.newWaveOfContactPrintSupplier,
      collectionExercise: 'collectionExercises/' + this.props.collectionExerciseId
    }

    const response = await fetch('/waveOfContacts', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(newWaveOfContact)
    })

    if (response.ok) {
      this.setState({ createWaveOfContactsDialogDisplayed: false })
    }
  }

  getTimeNowForDateTimePicker = () => {
    var dateNow = new Date()
    dateNow.setMinutes(dateNow.getMinutes() - dateNow.getTimezoneOffset())
    return dateNow.toJSON().slice(0, 16)
  }

  render() {
    const waveOfContactTableRows = this.state.waveOfContacts.map((woc, index) => (
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
          <Button variant="contained" onClick={this.openDialog}>Create Wave of Contact</Button>
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
              {waveOfContactTableRows}
            </TableBody>
          </Table>
        </TableContainer>
        <SampleUpload collectionExerciseId={this.props.collectionExerciseId} />
        <Dialog open={this.state.createWaveOfContactsDialogDisplayed}>
          <DialogContent style={{ padding: 30 }}>
            <div>
              <div>
                <FormControl>
                  <InputLabel>Type</InputLabel>
                  <Select value={'PRINT'}>
                    <MenuItem value={'PRINT'}>PRINT</MenuItem>
                    <MenuItem value={'FACE_TO_FACE'}>FACE-TO-FACE</MenuItem>
                    <MenuItem value={'OUTBOUND_PHONE'}>OUTBOUND PHONE</MenuItem>
                  </Select>
                </FormControl>
                <FormControl
                  required
                  fullWidth={true}>
                  <InputLabel>Print Supplier</InputLabel>
                  <Select
                    onChange={this.onNewWaveOfContactPrintSupplierChange}
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
                  onChange={this.onNewWaveOfContactPackCodeChange}
                  value={this.state.newWaveOfContactPackCode} />
                <TextField
                  required
                  fullWidth={true}
                  style={{ marginTop: 20 }}
                  error={this.state.classifiersValidationError}
                  label="Classifiers"
                  onChange={this.onNewWaveOfContactClassifiersChange}
                  value={this.state.newWaveOfContactClassifiers} />
                <TextField
                  required
                  fullWidth={true}
                  style={{ marginTop: 20 }}
                  error={this.state.templateValidationError}
                  label="Template"
                  onChange={this.onNewWaveOfContactTemplateChange}
                  value={this.state.newWaveOfContactTemplate} />
                <TextField
                  label="Trigger Date"
                  type="datetime-local"
                  value={this.state.newWaveOfContactTriggerDate}
                  onChange={this.onNewWaveOfTriggerDateChange}
                  style={{ marginTop: 20 }}
                  InputLabelProps={{
                    shrink: true,
                  }} />
              </div>
              <div style={{ marginTop: 10 }}>
                <Button onClick={this.onCreateWaveOfContact} variant="contained" style={{ margin: 10 }}>
                  Create wave of contact
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