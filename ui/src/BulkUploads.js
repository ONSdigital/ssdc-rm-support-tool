import React, { Component } from "react";
import { Link } from "react-router-dom";
import {
  Button,
  CircularProgress,
  Dialog,
  DialogContent,
  DialogContentText,
  DialogTitle, LinearProgress,
  Paper, Snackbar, SnackbarContent,
  Typography
} from "@material-ui/core";
import Table from "@material-ui/core/Table";
import TableBody from "@material-ui/core/TableBody";
import TableCell from "@material-ui/core/TableCell";
import TableContainer from "@material-ui/core/TableContainer";
import TableHead from "@material-ui/core/TableHead";
import TableRow from "@material-ui/core/TableRow";
import { convertStatusText } from "./common";
import JobDetails from "./JobDetails";

class BulkUploads extends Component {
  state = {
    authorisedActivities: [],
    jobs: [],
  };

  componentDidMount() {
    this.getAuthorisedBackendData();
  }

  componentWillUnmount() {
    clearInterval(this.interval);
  }

  getAuthorisedBackendData = async () => {
    const authorisedActivities = await this.getAuthorisedActivities(); // Only need to do this once; don't refresh it repeatedly as it changes infrequently
    this.refreshDataFromBackend(authorisedActivities);

    this.interval = setInterval(
      () => this.refreshDataFromBackend(authorisedActivities),
      1000
    );
  };

  refreshDataFromBackend = async (authorisedActivities) => {
    if (!authorisedActivities.includes("LOAD_BULK_REFUSAL")) return;

    const response = await fetch(`/api/job?collectionExercise=${this.props.collectionExerciseId}&jobType=BULK_REFUSAL`);
    const jobs = await response.json();

    this.setState({ jobs: jobs });
  };

  getAuthorisedActivities = async () => {
    const authResponse = await fetch("/api/auth");

    // TODO: We need more elegant error handling throughout the whole application, but this will at least protect temporarily
    if (!authResponse.ok) {
      return;
    }

    const authorisedActivities = await authResponse.json();
    this.setState({
      authorisedActivities: authorisedActivities,
      isLoading: false,
    });

    return authorisedActivities;
  };

  // openDetailsDialog = async (messageHash) => {
  //   const response = await fetch(
  //     `/api/exceptionManager/badMessage/${messageHash}`
  //   );
  //   const exceptionDetails = await response.json();
  //
  //   this.setState({ exceptionDetails: exceptionDetails });
  // };

  // closeDetailsDialog = () => {
  //   this.setState({ exceptionDetails: null });
  // };

  // onPeek = () => {
  //   if (this.peekInProgress) {
  //     return;
  //   }
  //
  //   this.peekInProgress = true;
  //
  //   this.setState({ peekResult: "", showPeekDialog: true });
  //
  //   this.peekMessage(
  //     this.state.exceptionDetails[0].exceptionReport.messageHash
  //   );
  // };

  // peekMessage = async (messageHash) => {
  //   const response = await fetch(
  //     `/api/exceptionManager/peekMessage/${messageHash}`
  //   );
  //   const peekText = await response.text();
  //
  //   this.setState({ peekResult: peekText });
  //   this.peekInProgress = false; // Don't copy this pattern... go looking somewhere other than this file
  // };

  // closePeekDialog = () => {
  //   this.setState({ showPeekDialog: false });
  // };
  //
  // openQuarantineDialog = () => {
  //   this.setState({
  //     showQuarantineDialog: true,
  //   });
  // };
  //
  // closeQuarantineDialog = () => {
  //   this.setState({
  //     showQuarantineDialog: false,
  //   });
  // };
  //
  // quarantineMessage = async () => {
  //   if (this.quarantineInProgress) {
  //     return;
  //   }
  //
  //   this.quarantineInProgress = true;
  //
  //   const response = await fetch(
  //     `/api/exceptionManager/skipMessage/${this.state.exceptionDetails[0].exceptionReport.messageHash}`
  //   );
  //
  //   if (response.ok) {
  //     this.setState({
  //       exceptionDetails: null,
  //     });
  //     this.closeQuarantineDialog();
  //   }
  //
  //   this.quarantineInProgress = false; // Don't copy this pattern... go looking somewhere other than this file
  // };

  render() {
    // const activeExceptions = this.state.exceptions.filter(
    //   (exception) => !exception.quarantined
    // );
    //
    // var sortedExceptions = activeExceptions.sort((firstEx, secondEx) =>
    //   firstEx.firstSeen.localeCompare(secondEx.firstSeen)
    // );
    // sortedExceptions.reverse();

    const jobTableRows = this.state.jobs.map((job, index) => (
      <TableRow key={job.createdAt}>
        <TableCell component="th" scope="row">
          {job.fileName}
        </TableCell>
        <TableCell>{job.createdAt}</TableCell>
        <TableCell align="right">
          <Button
            onClick={() => this.handleOpenDetails(job)}   //TODO: Add this in
            variant="contained"
          >
            {convertStatusText(job.jobStatus)}{" "}
            {[
              "STAGING_IN_PROGRESS",
              "VALIDATION_IN_PROGRESS",
              "PROCESSING_IN_PROGRESS",
            ].includes(job.jobStatus) && (
              <CircularProgress size={15} style={{ marginLeft: 10 }} />
            )}
          </Button>
        </TableCell>
      </TableRow>
    ));

    return (
      <div style={{ marginTop: 20 }}>
        {this.state.authorisedActivities.includes(
          "LOAD_BULK_REFUSAL"
        ) && (
          <>
            <Typography variant="h6" color="inherit" style={{ marginTop: 20 }}>
              Uploaded Bulk Files
            </Typography>
            <TableContainer component={Paper}>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>File Name</TableCell>
                    <TableCell>Date Uploaded</TableCell>
                    <TableCell align="right">Status</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>{jobTableRows}</TableBody>
              </Table>
            </TableContainer>
          </>
        )}
        {/*{this.props.authorisedActivities.includes("LOAD_SAMPLE") && (*/}
        {/*  <>*/}
        {/*    <input*/}
        {/*      accept=".csv"*/}
        {/*      style={{ display: "none" }}*/}
        {/*      id="contained-button-file"*/}
        {/*      type="file"*/}
        {/*      onChange={(e) => {*/}
        {/*        this.handleUpload(e);*/}
        {/*      }}*/}
        {/*    />*/}
        {/*    <label htmlFor="contained-button-file">*/}
        {/*      <Button*/}
        {/*        variant="contained"*/}
        {/*        component="span"*/}
        {/*        style={{ marginTop: 10 }}*/}
        {/*      >*/}
        {/*        Upload Sample File*/}
        {/*      </Button>*/}
        {/*    </label>*/}
        {/*  </>*/}
        {/*)}*/}
        <Dialog open={this.state.uploadInProgress}>
          <DialogContent style={{ padding: 30 }}>
            <Typography variant="h6" color="inherit">
              Uploading file...
            </Typography>
            <LinearProgress
              variant="determinate"
              value={this.state.fileProgress * 100}
              style={{ marginTop: 20, marginBottom: 20, width: 400 }}
            />
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
            vertical: "bottom",
            horizontal: "left",
          }}
        >
          <SnackbarContent
            style={{ backgroundColor: "#4caf50" }}
            message={"File upload successful!"}
          />
        </Snackbar>
        <JobDetails
          jobTitle={"Sample"}
          // job={selectedJob}
          showDetails={this.state.showDetails}
          handleClosedDetails={this.handleClosedDetails}
          onClickAway={this.handleClosedDetails}
          onProcessJob={this.onProcessJob}
          onCancelJob={this.onCancelJob}
          authorisedActivities={this.props.authorisedActivities}
        ></JobDetails>
      </div>
    );
  }
}

export default BulkUploads;
