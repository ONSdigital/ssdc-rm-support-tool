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
import { errorAlert, getLocalDateTime } from "./Utils";

class SampleUpload extends Component {
  state = {
    jobs: [],
    fileProgress: 0, // Percentage of the file uploaded
    fileUploadSuccess: false, // Flag to flash the snackbar message on the screen, when file uploads successfully
    uploadInProgress: false, // Flag to display the file upload progress modal dialog
    showDetails: false, // Flag to display the job details dialog
    showCancelDialog: false,
  };

  closeCancelDialog = () => {
    this.setState({
      showCancelDialog: false,
    });
  };

  openCancelDialog = () => {
    this.setState({
      showCancelDialog: true,
    });
  };

  componentDidMount() {
    this.getJobs();

    // This is kept because the progress bars for file processing rely on it.
    // It has been put to every 10 seconds to cut it back a little
    this.interval = setInterval(() => this.getJobs(), 10000);
  }

  componentWillUnmount() {
    clearInterval(this.interval);
  }

  handleUpload = (e) => {
    if (e.target.files.length === 0) {
      return;
    }

    // This must be <= spring.servlet.multipart.max-file-size: x
    const max_file_size_in_mb = 100;

    // Comment here to explain why 1,000,000 and not 1024*1024.
    // Only dividing by 1,000,000 gives the size in mb that agrees with value on mac
    var file_size_in_mb = e.target.files[0].size / 1000000;

    if (file_size_in_mb > max_file_size_in_mb) {
      alert(
        "Maximum file size is " +
          max_file_size_in_mb +
          "MB. This file size is: " +
          file_size_in_mb +
          " MB",
      );
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
        // TODO: Temp hardcoded to SAMPLE
        jobData.append("jobType", "SAMPLE");

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
    // TODO: Job Type Temp hardcoded to SAMPLE
    const response = await fetch(
      `/api/job?collectionExercise=${this.props.collectionExerciseId}&jobType=SAMPLE`,
    );

    // TODO: We need more elegant error handling throughout the whole application, but this will at least protect temporarily
    const responseJson = await response.json();
    if (!response.ok) {
      errorAlert(responseJson);
      return;
    }

    this.setState({ jobs: responseJson });
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
    }).then(() => this.getJobs());
  };

  onCancelJob = () => {
    this.closeCancelDialog();
    fetch(`/api/job/${this.state.selectedJob}/cancel`, {
      method: "POST",
    });
  };

  render() {
    const selectedJob = this.state.jobs.find(
      (job) => job.id === this.state.selectedJob,
    );

    const jobTableRows = this.state.jobs.map((job, index) => (
      <TableRow key={index}>
        <TableCell component="th" scope="row">
          {job.fileName}
        </TableCell>
        <TableCell>{getLocalDateTime(job.createdAt)}</TableCell>
        <TableCell align="right">
          <Button
            id={"sampleStatus" + index}
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
      <div style={{ marginTop: 20 }}>
        {this.props.authorisedActivities.includes(
          "VIEW_SAMPLE_LOAD_PROGRESS",
        ) && (
          <>
            <Typography variant="h6" color="inherit" style={{ marginTop: 20 }}>
              Uploaded Sample Files
            </Typography>
            <TableContainer component={Paper}>
              <Table id="sampleFilesList">
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
        {this.props.authorisedActivities.includes("LOAD_SAMPLE") && (
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
                id="uploadSampleFileBtn"
                variant="contained"
                component="span"
                style={{ marginTop: 10 }}
              >
                Upload Sample File
              </Button>
            </label>
          </>
        )}
        <Dialog open={this.state.uploadInProgress}>
          <DialogContent style={{ padding: 30 }}>
            <Typography variant="h6" color="inherit">
              Uploading file. Do not close or refresh this tab.
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
          job={selectedJob}
          showDetails={this.state.showDetails}
          handleClosedDetails={this.handleClosedDetails}
          onClickAway={this.handleClosedDetails}
          onProcessJob={this.onProcessJob}
          showCancelDialog={this.state.showCancelDialog}
          openCancelDialog={this.openCancelDialog}
          closeCancelDialog={this.closeCancelDialog}
          onCancelJob={this.onCancelJob}
          authorisedActivities={this.props.authorisedActivities}
          loadPermission={"LOAD_SAMPLE"}
        ></JobDetails>
      </div>
    );
  }
}

export default SampleUpload;
