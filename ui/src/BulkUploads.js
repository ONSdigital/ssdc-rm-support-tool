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
import { Link } from "react-router-dom";

class BulkUploads extends Component {
  state = {
    bulkRefusalJobs: [],
    authorisedActivities: [],
    fileProgress: 0, // Percentage of the file uploaded
    fileUploadSuccess: false, // Flag to flash the snackbar message on the screen, when file uploads successfully
    uploadInProgress: false, // Flag to display the file upload progress modal dialog
    showDetails: false, // Flag to display the job details dialog
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
    this.refreshBulkRefusalsFromBackend(authorisedActivities);
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

  refreshBulkRefusalsFromBackend = async (authorisedActivities) => {
    if (!authorisedActivities.includes("VIEW_BULK_REFUSAL_PROGRESS")) return;

    const response = await fetch(
      `/api/job?collectionExercise=${this.props.collectionExerciseId}&jobType=BULK_REFUSAL`
    );
    const bulkRefusalJobs = await response.json();

    this.setState({ bulkRefusalJobs: bulkRefusalJobs });
  };

  handleBulkFileUpload = (e, job_type) => {
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
        jobData.append("jobType", job_type);

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
        this.refreshDataFromBackend(this.state.authorisedActivities);
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

  handleOpenDetails = (job, jobType) => {
    this.setState({
      showDetails: true,
      selectedJob: job.id,
      selectedJobType: jobType,
    });
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
    var selectedJob;
    var detailsDialogTitle;
    var loadPermission;
    switch (this.state.selectedJobType) {
      case "BULK_REFUSAL":
        selectedJob = this.state.bulkRefusalJobs.find(
          (job) => job.id === this.state.selectedJob
        );
        detailsDialogTitle = "Bulk Refusal Detail";
        loadPermission = "LOAD_BULK_REFUSAL";
        break;
      default:
    }

    return (
      <div style={{ padding: 20 }}>
        <Link
          to={`/collex?surveyId=${this.props.surveyId}&collexId=${this.props.collectionExerciseId}`}
        >
          ← Back to collection exercise details
        </Link>
        <Typography variant="h4" color="inherit">
          Uploaded Bulk Process Files
        </Typography>
        {this.buildBulkProcessTable(
          this.state.bulkRefusalJobs,
          "BULK_REFUSAL",
          "Bulk Refusals",
          "LOAD_BULK_REFUSAL",
          "VIEW_BULK_REFUSAL_PROGRESS"
        )}
        <JobDetails
          jobTitle={detailsDialogTitle}
          job={selectedJob}
          showDetails={this.state.showDetails}
          handleClosedDetails={this.handleClosedDetails}
          onClickAway={this.handleClosedDetails}
          onProcessJob={this.onProcessJob}
          onCancelJob={this.onCancelJob}
          authorisedActivities={this.state.authorisedActivities}
          loadPermission={loadPermission}
        ></JobDetails>
      </div>
    );
  }

  buildBulkProcessTable(
    bulkJobs,
    jobType,
    jobTitle,
    loadPermission,
    viewerPermission
  ) {
    const selectedJob = bulkJobs.find(
      (job) => job.id === this.state.selectedJob
    );

    const bulkJobTableRows = bulkJobs.map((job, index) => (
      <TableRow key={job.createdAt}>
        <TableCell component="th" scope="row">
          {job.fileName}
        </TableCell>
        <TableCell>{job.createdAt}</TableCell>
        <TableCell align="right">
          <Button
            onClick={() => this.handleOpenDetails(job, jobType)}
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
        {this.state.authorisedActivities.includes(viewerPermission) && (
          <>
            <Typography variant="h6" color="inherit" style={{ marginTop: 20 }}>
              {jobTitle}
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
                <TableBody>{bulkJobTableRows}</TableBody>
              </Table>
            </TableContainer>
          </>
        )}
        {this.state.authorisedActivities.includes(loadPermission) && (
          <>
            <input
              accept=".csv"
              style={{ display: "none" }}
              id="contained-button-file"
              type="file"
              onChange={(e) => {
                this.handleBulkFileUpload(e, jobType);
              }}
            />
            <label htmlFor="contained-button-file">
              <Button
                variant="contained"
                component="span"
                style={{ marginTop: 10 }}
              >
                Upload {jobTitle} File
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
      </div>
    );
  }
}

export default BulkUploads;
