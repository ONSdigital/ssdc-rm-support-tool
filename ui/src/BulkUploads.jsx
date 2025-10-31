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
import { errorAlert, getLocalDateTime } from "./Utils";

const BULK_REFUSAL_JOB_TYPE = "BULK_REFUSAL";
const BULK_REFUSAL_VIEW_PERMISSION = "VIEW_BULK_REFUSAL_PROGRESS";
const BULK_REFUSAL_LOAD_PERMISSION = "LOAD_BULK_REFUSAL";

const BULK_INVALID_JOB_TYPE = "BULK_INVALID";
const BULK_INVALID_VIEW_PERMISSION = "VIEW_BULK_INVALID_PROGRESS";
const BULK_INVALID_LOAD_PERMISSION = "LOAD_BULK_INVALID";

const BULK_UPDATE_SAMPLE_JOB_TYPE = "BULK_UPDATE_SAMPLE";
const BULK_UPDATE_SAMPLE_VIEW_PERMISSION = "VIEW_BULK_UPDATE_SAMPLE_PROGRESS";
const BULK_UPDATE_SAMPLE_LOAD_PERMISSION = "LOAD_BULK_UPDATE_SAMPLE";

const BULK_UPDATE_SAMPLE_SENSITIVE = "BULK_UPDATE_SAMPLE_SENSITIVE";
const BULK_UPDATE_SAMPLE_SENSITIVE_VIEW_PERMISSION =
  "VIEW_BULK_UPDATE_SAMPLE_SENSITIVE_PROGRESS";
const BULK_UPDATE_SAMPLE_SENSITIVE_LOAD_PERMISSION =
  "LOAD_BULK_UPDATE_SAMPLE_SENSITIVE";

class BulkUploads extends Component {
  state = {
    authorisedActivities: [],
    fileProgress: 0, // Percentage of the file uploaded
    fileUploadSuccess: false, // Flag to flash the snackbar message on the screen, when file uploads successfully
    uploadInProgress: false, // Flag to display the file upload progress modal dialog
    showDetails: false, // Flag to display the job details dialog
    bulkRefusalJobs: [],
    bulkInvalidJobs: [],
    bulkUpdateSampleJobs: [],
    bulkUpdateSampleSensitiveJobs: [],
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
    this.getAuthorisedBackendData();
  }

  getAuthorisedBackendData = async () => {
    const authorisedActivities = await this.getAuthorisedActivities(); // Only need to do this once; don't refresh it repeatedly as it changes infrequently
    this.refreshDataFromBackend(authorisedActivities);

    // Left in but increased to refresh upload status button
    this.interval = setInterval(
      () => this.refreshDataFromBackend(authorisedActivities),
      10000,
    );
  };

  refreshDataFromBackend = async (authorisedActivities) => {
    this.refreshBulkJobsFromBackend(
      authorisedActivities,
      BULK_REFUSAL_JOB_TYPE,
      BULK_REFUSAL_VIEW_PERMISSION,
      "bulkRefusalJobs",
    );
    this.refreshBulkJobsFromBackend(
      authorisedActivities,
      BULK_INVALID_JOB_TYPE,
      BULK_INVALID_VIEW_PERMISSION,
      "bulkInvalidJobs",
    );
    this.refreshBulkJobsFromBackend(
      authorisedActivities,
      BULK_UPDATE_SAMPLE_JOB_TYPE,
      BULK_UPDATE_SAMPLE_VIEW_PERMISSION,
      "bulkUpdateSampleJobs",
    );
    this.refreshBulkJobsFromBackend(
      authorisedActivities,
      BULK_UPDATE_SAMPLE_SENSITIVE,
      BULK_UPDATE_SAMPLE_SENSITIVE_VIEW_PERMISSION,
      "bulkUpdateSampleSensitiveJobs",
    );
  };

  getAuthorisedActivities = async () => {
    const response = await fetch(`/api/auth?surveyId=${this.props.surveyId}`);

    // TODO: We need more elegant error handling throughout the whole application, but this will at least protect temporarily
    const responseJson = await response.json();
    if (!response.ok) {
      errorAlert(responseJson);
      return;
    }

    this.setState({ authorisedActivities: responseJson });

    return responseJson;
  };

  refreshBulkJobsFromBackend = async (
    authorisedActivities,
    jobType,
    viewPermission,
    bulkJobsKey,
  ) => {
    if (!authorisedActivities.includes(viewPermission)) return;

    const response = await fetch(
      `/api/job?collectionExercise=${this.props.collectionExerciseId}&jobType=${jobType}`,
    );
    const bulkJobs = await response.json();

    this.setState({ [bulkJobsKey]: bulkJobs });
  };

  handleBulkFileUpload = (event, jobType) => {
    if (event.target.files.length === 0) {
      return;
    }

    // This must be <= spring.servlet.multipart.max-file-size: x
    const max_file_size_in_mb = 100;

    // Comment here to explain why 1,000,000 and not 1024*1024.
    // Only dividing by 1,000,000 gives the size in mb that agrees with value on mac
    var file_size_in_mb = event.target.files[0].size / 1000000;

    if (file_size_in_mb > max_file_size_in_mb) {
      alert(
        "Maximum file size is " +
          max_file_size_in_mb +
          "MB. This file size is: " +
          file_size_in_mb +
          "MB.",
      );
      return;
    }

    // Display the progress modal dialog
    this.setState({
      uploadInProgress: true,
    });

    const formData = new FormData();
    formData.append("file", event.target.files[0]);

    const fileName = event.target.files[0].name;
    // Reset the file
    event.target.value = null;

    // Send the file data to the backend
    axios
      .request({
        method: "post",
        url: "/api/upload",
        data: formData,
        headers: {
          "Content-Type": "multipart/form-data",
        },
        onUploadProgress: (progress) => {
          console.log(progress);

          // Update file upload progress
          this.setState({
            fileProgress: progress.loaded / progress.total,
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
        jobData.append("jobType", jobType);

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
    this.closeCancelDialog();
    fetch(`/api/job/${this.state.selectedJob}/cancel`, {
      method: "POST",
    });
  };

  buildBulkProcessTable(
    bulkJobs,
    jobType,
    jobTitle,
    loadPermission,
    viewerPermission,
  ) {
    const bulkJobTableRows = bulkJobs.map((job, index) => (
      <TableRow key={index}>
        <TableCell component="th" scope="row">
          {job.fileName}
        </TableCell>
        <TableCell>{getLocalDateTime(job.createdAt)}</TableCell>
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
      <div>
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
              id={`contained-button-file-${jobType}`}
              type="file"
              onChange={(event) => {
                this.handleBulkFileUpload(event, jobType);
                this.refreshDataFromBackend(this.state.authorisedActivities);
              }}
            />
            <label htmlFor={`contained-button-file-${jobType}`}>
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
      </div>
    );
  }

  render() {
    let selectedJob;
    let detailsDialogTitle;
    let loadPermission;
    switch (this.state.selectedJobType) {
      case BULK_REFUSAL_JOB_TYPE:
        selectedJob = this.state.bulkRefusalJobs.find(
          (job) => job.id === this.state.selectedJob,
        );
        detailsDialogTitle = "Bulk Refusal Detail";
        loadPermission = BULK_REFUSAL_LOAD_PERMISSION;
        break;
      case BULK_INVALID_JOB_TYPE:
        selectedJob = this.state.bulkInvalidJobs.find(
          (job) => job.id === this.state.selectedJob,
        );
        detailsDialogTitle = "Bulk Case Invalidation Detail";
        loadPermission = BULK_INVALID_LOAD_PERMISSION;
        break;
      case BULK_UPDATE_SAMPLE_JOB_TYPE:
        selectedJob = this.state.bulkUpdateSampleJobs.find(
          (job) => job.id === this.state.selectedJob,
        );
        detailsDialogTitle = "Bulk Update Sample Detail";
        loadPermission = BULK_UPDATE_SAMPLE_LOAD_PERMISSION;
        break;
      case BULK_UPDATE_SAMPLE_SENSITIVE:
        selectedJob = this.state.bulkUpdateSampleSensitiveJobs.find(
          (job) => job.id === this.state.selectedJob,
        );
        detailsDialogTitle = "Bulk Update Sample Sensitive Detail";
        loadPermission = BULK_UPDATE_SAMPLE_SENSITIVE_LOAD_PERMISSION;
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
          BULK_REFUSAL_JOB_TYPE,
          "Bulk Refusals",
          BULK_REFUSAL_LOAD_PERMISSION,
          BULK_REFUSAL_VIEW_PERMISSION,
        )}

        {this.buildBulkProcessTable(
          this.state.bulkInvalidJobs,
          BULK_INVALID_JOB_TYPE,
          "Bulk Case Invalidation",
          BULK_INVALID_LOAD_PERMISSION,
          BULK_INVALID_VIEW_PERMISSION,
        )}

        {this.buildBulkProcessTable(
          this.state.bulkUpdateSampleJobs,
          BULK_UPDATE_SAMPLE_JOB_TYPE,
          "Bulk Update Sample",
          BULK_UPDATE_SAMPLE_LOAD_PERMISSION,
          BULK_UPDATE_SAMPLE_VIEW_PERMISSION,
        )}

        {this.buildBulkProcessTable(
          this.state.bulkUpdateSampleSensitiveJobs,
          BULK_UPDATE_SAMPLE_SENSITIVE,
          "Bulk Update Sample Sensitive",
          BULK_UPDATE_SAMPLE_SENSITIVE_LOAD_PERMISSION,
          BULK_UPDATE_SAMPLE_SENSITIVE_VIEW_PERMISSION,
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
          jobTitle={detailsDialogTitle}
          job={selectedJob}
          showDetails={this.state.showDetails}
          handleClosedDetails={this.handleClosedDetails}
          onClickAway={this.handleClosedDetails}
          onProcessJob={this.onProcessJob}
          showCancelDialog={this.state.showCancelDialog}
          openCancelDialog={this.openCancelDialog}
          closeCancelDialog={this.closeCancelDialog}
          onCancelJob={this.onCancelJob}
          authorisedActivities={this.state.authorisedActivities}
          loadPermission={loadPermission}
        />
      </div>
    );
  }
}

export default BulkUploads;
