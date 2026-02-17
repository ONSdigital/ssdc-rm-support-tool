import React, { Component } from "react";
import { Link } from "react-router-dom";
import {
  Button,
  CircularProgress,
  Dialog,
  DialogContent,
  DialogContentText,
  DialogTitle,
  Paper,
  Typography,
} from "@material-ui/core";
import Table from "@material-ui/core/Table";
import TableBody from "@material-ui/core/TableBody";
import TableCell from "@material-ui/core/TableCell";
import TableContainer from "@material-ui/core/TableContainer";
import TableHead from "@material-ui/core/TableHead";
import TableRow from "@material-ui/core/TableRow";
import { errorAlert, getLocalDateTime } from "./Utils";

class ExceptionManager extends Component {
  state = {
    authorisedActivities: [],
    exceptions: [],
    exceptionDetails: null,
    showPeekDialog: false,
    showQuarantineDialog: false,
    peekResult: "",
    exceptionCount: "",
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

    // This has an interval still, as we presume devs doing support may wish to keep this screen up
    this.interval = setInterval(
      () => this.refreshDataFromBackend(authorisedActivities),
      10000,
    );
  };

  refreshDataFromBackend = async (authorisedActivities) => {
    if (!authorisedActivities.includes("EXCEPTION_MANAGER_VIEWER")) return;

    const countResponse = await fetch("/api/exceptionManager/badMessagesCount");
    const count = await countResponse.json();

    const response = await fetch("/api/exceptionManager/badMessagesSummary");
    const exceptions = await response.json();

    this.setState({
      exceptionCount: count,
      exceptions: exceptions,
    });
  };

  getAuthorisedActivities = async () => {
    const authResponse = await fetch("/api/auth");

    // TODO: We need more elegant error handling throughout the whole application, but this will at least protect temporarily
    const responseJson = await authResponse.json();
    if (!authResponse.ok) {
      errorAlert(responseJson);
      return;
    }
    this.setState({
      authorisedActivities: responseJson,
      isLoading: false,
    });

    return responseJson;
  };

  openDetailsDialog = async (messageHash) => {
    const response = await fetch(
      `/api/exceptionManager/badMessage/${messageHash}`,
    );
    const exceptionDetails = await response.json();

    this.setState({ exceptionDetails: exceptionDetails });
  };

  closeDetailsDialog = () => {
    this.setState({ exceptionDetails: null });
  };

  onPeek = () => {
    if (this.peekInProgress) {
      return;
    }

    this.peekInProgress = true;

    this.setState({ peekResult: "", showPeekDialog: true });

    this.peekMessage(
      this.state.exceptionDetails[0].exceptionReport.messageHash,
    );
  };

  peekMessage = async (messageHash) => {
    const response = await fetch(
      `/api/exceptionManager/peekMessage/${messageHash}`,
    );
    const peekText = await response.text();

    this.setState({ peekResult: peekText });
    this.peekInProgress = false; // Don't copy this pattern... go looking somewhere other than this file
  };

  closePeekDialog = () => {
    this.setState({ showPeekDialog: false });
  };

  openQuarantineDialog = () => {
    this.setState({
      showQuarantineDialog: true,
    });
  };

  closeQuarantineDialog = () => {
    this.setState({
      showQuarantineDialog: false,
    });
  };

  quarantineMessage = async () => {
    if (this.quarantineInProgress) {
      return;
    }

    this.quarantineInProgress = true;

    const response = await fetch(
      `/api/exceptionManager/skipMessage/${this.state.exceptionDetails[0].exceptionReport.messageHash}`,
    );

    if (response.ok) {
      this.setState({
        exceptionDetails: null,
      });
      this.closeQuarantineDialog();
    }

    this.quarantineInProgress = false; // Don't copy this pattern... go looking somewhere other than this file
  };

  render() {
    const activeExceptions = this.state.exceptions.filter(
      (exception) => !exception.quarantined,
    );

    var sortedExceptions = activeExceptions.sort((firstEx, secondEx) =>
      firstEx.firstSeen.localeCompare(secondEx.firstSeen),
    );
    sortedExceptions.reverse();

    const exceptionTableRows = sortedExceptions.map((exception) => (
      <TableRow key={exception.messageHash}>
        <TableCell component="th" scope="row">
          {getLocalDateTime(exception.firstSeen)}
        </TableCell>
        <TableCell component="th" scope="row">
          {getLocalDateTime(exception.lastSeen)}
        </TableCell>
        <TableCell component="th" scope="row">
          {exception.affectedServices.join(", ")}
        </TableCell>
        <TableCell component="th" scope="row">
          {exception.affectedSubscriptions.join(", ")}
        </TableCell>
        <TableCell component="th" scope="row">
          <Button
            variant="contained"
            onClick={() => this.openDetailsDialog(exception.messageHash)}
          >
            Details
          </Button>
        </TableCell>
      </TableRow>
    ));

    var exceptionDetailsSections;

    if (this.state.exceptionDetails) {
      exceptionDetailsSections = this.state.exceptionDetails.map(
        (exceptionDetail, index) => (
          <div key={index} style={{ marginTop: 10, marginBottom: 10 }}>
            <div>Index: {index}</div>
            <div>Class: {exceptionDetail.exceptionReport.exceptionClass}</div>
            <div>
              Message: {exceptionDetail.exceptionReport.exceptionMessage}
            </div>
            <div>
              Root cause: {exceptionDetail.exceptionReport.exceptionRootCause}
            </div>
          </div>
        ),
      );
    }

    return (
      <div style={{ padding: 20 }}>
        <Link to="/">← Back to home</Link>
        <Typography variant="h4" color="inherit" style={{ marginBottom: 20 }}>
          Exception Manager
        </Typography>
        <Typography variant="h6" color="inherit" style={{ marginBottom: 20 }}>
          Exception Count: {this.state.exceptionCount}
        </Typography>
        <TableContainer component={Paper} style={{ marginTop: 20 }}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>First Seen</TableCell>
                <TableCell>Last Seen</TableCell>
                <TableCell>Affected Services</TableCell>
                <TableCell>Affected Subscriptions</TableCell>
                <TableCell>Action</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>{exceptionTableRows}</TableBody>
          </Table>
        </TableContainer>
        {this.state.exceptionDetails && (
          <Dialog open={true} maxWidth={300}>
            <DialogContent style={{ padding: 30 }}>
              <div>{exceptionDetailsSections}</div>
              <div>
                {this.state.authorisedActivities.includes(
                  "EXCEPTION_MANAGER_PEEK",
                ) && (
                  <Button
                    onClick={this.onPeek}
                    variant="contained"
                    style={{ margin: 10 }}
                  >
                    Peek
                  </Button>
                )}
                {this.state.authorisedActivities.includes(
                  "EXCEPTION_MANAGER_QUARANTINE",
                ) && (
                  <Button
                    onClick={this.openQuarantineDialog}
                    variant="contained"
                    style={{ margin: 10 }}
                  >
                    Quarantine
                  </Button>
                )}
                <Button
                  onClick={this.closeDetailsDialog}
                  variant="contained"
                  style={{ margin: 10 }}
                >
                  Close
                </Button>
              </div>
            </DialogContent>
          </Dialog>
        )}
        {this.state.showPeekDialog && (
          <Dialog open={true} maxWidth={300}>
            <DialogContent style={{ padding: 30 }}>
              {!this.state.peekResult && <CircularProgress color="inherit" />}
              {this.state.peekResult && <p>{this.state.peekResult}</p>}
              <div>
                <Button
                  onClick={this.closePeekDialog}
                  variant="contained"
                  style={{ margin: 10 }}
                >
                  Close
                </Button>
              </div>
            </DialogContent>
          </Dialog>
        )}
        <Dialog open={this.state.showQuarantineDialog}>
          <DialogTitle id="alert-dialog-title">
            {"Confirm quarantine?"}
          </DialogTitle>
          <DialogContent>
            <DialogContentText id="alert-dialog-description">
              Are you sure you want to quarantine this message?
            </DialogContentText>
          </DialogContent>
          <div style={{ textAlign: "center" }}>
            <Button
              onClick={this.quarantineMessage}
              variant="contained"
              style={{ margin: 10 }}
            >
              Yes
            </Button>
            <Button
              onClick={this.closeQuarantineDialog}
              variant="contained"
              autoFocus
              style={{ margin: 10 }}
            >
              No
            </Button>
          </div>
        </Dialog>
      </div>
    );
  }
}

export default ExceptionManager;
