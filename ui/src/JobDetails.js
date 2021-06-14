import React, { Component } from 'react';
import '@fontsource/roboto';
import { Typography, Grid, Button, Dialog, DialogContent, LinearProgress } from '@material-ui/core';
import { convertStatusText } from './common'

class JobDetails extends Component {
  render() {

    var jobDetailsFragment
    if (this.props.job) {
      jobDetailsFragment = (
        <Grid container spacing={1}>
          <Grid container item xs={12} spacing={3}>
            <Typography variant="h5" color="inherit" style={{ margin: 10, padding: 10 }}>
              {this.props.jobTitle}
            </Typography>
          </Grid>
          <Grid container item xs={12} spacing={3}>
            <Typography variant="inherit" color="inherit" style={{ margin: 10, padding: 10 }}>
              File: {this.props.job.fileName}
            </Typography>
          </Grid>
          <Grid container item xs={12} spacing={3}>
            <Typography variant="inherit" color="inherit" style={{ margin: 10, padding: 10 }}>
              File line count: {this.props.job.fileRowCount}
            </Typography>
          </Grid>
          <Grid container item xs={12} spacing={3}>
            <Typography variant="inherit" color="inherit" style={{ margin: 10, padding: 10 }}>
              Job status: {convertStatusText(this.props.job.jobStatus)}
            </Typography>
          </Grid>
          <Grid container item xs={12} spacing={3}>
            <Typography variant="inherit" color="inherit" style={{ margin: 10, padding: 10 }}>
              Rows staged:
            </Typography>
            <LinearProgress
              variant="determinate"
              value={Math.round((this.props.job.stagedRowCount / (this.props.job.fileRowCount - 1)) * 100)}
              style={{ marginTop: 20, marginBottom: 20, width: 300 }} />
          </Grid>
          {!this.props.job.fatalErrorDescription &&
            <Grid container item xs={12}>
              <Grid container item xs={12} spacing={3}>
                <Typography variant="inherit" color="inherit" style={{ margin: 10, padding: 10 }}>
                  Rows processed:
            </Typography>
                <LinearProgress
                  variant="determinate"
                  value={Math.round(((this.props.job.processedRowCount + this.props.job.rowErrorCount) / (this.props.job.fileRowCount - 1)) * 100)}
                  style={{ marginTop: 20, marginBottom: 20, width: 300 }} />
              </Grid>
              <Grid container item xs={12} spacing={3}>
                <Typography variant="inherit" color="inherit" style={{ margin: 10, padding: 10 }}>
                  Errors: {this.props.job.rowErrorCount}
                </Typography>
              </Grid>
            </Grid>
          }
          {this.props.job.fatalErrorDescription &&
            <Grid container item xs={12} spacing={3}>
              <Typography variant="inherit" color="inherit" style={{ margin: 10, padding: 10 }}>
                Fatal error: {this.props.job.fatalErrorDescription}
              </Typography>
            </Grid>
          }
          <Grid container item xs={12} spacing={3}>
            <Typography variant="inherit" color="inherit" style={{ margin: 10, padding: 10 }}>
              Uploaded at: {this.props.job.createdAt}
            </Typography>
          </Grid>
          <Grid container item xs={12} spacing={3}>
            <Typography variant="inherit" color="inherit" style={{ margin: 10, padding: 10 }}>
              Uploaded by: {this.props.job.createdBy}
            </Typography>
          </Grid>
        </Grid>
      )
    }

    var fileDownloadHost = ""
    if (process.env.NODE_ENV !== 'production') {
      fileDownloadHost = "http://localhost:8080"
    }

    var buttonFragment
    if (this.props.job && this.props.job.jobStatus === 'PROCESSED_WITH_ERRORS') {
      buttonFragment = (
        <Grid container spacing={1}>
          <Button target="_blank" href={fileDownloadHost + "/job/" + this.props.job.id + "/error"} variant="contained" style={{ margin: 10 }}>
            Download Failed Rows CSV
            </Button>
          <Button target="_blank" href={fileDownloadHost + "/job/" + this.props.job.id + "/errorDetail"} variant="contained" style={{ margin: 10 }}>
            Download Error Details CSV
            </Button>
        </Grid>
      )
    }

    return (

      <Dialog open={this.props.showDetails} onClose={this.props.onClickAway}>
        <DialogContent style={{ padding: 30 }}>
          <Grid container spacing={1}>
            {jobDetailsFragment}
            {buttonFragment}
            <Button onClick={this.props.handleClosedDetails} variant="contained" style={{ margin: 10 }}>
              Close
            </Button>
          </Grid>
        </DialogContent>
      </Dialog>

    )
  }
}

export default JobDetails