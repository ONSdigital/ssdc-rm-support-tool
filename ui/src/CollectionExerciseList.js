import React, { Component } from "react";
import "@fontsource/roboto";
import {
  Button,
  Dialog,
  DialogContent,
  Paper,
  TextField,
  Typography,
} from "@material-ui/core";
import Table from "@material-ui/core/Table";
import TableBody from "@material-ui/core/TableBody";
import TableCell from "@material-ui/core/TableCell";
import TableContainer from "@material-ui/core/TableContainer";
import TableHead from "@material-ui/core/TableHead";
import TableRow from "@material-ui/core/TableRow";
import {errorAlert, getAuthorisedActivities} from "./Utils";
import { Link } from "react-router-dom";

class CollectionExerciseList extends Component {
  state = {
    authorisedActivities: [],
    collectionExercises: [],
    createCollectionExerciseDialogDisplayed: false,
    newCollectionExerciseName: "",
    newCollectionExerciseNameError: false,
    newCollectionExerciseReference: "",
    newCollectionExerciseReferenceError: false,
    newCollectionExerciseStartDate: null,
    newCollectionExerciseEndDate: null,
    newCollectionExerciseDateError: "",
    newCollectionExerciseMetadata: "",
    newCollectionExerciseMetadataError: false,
    newCollectionExerciseCIRules: "",
    newCollectionExerciseCIRulesError: false,
  };

  componentDidMount() {
    this.getAuthorisedBackendData();
  }

  componentWillUnmount() {
    clearInterval(this.interval);
  }

  getAuthorisedBackendData = async () => {
    const authorisedActivities = await getAuthorisedActivities();
    this.setState({ authorisedActivities: authorisedActivities });
    this.refreshDataFromBackend(authorisedActivities);

    this.interval = setInterval(
      () => this.getCollectionExercises(authorisedActivities),
      1000
    );
  };

  refreshDataFromBackend = async (authorisedActivities) => {
    this.getCollectionExercises(authorisedActivities);
  };

  getCollectionExercises = async (authorisedActivities) => {
    if (!authorisedActivities.includes("LIST_COLLECTION_EXERCISES")) return;

    const response = await fetch(
      `/api/collectionExercises/?surveyId=${this.props.surveyId}`
    );
    const collexJson = await response.json();

    this.setState({
      collectionExercises: collexJson,
    });
  };

  openCreateCollectionExerciseDialog = () => {
    this.createCollectionExerciseInProgress = false;

    this.setState({
      newCollectionExerciseName: "",
      newCollectionExerciseReference: "",
      newCollectionExerciseStartDate: this.getTimeNowForDateTimePicker(),
      newCollectionExerciseEndDate: this.getTimeNowForDateTimePicker(),
      newCollectionExerciseMetadata: "",
      newCollectionExerciseNameError: false,
      newCollectionExerciseReferenceError: false,
      createCollectionExerciseDialogDisplayed: true,
      newCollectionExerciseMetadataError: false,
      newCollectionExerciseDateError: "",
      newCollectionExerciseCIRules: "",
      newCollectionExerciseCIRulesError: false,
    });
  };

  getTimeNowForDateTimePicker = () => {
    var dateNow = new Date();
    dateNow.setMinutes(dateNow.getMinutes() - dateNow.getTimezoneOffset());
    return dateNow.toJSON().slice(0, 16);
  };

  closeCreateCollectionExerciseDialog = () => {
    this.setState({ createCollectionExerciseDialogDisplayed: false });
  };

  onNewCollectionExerciseNameChange = (event) => {
    const resetValidation = !event.target.value.trim();
    this.setState({
      newCollectionExerciseName: event.target.value,
      newCollectionExerciseNameError: resetValidation,
    });
  };

  onNewCollectionExerciseReferenceChange = (event) => {
    const resetValidation = !event.target.value.trim();
    this.setState({
      newCollectionExerciseReference: event.target.value,
      newCollectionExerciseReferenceError: resetValidation,
    });
  };

  onNewCollectionExerciseStartDateChange = (event) => {
    this.setState({
      newCollectionExerciseStartDate: event.target.value,
      newCollectionExerciseDateError: "",
    });
  };

  onNewCollectionExerciseEndDateChange = (event) => {
    this.setState({
      newCollectionExerciseEndDate: event.target.value,
      newCollectionExerciseDateError: "",
    });
  };

  onNewCollectionExerciseMetadataChange = (event) => {
    this.setState({
      newCollectionExerciseMetadata: event.target.value,
      newCollectionExerciseMetadataError: false,
    });
  };

  onNewCollectionExerciseCIRulesChange = (event) => {
    this.setState({
      newCollectionExerciseCIRules: event.target.value,
      newCollectionExerciseCIRulesError: false,
    });
  };

  onCreateCollectionExercise = async () => {
    if (this.createCollectionExerciseInProgress) {
      return;
    }

    this.createCollectionExerciseInProgress = true;

    let validationFailed = false;

    if (!this.state.newCollectionExerciseName.trim()) {
      this.setState({ newCollectionExerciseNameError: true });
      validationFailed = true;
    }

    if (!this.state.newCollectionExerciseReference.trim()) {
      this.setState({ newCollectionExerciseReferenceError: true });
      validationFailed = true;
    }

    if (
      this.state.newCollectionExerciseEndDate <
      this.state.newCollectionExerciseStartDate
    ) {
      this.setState({
        newCollectionExerciseDateError: "Start date must be before end date",
      });
      validationFailed = true;
    }

    let metadataJson = null;
    if (this.state.newCollectionExerciseMetadata.length > 0) {
      try {
        metadataJson = JSON.parse(this.state.newCollectionExerciseMetadata);
        if (Object.keys(metadataJson).length === 0) {
          this.setState({ newCollectionExerciseMetadataError: true });
          validationFailed = true;
        }
      } catch (err) {
        this.setState({ newCollectionExerciseMetadataError: true });
        validationFailed = true;
      }
    }

    let ciRulesJson = null;
    if (this.state.newCollectionExerciseCIRules.length > 0) {
      try {
        ciRulesJson = JSON.parse(this.state.newCollectionExerciseCIRules);
        if (Object.keys(ciRulesJson).length === 0) {
          this.setState({ newCollectionExerciseCIRulesError: true });
          validationFailed = true;
        }
      } catch (err) {
        this.setState({ newCollectionExerciseCIRulesError: true });
        validationFailed = true;
      }
    } else {
      this.setState({ newCollectionExerciseCIRulesError: true });
      validationFailed = true;
    }

    if (validationFailed) {
      this.createCollectionExerciseInProgress = false;
      return;
    }

    const newCollectionExercise = {
      name: this.state.newCollectionExerciseName,
      surveyId: this.props.surveyId,
      reference: this.state.newCollectionExerciseReference,
      startDate: new Date(
        this.state.newCollectionExerciseStartDate
      ).toISOString(),
      endDate: new Date(this.state.newCollectionExerciseEndDate).toISOString(),
      metadata: metadataJson,
      collectionInstrumentSelectionRules: ciRulesJson,
    };

    const response = await fetch("/api/collectionExercises", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(newCollectionExercise),
    });

    if (response.ok) {
      this.setState({ createCollectionExerciseDialogDisplayed: false });
    } else {
      this.createCollectionExerciseInProgress = false;
      const responseJson = await response.json();
      errorAlert(responseJson)
    }
  };

  render() {
    const collectionExerciseTableRows = this.state.collectionExercises.map(
      (collex) => (
        <TableRow key={collex.name}>
          <TableCell component="th" scope="row">
            <Link
              to={`/collex?surveyId=${this.props.surveyId}&collexId=${collex.id}`}
            >
              {collex.name}
            </Link>
          </TableCell>
          <TableCell component="th" scope="row">
            {collex.reference}
          </TableCell>
          <TableCell component="th" scope="row">
            {collex.startDate}
          </TableCell>
          <TableCell component="th" scope="row">
            {collex.endDate}
          </TableCell>
          <TableCell component="th" scope="row">
            {JSON.stringify(collex.metadata)}
          </TableCell>
        </TableRow>
      )
    );

    return (
      <>
        {this.state.authorisedActivities.includes(
          "LIST_COLLECTION_EXERCISES"
        ) && (
          <>
            <Typography variant="h6" color="inherit" style={{ marginTop: 10 }}>
              Collection Exercises
            </Typography>
            <TableContainer component={Paper}>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>Collection Exercise Name</TableCell>
                    <TableCell>Reference</TableCell>
                    <TableCell>Start Date</TableCell>
                    <TableCell>End Date</TableCell>
                    <TableCell>Metadata</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>{collectionExerciseTableRows}</TableBody>
              </Table>
            </TableContainer>
          </>
        )}
        {this.state.authorisedActivities.includes(
          "CREATE_COLLECTION_EXERCISE"
        ) && (
          <Button
            variant="contained"
            onClick={this.openCreateCollectionExerciseDialog}
            style={{ marginTop: 10 }}
          >
            Create Collection Exercise
          </Button>
        )}
        <Dialog open={this.state.createCollectionExerciseDialogDisplayed}>
          <DialogContent style={{ padding: 30 }}>
            <div>
              <div>
                <TextField
                  required
                  fullWidth={true}
                  error={this.state.newCollectionExerciseNameError}
                  label="Collection exercise name"
                  onChange={this.onNewCollectionExerciseNameChange}
                  value={this.state.newCollectionExerciseName}
                />
                <TextField
                  required
                  fullWidth={true}
                  error={this.state.newCollectionExerciseReferenceError}
                  label="Reference"
                  onChange={this.onNewCollectionExerciseReferenceChange}
                  value={this.state.newCollectionExerciseReference}
                />
                <TextField
                  required
                  label="Start Date"
                  type="datetime-local"
                  error={this.state.newCollectionExerciseDateError}
                  helperText={this.state.newCollectionExerciseDateError}
                  value={this.state.newCollectionExerciseStartDate}
                  onChange={this.onNewCollectionExerciseStartDateChange}
                  style={{ marginTop: 20 }}
                  InputLabelProps={{
                    shrink: true,
                  }}
                />
                <TextField
                  required
                  label="End Date"
                  type="datetime-local"
                  error={this.state.newCollectionExerciseDateError}
                  value={this.state.newCollectionExerciseEndDate}
                  onChange={this.onNewCollectionExerciseEndDateChange}
                  style={{ marginTop: 20, marginLeft: 30 }}
                  InputLabelProps={{
                    shrink: true,
                  }}
                />
                <TextField
                  style={{ marginTop: 10 }}
                  multiline
                  fullWidth={true}
                  error={this.state.newCollectionExerciseMetadataError}
                  label="Metadata"
                  onChange={this.onNewCollectionExerciseMetadataChange}
                  value={this.state.newCollectionExerciseMetadata}
                />
                <TextField
                  style={{ marginTop: 10 }}
                  multiline
                  required
                  fullWidth={true}
                  error={this.state.newCollectionExerciseCIRulesError}
                  label="CI Rules"
                  onChange={this.onNewCollectionExerciseCIRulesChange}
                  value={this.state.newCollectionExerciseCIRules}
                />
              </div>
              <div style={{ marginTop: 10 }}>
                <Button
                  onClick={this.onCreateCollectionExercise}
                  variant="contained"
                  style={{ margin: 10 }}
                >
                  Create collection exercise
                </Button>
                <Button
                  onClick={this.closeCreateCollectionExerciseDialog}
                  variant="contained"
                  style={{ margin: 10 }}
                >
                  Cancel
                </Button>
              </div>
            </div>
          </DialogContent>
        </Dialog>
      </>
    );
  }
}

export default CollectionExerciseList;
