import React, { Component } from 'react';
import '@fontsource/roboto';
import { Button, Dialog, DialogContent, TextField, Paper, Typography } from '@material-ui/core';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableContainer from '@material-ui/core/TableContainer';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import { uuidv4 } from './common'

class SurveyDetails extends Component {
  state = {
    collectionExercises: [],
    createCollectionExerciseDialogDisplayed: false,
    validationError: false,
    newCollectionExerciseName: ''
  }

  componentDidMount() {
    this.getCollectionExercises()

    this.interval = setInterval(
      () => this.getCollectionExercises(),
      1000
    )
  }

  componentWillUnmount() {
    clearInterval(this.interval)
  }

  getCollectionExercises = async () => {
    const response = await fetch('/surveys/' + this.props.surveyId + '/collectionExercises')
    const collex_json = await response.json()

    this.setState({ collectionExercises: collex_json._embedded.collectionExercises })
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

    this.setState({ createCollectionExerciseDialogDisplayed: false })
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

    return (
      <div style={{ padding: 20 }}>
        <Typography variant="h4" color="inherit" style={{marginBottom: 20}}>
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
      </div>
    )
  }
}

export default SurveyDetails