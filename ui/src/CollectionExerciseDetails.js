import React, { Component } from 'react';
import '@fontsource/roboto';
import { Button, Dialog, DialogContent, TextField, Paper, Select, MenuItem, FormControl, InputLabel } from '@material-ui/core';
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
    validationError: false,
    newWaveOfContactPrintSupplier: ''
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
      validationError: false,
      createWaveOfContactsDialogDisplayed: true
    })
  }

  closeDialog = () => {
    this.setState({ createWaveOfContactsDialogDisplayed: false })
  }

  onNewWaveOfContactPrintSupplierChange = (event) => {
    const resetValidation = !event.target.value.trim()
    this.setState({
      validationError: resetValidation,
      newWaveOfContactPrintSupplier: event.target.value
    })
  }

  onCreateWaveOfContact = async () => {
    if (!this.state.newWaveOfContactPrintSupplier.trim()) {
      this.setState({ validationError: true })
      return
    }

    const newWaveOfContact = {
      id: uuidv4(),
      type: 'PRINT',
      triggerDateTime: '2021-01-01T00:00:00.000Z',
      hasTriggered: false,
      classifiers: '1=2',
      template: ['__caseref__', '__uac__', '__qid__'],
      packCode: 'DUMMY_PACK',
      printSupplier: this.state.newWaveOfContactPrintSupplier,
      collectionExercise: 'collectionExercises/' + this.props.collectionExerciseId
    }

    const response = await fetch('/waveOfContacts', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(newWaveOfContact)
    })

    this.setState({ createWaveOfContactsDialogDisplayed: false })
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
                  <Select
                    value={'PRINT'}
                  >
                    <MenuItem value={'PRINT'}>PRINT</MenuItem>
                    <MenuItem value={'FACE_TO_FACE'}>FACE-TO-FACE</MenuItem>
                    <MenuItem value={'OUTBOUND_PHONE'}>OUTBOUND PHONE</MenuItem>
                  </Select>
                </FormControl>
                <TextField
                  required
                  fullWidth={true}
                  error={this.state.validationError}
                  id="standard-required"
                  label="Print Supplier"
                  onChange={this.onNewWaveOfContactPrintSupplierChange}
                  value={this.state.newWaveOfContactPrintSupplier} />
                <TextField
                  id="datetime-local"
                  label="Trigger date"
                  type="datetime-local"
                  defaultValue={new Date()}
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