import React, { Component } from 'react';
import '@fontsource/roboto';
import { Button, Dialog, DialogContent, TextField, Paper, Select, MenuItem, FormControl, InputLabel, Typography } from '@material-ui/core';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableContainer from '@material-ui/core/TableContainer';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import { uuidv4 } from './common'

class CollectionExerciseDetails extends Component {
  state = {
    waveOfContacts: [],
    createWaveOfContactsDialogDisplayed: false,
    printSupplierValidationError: false,
    packCodeValidationError: false,
    newWaveOfContactPrintSupplier: '',
    newWaveOfContactPackCode: ''
  }

  componentDidMount() {
    this.getWaveOfContacts()

    this.interval = setInterval(
      () => this.getWaveOfContacts(),
      1000
    )
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

    if (failedValidation) {
      return
    }

    const newWaveOfContact = {
      id: uuidv4(),
      type: 'PRINT',
      triggerDateTime: new Date(this.state.newWaveOfContactTriggerDate).toISOString(),
      hasTriggered: false,
      classifiers: '1=2',
      template: ['__caseref__', '__uac__', '__qid__'],
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
          {woc.classifiers}
        </TableCell>
        <TableCell component="th" scope="row">
          {woc.template}
        </TableCell>
        <TableCell component="th" scope="row">
          {woc.printSupplier}
        </TableCell>
        <TableCell component="th" scope="row">
          {woc.packCode}
        </TableCell>
      </TableRow>
    ))

    return (
      <div style={{ padding: 20 }}>
        <Typography variant="h4" color="inherit" style={{ marginBottom: 20 }}>
          Collection Exercise: {this.props.collectionExerciseName}
        </Typography>
        <Button variant="contained" onClick={this.openDialog}>Create Wave of Contact</Button>
        <TableContainer component={Paper} style={{ marginTop: 20 }}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Type</TableCell>
                <TableCell>Trigger date</TableCell>
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
                <TextField
                  required
                  fullWidth={true}
                  style={{ marginTop: 20 }}
                  error={this.state.printSupplierValidationError}
                  label="Print Supplier"
                  onChange={this.onNewWaveOfContactPrintSupplierChange}
                  value={this.state.newWaveOfContactPrintSupplier} />
                <TextField
                  required
                  fullWidth={true}
                  style={{ marginTop: 20 }}
                  error={this.state.packCodeValidationError}
                  label="Pack Code"
                  onChange={this.onNewWaveOfContactPackCodeChange}
                  value={this.state.newWaveOfContactPackCode} />
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