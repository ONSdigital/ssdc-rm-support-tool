import React, { Component } from "react";
import { Link } from "react-router-dom";
import {
  Typography,
  Paper,
  Button,
  Dialog,
  DialogContent,
} from "@material-ui/core";
import Table from "@material-ui/core/Table";
import TableBody from "@material-ui/core/TableBody";
import TableCell from "@material-ui/core/TableCell";
import TableContainer from "@material-ui/core/TableContainer";
import TableHead from "@material-ui/core/TableHead";
import TableRow from "@material-ui/core/TableRow";

class ExceptionManager extends Component {
  state = {
    authorisedActivities: [],
    exceptions: [],
    exceptionDetails: null,
  };

  componentDidMount() {
    this.getAuthorisedActivities(); // Only need to do this once; don't refresh it repeatedly as it changes infrequently
    this.refreshDataFromBackend();

    this.interval = setInterval(() => this.refreshDataFromBackend(), 1000);
  }

  componentWillUnmount() {
    clearInterval(this.interval);
  }

  refreshDataFromBackend = async () => {
    const response = await fetch("/api/exceptionManager/badMessagesSummary");
    const exceptions = await response.json();

    this.setState({ exceptions: exceptions });
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
  };

  openDetailsDialog = async (messageHash) => {
    const response = await fetch(
      `/api/exceptionManager/badMessage/${messageHash}`
    );
    const exceptionDetails = await response.json();

    this.setState({ exceptionDetails: exceptionDetails });
  };

  closeDetailsDialog = () => {
    this.setState({ exceptionDetails: null });
  };

  render() {
    const exceptionTableRows = this.state.exceptions.map((exception) => (
      <TableRow key={exception.messageHash}>
        <TableCell component="th" scope="row">
          {exception.firstSeen}
        </TableCell>
        <TableCell component="th" scope="row">
          {exception.lastSeen}
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
      exceptionDetailsSections = this.state.exceptionDetails.map((exceptionDetail) => (
        <>
            <div>Class: {exceptionDetail.exceptionReport.exceptionClass}</div>
            <div>Message: {exceptionDetail.exceptionReport.exceptionMessage}</div>
            <div>Root cause: {exceptionDetail.exceptionReport.exceptionRootCause}</div>
        </>
      ));
    }

    return (
      <div style={{ padding: 20 }}>
        <Link to="/">← Back to home</Link>
        <Typography variant="h4" color="inherit" style={{ marginBottom: 20 }}>
          Exception Manager
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
            <div>
            {exceptionDetailsSections}
            </div>
              <div>
                <Button
                  onClick={this.closeDetailsDialog}
                  variant="contained"
                  style={{ margin: 10 }}
                >
                  Cancel
                </Button>
              </div>
            </DialogContent>
          </Dialog>
        )}
      </div>
    );
  }
}

export default ExceptionManager;
