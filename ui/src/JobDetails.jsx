import React, { Component } from "react";
import "@fontsource/roboto";
import {
  Typography,
  Grid,
  Button,
  Dialog,
  DialogContent,
  LinearProgress,
  DialogTitle,
  DialogContentText,
  DialogActions,
} from "@material-ui/core";
import { convertStatusText } from "./common";
import { getLocalDateTime } from "./Utils";

class JobDetails extends Component {
  render() {
    var jobDetailsFragment;
    let headerRowCorrection = 0;

    if (this.props.job) {
      if (this.props.job.sampleWithHeaderRow) {
        headerRowCorrection = 1;
      }

      jobDetailsFragment = (
        <Grid container spacing={1}>
          <Grid container item xs={12} spacing={3}>
            <Typography
              variant="h5"
              color="inherit"
              style={{ margin: 10, padding: 10 }}
            >
              {this.props.jobTitle}
            </Typography>
          </Grid>
          <Grid container item xs={12} spacing={3}>
            <Typography
              variant="inherit"
              color="inherit"
              style={{ margin: 10, padding: 10 }}
            >
              File: {this.props.job.fileName}
            </Typography>
          </Grid>
          <Grid container item xs={12} spacing={3}>
            <Typography
              variant="inherit"
              color="inherit"
              style={{ margin: 10, padding: 10 }}
            >
              File line count: {this.props.job.fileRowCount}
            </Typography>
          </Grid>
          <Grid container item xs={12} spacing={3}>
            <Typography
              variant="inherit"
              color="inherit"
              style={{ margin: 10, padding: 10 }}
            >
              Job status: {convertStatusText(this.props.job.jobStatus)}
            </Typography>
          </Grid>
          <Grid container item xs={12} spacing={3}>
            <Typography
              variant="inherit"
              color="inherit"
              style={{ margin: 10, padding: 10 }}
            >
              Rows staged:
            </Typography>
            <LinearProgress
              variant="determinate"
              value={Math.round(
                (this.props.job.stagedRowCount /
                  (this.props.job.fileRowCount - headerRowCorrection)) *
                  100,
              )}
              style={{ marginTop: 20, marginBottom: 20, width: 300 }}
            />
          </Grid>
          {!this.props.job.fatalErrorDescription && (
            <Grid container item xs={12}>
              <Grid container item xs={12} spacing={3}>
                <Typography
                  variant="inherit"
                  color="inherit"
                  style={{ margin: 10, padding: 10 }}
                >
                  Rows validated:
                </Typography>
                <LinearProgress
                  variant="determinate"
                  value={Math.round(
                    (this.props.job.validatedRowCount /
                      (this.props.job.fileRowCount - headerRowCorrection)) *
                      100,
                  )}
                  style={{ marginTop: 20, marginBottom: 20, width: 300 }}
                />
              </Grid>
              <Grid container item xs={12} spacing={3}>
                <Typography
                  variant="inherit"
                  color="inherit"
                  style={{ margin: 10, padding: 10 }}
                >
                  Rows processed:
                </Typography>
                <LinearProgress
                  variant="determinate"
                  value={Math.round(
                    (this.props.job.processedRowCount /
                      (this.props.job.fileRowCount -
                        headerRowCorrection -
                        this.props.job.rowErrorCount)) *
                      100,
                  )}
                  style={{ marginTop: 20, marginBottom: 20, width: 300 }}
                />
              </Grid>
              <Grid container item xs={12} spacing={3}>
                <Typography
                  variant="inherit"
                  color="inherit"
                  style={{ margin: 10, padding: 10 }}
                >
                  Errors: {this.props.job.rowErrorCount}
                </Typography>
              </Grid>
            </Grid>
          )}
          {this.props.job.fatalErrorDescription && (
            <Grid container item xs={12} spacing={3}>
              <Typography
                variant="inherit"
                color="inherit"
                style={{ margin: 10, padding: 10 }}
              >
                Fatal error: {this.props.job.fatalErrorDescription}
              </Typography>
            </Grid>
          )}
          <Grid container item xs={12} spacing={3}>
            <Typography
              variant="inherit"
              color="inherit"
              style={{ margin: 10, padding: 10 }}
            >
              Uploaded at: {getLocalDateTime(this.props.job.createdAt)}
            </Typography>
          </Grid>
          <Grid container item xs={12} spacing={3}>
            <Typography
              variant="inherit"
              color="inherit"
              style={{ margin: 10, padding: 10 }}
            >
              Uploaded by: {this.props.job.createdBy}
            </Typography>
          </Grid>
          {this.props.job.processedAt && (
            <Grid container item xs={12}>
              <Grid container item xs={12} spacing={3}>
                <Typography
                  variant="inherit"
                  color="inherit"
                  style={{ margin: 10, padding: 10 }}
                >
                  Processed at: {getLocalDateTime(this.props.job.processedAt)}
                </Typography>
              </Grid>
              <Grid container item xs={12} spacing={3}>
                <Typography
                  variant="inherit"
                  color="inherit"
                  style={{ margin: 10, padding: 10 }}
                >
                  Processed by: {this.props.job.processedBy}
                </Typography>
              </Grid>
            </Grid>
          )}
          {this.props.job.cancelledAt && (
            <Grid container item xs={12}>
              <Grid container item xs={12} spacing={3}>
                <Typography
                  variant="inherit"
                  color="inherit"
                  style={{ margin: 10, padding: 10 }}
                >
                  Cancelled at: {getLocalDateTime(this.props.job.cancelledAt)}
                </Typography>
              </Grid>
              <Grid container item xs={12} spacing={3}>
                <Typography
                  variant="inherit"
                  color="inherit"
                  style={{ margin: 10, padding: 10 }}
                >
                  Cancelled by: {this.props.job.cancelledBy}
                </Typography>
              </Grid>
            </Grid>
          )}
        </Grid>
      );
    }

    var fileDownloadHost = "";
    if (process.env.NODE_ENV !== "production") {
      fileDownloadHost = "http://localhost:8080";
    }

    var buttonFragment;
    if (
      this.props.job &&
      ["VALIDATED_WITH_ERRORS", "PROCESSING_IN_PROGRESS", "PROCESSED"].includes(
        this.props.job.jobStatus,
      ) &&
      this.props.job.rowErrorCount > 0 &&
      this.props.authorisedActivities.includes(this.props.loadPermission)
    ) {
      buttonFragment = (
        <Grid container spacing={1}>
          <Button
            target="_blank"
            href={fileDownloadHost + "/api/job/" + this.props.job.id + "/error"}
            variant="contained"
            style={{ margin: 10 }}
          >
            Download Failed Rows CSV
          </Button>
          <Button
            target="_blank"
            href={
              fileDownloadHost +
              "/api/job/" +
              this.props.job.id +
              "/errorDetail"
            }
            variant="contained"
            style={{ margin: 10 }}
          >
            Download Error Details CSV
          </Button>
        </Grid>
      );
    }

    return (
      <Dialog open={this.props.showDetails} onClose={this.props.onClickAway}>
        <DialogContent style={{ padding: 30 }}>
          <Grid container spacing={1}>
            {jobDetailsFragment}
            {buttonFragment}
            {this.props.job &&
              this.props.authorisedActivities.includes(
                this.props.loadPermission,
              ) &&
              ["VALIDATED_OK", "VALIDATED_WITH_ERRORS"].includes(
                this.props.job.jobStatus,
              ) &&
              this.props.job.rowErrorCount <
                this.props.job.fileRowCount - headerRowCorrection && (
                <Button
                  onClick={this.props.onProcessJob}
                  variant="contained"
                  style={{ margin: 10 }}
                  id="jobProcessBtn"
                >
                  Process
                </Button>
              )}
            {this.props.job &&
              this.props.authorisedActivities.includes(
                this.props.loadPermission,
              ) &&
              ["VALIDATED_OK", "VALIDATED_WITH_ERRORS"].includes(
                this.props.job.jobStatus,
              ) && (
                <Button
                  onClick={this.props.openCancelDialog}
                  variant="contained"
                  style={{ margin: 10 }}
                >
                  Cancel
                </Button>
              )}
            <Button
              onClick={this.props.handleClosedDetails}
              variant="contained"
              style={{ margin: 10 }}
              id="closeSampledetailsBtn"
            >
              Close
            </Button>
            {this.props.job &&
              this.props.authorisedActivities.includes(
                this.props.loadPermission,
              ) &&
              ["VALIDATED_OK", "VALIDATED_WITH_ERRORS"].includes(
                this.props.job.jobStatus,
              ) &&
              this.props.showCancelDialog === true && (
                <Dialog open={this.props.showCancelDialog}>
                  <DialogTitle id="alert-dialog-title">
                    {"Really Cancel?"}
                  </DialogTitle>
                  <DialogContent>
                    <DialogContentText id="alert-dialog-description">
                      Are you sure you wish cancel the upload? All error data
                      will be lost!
                    </DialogContentText>
                  </DialogContent>
                  <DialogActions>
                    <Button onClick={this.props.onCancelJob} color="primary">
                      Yes
                    </Button>
                    <Button
                      onClick={this.props.closeCancelDialog}
                      variant="contained"
                      color="primary"
                      autoFocus
                    >
                      No
                    </Button>
                  </DialogActions>
                </Dialog>
              )}
          </Grid>
        </DialogContent>
      </Dialog>
    );
  }
}

export default JobDetails;
