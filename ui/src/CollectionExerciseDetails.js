import React, { Component } from 'react';
import '@fontsource/roboto';
import { Button, Dialog, DialogContent, TextField, Paper, Select, MenuItem, FormControl, InputLabel, Typography, LinearProgress, Snackbar, SnackbarContent, CircularProgress } from '@material-ui/core';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableContainer from '@material-ui/core/TableContainer';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import { uuidv4, convertStatusText } from './common'
import axios from 'axios';
import JobDetails from './JobDetails';

class CollectionExerciseDetails extends Component {
  state = {
    waveOfContacts: [],
    createWaveOfContactsDialogDisplayed: false,
    printSupplierValidationError: false,
    packCodeValidationError: false,
    packCodeValidationError: false,
    classifiersValidationError: false,
    templateValidationError: false,
    newWaveOfContactPrintSupplier: '',
    newWaveOfContactPackCode: '',
    newWaveOfContactClassifiers: '',
    newWaveOfContactTemplate: '',
    jobs: [],
    fileProgress: 0,  // Percentage of the file uploaded
    fileUploadSuccess: false, // Flag to flash the snackbar message on the screen, when file uploads successfully
    uploadInProgress: false, // Flag to display the file upload progress modal dialog
    showDetails: false // Flag to display the job details dialog
  }

  componentDidMount() {
    this.getStuffFromBackend()

    this.interval = setInterval(
      () => this.getStuffFromBackend(),
      1000
    )
  }

  getStuffFromBackend() {
    this.getJobs()
    this.getWaveOfContacts()
  }

  componentWillUnmount() {
    clearInterval(this.interval)
  }

  handleUpload = (e) => {
    if (e.target.files.length === 0) {
      return
    }

    // Display the progress modal dialog
    this.setState({
      uploadInProgress: true,
    })

    const formData = new FormData();
    formData.append("file", e.target.files[0]);
    formData.append("bulkProcess", 'SAMPLE')
    formData.append("collectionExerciseId", this.props.collectionExerciseId)

    // Reset the file
    e.target.value = null;

    // Send the file data to the backend
    axios.request({
      method: "post",
      url: "/upload",
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      },
      onUploadProgress: (p) => {
        console.log(p);

        // Update file upload progress
        this.setState({
          fileProgress: p.loaded / p.total
        })
      }

    }).then(data => {
      // Hide the progress dialog and flash the snackbar message
      this.setState({
        fileProgress: 1.0,
        fileUploadSuccess: true,
        uploadInProgress: false,
      })

      this.getJobs()
    })
  }

  handleClose = (event, reason) => {
    // Ignore clickaways so that the dialog is modal
    if (reason === 'clickaway') {
      return;
    }

    this.setState({
      fileUploadSuccess: false,
    })
  }

  getJobs = async () => {
    const response = await fetch('/collectionExercises/' + this.props.collectionExerciseId + '/jobs')
    const jobs_json = await response.json()

    this.setState({ jobs: jobs_json._embedded.jobs })
  }

  getWaveOfContacts = async () => {
    const response = await fetch('/collectionExercises/' + this.props.collectionExerciseId + '/waveOfContacts')
    const woc_json = await response.json()

    this.setState({ waveOfContacts: woc_json._embedded.waveOfContacts })
  }

  handleOpenDetails = (job) => {
    this.setState({ showDetails: true, selectedJob: job.id })
  }

  handleClosedDetails = () => {
    this.setState({ showDetails: false })
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
        JSON.parse(this.state.newWaveOfContactTemplate)
      } catch(err) {
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
    const selectedJob = this.state.jobs.find(job => job.id === this.state.selectedJob)

    const jobTableRows = this.state.jobs.map((job, index) => (
      <TableRow key={job.createdAt}>
        <TableCell component="th" scope="row">
          {job.fileName}
        </TableCell>
        <TableCell>{job.createdAt}</TableCell>
        <TableCell align="right">
          <Button
            onClick={() => this.handleOpenDetails(job)}
            variant="contained">
            {convertStatusText(job.jobStatus)} {!job.jobStatus.startsWith('PROCESSED') && <CircularProgress size={15} style={{ marginLeft: 10 }} />}
          </Button>
        </TableCell>
      </TableRow>
    ))

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
        <div style={{ marginTop: 20 }}>
          <input
            accept=".csv"
            style={{ display: 'none' }}
            id="contained-button-file"
            type="file"
            onChange={(e) => {
              this.handleUpload(e)
            }}
          />
          <label htmlFor="contained-button-file">
            <Button variant="contained" component="span">
              Upload Sample File
              </Button>
          </label>
          <TableContainer component={Paper} style={{ marginTop: 20 }}>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>File Name</TableCell>
                  <TableCell>Date Uploaded</TableCell>
                  <TableCell align="right">Status</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {jobTableRows}
              </TableBody>
            </Table>
          </TableContainer>
          <Dialog open={this.state.uploadInProgress}>
            <DialogContent style={{ padding: 30 }}>
              <Typography variant="h6" color="inherit">
                Uploading file...
            </Typography>
              <LinearProgress
                variant="determinate"
                value={this.state.fileProgress * 100}
                style={{ marginTop: 20, marginBottom: 20, width: 400 }} />
              <Typography variant="h6" color="inherit">
                {Math.round(this.state.fileProgress * 100)}%
          </Typography>
            </DialogContent>
          </Dialog>
          <Snackbar
            open={this.state.fileUploadSuccess}
            autoHideDuration={6000}
            onClose={this.handleClose}
            anchorOrigin={{
              vertical: 'bottom',
              horizontal: 'left',
            }}>
            <SnackbarContent style={{ backgroundColor: '#4caf50' }}
              message={'File upload successful!'}
            />
          </Snackbar>
          <JobDetails jobTitle={'Sample'} job={selectedJob} showDetails={this.state.showDetails} handleClosedDetails={this.handleClosedDetails} onClickAway={this.handleClosedDetails}>
          </JobDetails>
        </div>
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