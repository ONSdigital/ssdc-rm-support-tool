import React, { Component } from "react";
import axios from "axios";
import { convertStatusText } from "./common";
import TableRow from "@material-ui/core/TableRow";
import TableCell from "@material-ui/core/TableCell";
import {
  Button,
  CircularProgress,
  Dialog,
  DialogContent,
  LinearProgress,
  Paper,
  Snackbar,
  SnackbarContent,
  Typography,
} from "@material-ui/core";
import TableContainer from "@material-ui/core/TableContainer";
import Table from "@material-ui/core/Table";
import TableHead from "@material-ui/core/TableHead";
import TableBody from "@material-ui/core/TableBody";
import JobDetails from "./JobDetails";

class BulkUploads extends Component {
  state = {
    jobs: [],
    authorisedActivities: [],
    fileProgress: 0, // Percentage of the file uploaded
    fileUploadSuccess: false, // Flag to flash the snackbar message on the screen, when file uploads successfully
    uploadInProgress: false, // Flag to display the file upload progress modal dialog
    showDetails: false, // Flag to display the job details dialog
  };

  componentDidMount() {
    this.getAuthorisedBackendData();
    this.getJobs();

    this.interval = setInterval(() => this.getJobs(), 1000);
  }

  componentWillUnmount() {
    clearInterval(this.interval);
  }

  getAuthorisedBackendData = async () => {
    const authorisedActivities = await this.getAuthorisedActivities(); // Only need to do this once; don't refresh it repeatedly as it changes infrequently

  };

  getAuthorisedActivities = async () => {
    const response = await fetch(`/api/auth?surveyId=${this.props.surveyId}`);

    // TODO: We need more elegant error handling throughout the whole application, but this will at least protect temporarily
    if (!response.ok) {
      return;
    }

    const authJson = await response.json();

    this.setState({ authorisedActivities: authJson });

    return authJson;
  };

  handleUpload = (e) => {
    if (e.target.files.length === 0) {
      return;
    }

    // Display the progress modal dialog
    this.setState({
      uploadInProgress: true,
    });

    const formData = new FormData();
    formData.append("file", e.target.files[0]);

    const fileName = e.target.files[0].name;
    // Reset the file
    e.target.value = null;

    // Send the file data to the backend
    axios
        .request({
          method: "post",
          url: "/api/upload",
          data: formData,
          headers: {
            "Content-Type": "multipart/form-data",
          },
          onUploadProgress: (p) => {
            console.log(p);

            // Update file upload progress
            this.setState({
              fileProgress: p.loaded / p.total,
            });
          },
        })
        .then((data) => {
          // send the job details
          const fileId = data.data;
          const jobData = new FormData();
          jobData.append("fileId", fileId);
          jobData.append("fileName", fileName);
          jobData.append("collectionExerciseId", this.props.collectionExerciseId);
          // TODO: Temp hardcoded to BULK_REFUSAL
          jobData.append("jobType", "BULK_REFUSAL");

          const response = fetch(`/api/job`, {
            method: "POST",
            body: jobData,
          });

          if (!response.ok) {
            // TODO - nice error handling
            // If we check it, it's is currently buggy and leaves the popup on the screen for unknown reasons - need to raise a defect
          }

          // Hide the progress dialog and flash the snackbar message
          this.setState({
            fileProgress: 1.0,
            fileUploadSuccess: true,
            uploadInProgress: false,
          });

          this.getJobs();
        });
  };

  handleClose = (event, reason) => {
    // Ignore clickaways so that the dialog is modal
    if (reason === "clickaway") {
      return;
    }

    this.setState({
      fileUploadSuccess: false,
    });
  };

  getJobs = async () => {
    // TODO: Job Type Temp hardcoded to BULD_REFUSAL
    const response = await fetch(
        `/api/job?collectionExercise=${this.props.collectionExerciseId}&jobType=BULK_REFUSAL`
    );

    // TODO: We need more elegant error handling throughout the whole application, but this will at least protect temporarily
    if (!response.ok) {
      return;
    }

    const jobsJson = await response.json();

    this.setState({ jobs: jobsJson });
  };

  handleOpenDetails = (job) => {
    this.setState({ showDetails: true, selectedJob: job.id });
  };

  handleClosedDetails = () => {
    this.setState({ showDetails: false });
  };

  onProcessJob = () => {
    fetch(`/api/job/${this.state.selectedJob}/process`, {
      method: "POST",
    });
  };

  onCancelJob = () => {
    fetch(`/api/job/${this.state.selectedJob}/cancel`, {
      method: "POST",
    });
  };

  render() {
    const selectedJob = this.state.jobs.find(
        (job) => job.id === this.state.selectedJob
    );

    const jobTableRows = this.state.jobs.map((job, index) => (
        <TableRow key={job.createdAt}>
          <TableCell component="th" scope="row">
            {job.fileName}
          </TableCell>
          <TableCell>{job.createdAt}</TableCell>
          <TableCell>{job.jobType}</TableCell>
          <TableCell align="right">
            <Button
                onClick={() => this.handleOpenDetails(job)}
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
        <div style={{ padding: 20 }}>
          {this.state.authorisedActivities.includes(
              "VIEW_BULK_REFUSAL_PROGRESS"
          ) && (
              <>
                <Typography variant="h4" color="inherit" style={{ marginTop: 20 }}>
                  Uploaded Bulk Process Files
                </Typography>
                <TableContainer component={Paper}>
                  <Table>
                    <TableHead>
                      <TableRow>
                        <TableCell>File Name</TableCell>
                        <TableCell>Date Uploaded</TableCell>
                        <TableCell>Job Type</TableCell>
                        <TableCell align="right">Status</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>{jobTableRows}</TableBody>
                  </Table>
                </TableContainer>
              </>
          )}
          {this.state.authorisedActivities.includes("LOAD_BULK_REFUSAL") && (
              <>
                <input
                    accept=".csv"
                    style={{ display: "none" }}
                    id="contained-button-file"
                    type="file"
                    onChange={(e) => {
                      this.handleUpload(e);
                    }}
                />
                <label htmlFor="contained-button-file">
                  <Button
                      variant="contained"
                      component="span"
                      style={{ marginTop: 10 }}
                  >
                    Upload Bulk Processing File
                  </Button>
                </label>
              </>
          )}
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
              jobTitle={"Bulk Refusal"}
              job={selectedJob}
              showDetails={this.state.showDetails}
              handleClosedDetails={this.handleClosedDetails}
              onClickAway={this.handleClosedDetails}
              onProcessJob={this.onProcessJob}
              onCancelJob={this.onCancelJob}
              authorisedActivities={this.state.authorisedActivities}
          ></JobDetails>
        </div>
    );
  }
}

export default BulkUploads;
