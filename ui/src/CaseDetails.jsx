import React, { Component } from "react";
import "@fontsource/roboto";
import {
  Typography,
  Paper,
  Button,
  Dialog,
  DialogContent,
  DialogTitle,
  DialogContentText,
} from "@material-ui/core";
import Table from "@material-ui/core/Table";
import TableBody from "@material-ui/core/TableBody";
import TableCell from "@material-ui/core/TableCell";
import TableContainer from "@material-ui/core/TableContainer";
import TableHead from "@material-ui/core/TableHead";
import TableRow from "@material-ui/core/TableRow";
import Refusal from "./Refusal";
import InvalidCase from "./InvalidCase";
import PrintFulfilment from "./PrintFulfilment";
import SampleData from "./SampleData";
import SensitiveData from "./SensitiveData";
import { Link } from "react-router-dom";
import SmsFulfilment from "./SmsFulfilment";
import EmailFulfilment from "./EmailFulfilment";
import JSONPretty from "react-json-pretty";
import { errorAlert, getLocalDateTime } from "./Utils";

class CaseDetails extends Component {
  state = {
    authorisedActivities: [],
    case: null,
    events: [],
    uacQidLinks: [],
    sample: {},
    eventToShow: null,
    surveyName: "",
    collexName: "",
    showDeactivaveDialog: false,
    qidToDeactivate: "",
  };

  componentDidMount() {
    this.getAuthorisedBackendData();
  }

  getAuthorisedBackendData = async () => {
    const authorisedActivities = await this.getAuthorisedActivities(); // Only need to do this once; don't refresh it repeatedly as it changes infrequently

    this.getSurveyName(authorisedActivities); // Only need to do this once; don't refresh it repeatedly as it changes infrequently
    this.getCasesAndQidData(authorisedActivities);

    // Left in to refresh event list
    this.interval = setInterval(
      () => this.getCasesAndQidData(authorisedActivities),
      10000,
    );
  };

  getSurveyName = async (authorisedActivities) => {
    if (!authorisedActivities.includes("VIEW_SURVEY")) return;

    const response = await fetch(`/api/surveys/${this.props.surveyId}`);

    const surveyJson = await response.json();

    this.setState({ surveyName: surveyJson.name });
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

  getCasesAndQidData = async (authorisedActivities) => {
    if (!authorisedActivities.includes("VIEW_CASE_DETAILS")) return;

    const response = await fetch(`/api/cases/${this.props.caseId}`);
    const caseJson = await response.json();

    if (response.ok) {
      this.setState({
        case: caseJson,
        uacQidLinks: caseJson.uacQidLinks,
        events: caseJson.events,
        sample: caseJson.sample,
      });
    }
  };

  onClickDeactivate = (qid) => {
    this.confirmDeactivateInProgress = false;
    this.setState({
      showDeactivaveDialog: true,
      qidToDeactivate: qid,
    });
  };

  confirmDeactivate = () => {
    if (this.confirmDeactivateInProgress) {
      return;
    }

    this.confirmDeactivateInProgress = true;

    fetch(`/api/deactivateUac/${this.state.qidToDeactivate}`);

    this.setState({
      showDeactivaveDialog: false,
      qidToDeactivate: "",
    });
  };

  cancelDeactivate = () => {
    this.setState({
      showDeactivaveDialog: false,
      qidToDeactivate: "",
    });
  };

  openEventPayloadDialog = (event) => {
    this.setState({
      eventToShow: event,
    });
  };

  closeEventDialog = () => {
    this.setState({ eventToShow: null });
  };

  render() {
    const sortedCaseEvents = this.state.events.sort((first, second) =>
      first.dateTime.localeCompare(second.dateTime),
    );
    sortedCaseEvents.reverse();

    const caseEvents = sortedCaseEvents.map((event, index) => (
      <TableRow key={index}>
        <TableCell component="th" scope="row">
          {getLocalDateTime(event.dateTime)}
        </TableCell>
        <TableCell component="th" scope="row">
          {event.description}
        </TableCell>
        <TableCell component="th" scope="row">
          {event.source}
        </TableCell>
        <TableCell component="th" scope="row">
          <Button
            onClick={() => this.openEventPayloadDialog(event)}
            variant="contained"
          >
            {event.type}
          </Button>
        </TableCell>
      </TableRow>
    ));

    const sortedUacQidLinks = this.state.uacQidLinks.sort((first, second) =>
      first.createdAt.localeCompare(second.createdAt),
    );
    sortedUacQidLinks.reverse();

    const uacQids = sortedUacQidLinks.map((uacQidLink, index) => (
      <TableRow key={index}>
        <TableCell component="th" scope="row">
          {uacQidLink.qid}
        </TableCell>
        <TableCell component="th" scope="row">
          {getLocalDateTime(uacQidLink.createdAt)}
        </TableCell>
        <TableCell component="th" scope="row">
          {getLocalDateTime(uacQidLink.lastUpdatedAt)}
        </TableCell>
        <TableCell component="th" scope="row">
          {uacQidLink.active ? "Yes" : "No"}
        </TableCell>
        <TableCell component="th" scope="row">
          {JSON.stringify(uacQidLink.metadata)}
        </TableCell>
        <TableCell component="th" scope="row">
          {uacQidLink.eqLaunched ? "Yes" : "No"}
        </TableCell>
        <TableCell component="th" scope="row">
          {uacQidLink.receiptReceived ? "Yes" : "No"}
        </TableCell>
        <TableCell>
          {this.state.authorisedActivities.includes("DEACTIVATE_UAC") &&
            uacQidLink.active && (
              <Button
                onClick={() => this.onClickDeactivate(uacQidLink.qid)}
                variant="contained"
              >
                Deactivate
              </Button>
            )}
        </TableCell>
      </TableRow>
    ));

    let sampleData = Object.keys(this.state.sample);
    const sampleDataHeaders = sampleData.map((sampleHeader, index) => (
      <TableCell key={index}>{sampleHeader}</TableCell>
    ));

    const sampleDataRows = sampleData.map((sampleHeader, index) => (
      <TableCell key={index}>{this.state.sample[sampleHeader]}</TableCell>
    ));

    return (
      <div>
        <Link to={`/search?surveyId=${this.props.surveyId}`}>
          ← Back to Search
        </Link>
        <Typography variant="h4" color="inherit">
          Case Details
        </Typography>
        {this.state.case && (
          <div>
            <TableContainer component={Paper}>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>Details</TableCell>
                    <TableCell align="right">Action</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  <TableCell component="th" scope="row">
                    <div>Case ref: {this.state.case.caseRef}</div>
                    <div>Survey name: {this.state.surveyName}</div>
                    <div>
                      Collection Exercise name:{" "}
                      {this.state.case.collectionExerciseName}
                    </div>
                    <div>
                      Created at: {getLocalDateTime(this.state.case.createdAt)}
                    </div>
                    <div>
                      Last updated at:{" "}
                      {getLocalDateTime(this.state.case.lastUpdatedAt)}
                    </div>
                    <div>
                      Refused:{" "}
                      {this.state.case.refusalReceived
                        ? this.state.case.refusalReceived
                        : "No"}
                    </div>
                    <div>Invalid: {this.state.case.invalid ? "Yes" : "No"}</div>
                  </TableCell>
                  <TableCell align="right">
                    {this.state.authorisedActivities.includes(
                      "CREATE_CASE_REFUSAL",
                    ) && (
                      <Refusal
                        caseId={this.props.caseId}
                        case={this.state.case}
                      />
                    )}
                    {this.state.authorisedActivities.includes(
                      "CREATE_CASE_INVALID_CASE",
                    ) && <InvalidCase caseId={this.props.caseId} />}
                    {this.state.authorisedActivities.includes(
                      "UPDATE_SAMPLE",
                    ) && (
                      <SampleData
                        caseId={this.props.caseId}
                        surveyId={this.props.surveyId}
                      />
                    )}
                    {this.state.authorisedActivities.includes(
                      "UPDATE_SAMPLE_SENSITIVE",
                    ) && (
                      <SensitiveData
                        caseId={this.props.caseId}
                        surveyId={this.props.surveyId}
                      />
                    )}
                    {this.state.authorisedActivities.includes(
                      "CREATE_CASE_EXPORT_FILE_FULFILMENT",
                    ) && (
                      <PrintFulfilment
                        caseId={this.props.caseId}
                        surveyId={this.props.surveyId}
                      />
                    )}
                    {this.state.authorisedActivities.includes(
                      "CREATE_CASE_SMS_FULFILMENT",
                    ) && (
                      <SmsFulfilment
                        caseId={this.props.caseId}
                        surveyId={this.props.surveyId}
                      />
                    )}
                    {this.state.authorisedActivities.includes(
                      "CREATE_CASE_EMAIL_FULFILMENT",
                    ) && (
                      <EmailFulfilment
                        caseId={this.props.caseId}
                        surveyId={this.props.surveyId}
                      />
                    )}
                  </TableCell>
                </TableBody>
              </Table>
            </TableContainer>
            <Typography variant="h6" color="inherit" style={{ marginTop: 20 }}>
              Event History
            </Typography>
            <TableContainer component={Paper}>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>Date</TableCell>
                    <TableCell>Description</TableCell>
                    <TableCell>Source</TableCell>
                    <TableCell>Event Type</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>{caseEvents}</TableBody>
              </Table>
            </TableContainer>
            <Typography variant="h6" color="inherit" style={{ marginTop: 20 }}>
              UACs and QIDs
            </Typography>
            <TableContainer component={Paper}>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>QID</TableCell>
                    <TableCell>Created At</TableCell>
                    <TableCell>Last Updated At</TableCell>
                    <TableCell>Active</TableCell>
                    <TableCell>UAC Metadata</TableCell>
                    <TableCell>EQ Launched</TableCell>
                    <TableCell>Receipt Received</TableCell>
                    <TableCell>Action</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>{uacQids}</TableBody>
              </Table>
            </TableContainer>
            {this.state.authorisedActivities.includes("VIEW_CASE_DETAILS") && (
              <div>
                <Typography
                  variant="h6"
                  color="inherit"
                  style={{ marginTop: 20 }}
                >
                  Sample Data (Non-Sensitive)
                </Typography>
                <TableContainer component={Paper}>
                  <Table>
                    <TableHead>
                      <TableRow>{sampleDataHeaders}</TableRow>
                    </TableHead>
                    <TableBody>{sampleDataRows}</TableBody>
                  </Table>
                </TableContainer>
              </div>
            )}
          </div>
        )}
        {this.state.eventToShow && (
          <Dialog open={true}>
            <DialogContent style={{ padding: 30 }}>
              <div>
                <Typography
                  variant="h5"
                  color="inherit"
                  style={{ margin: 10, padding: 10 }}
                >
                  Event Type: {this.state.eventToShow.type}
                </Typography>
              </div>
              <div>
                <Typography
                  variant="inherit"
                  color="inherit"
                  style={{ margin: 10, padding: 10 }}
                >
                  Time of event:{" "}
                  {getLocalDateTime(this.state.eventToShow.dateTime)}
                </Typography>
              </div>
              <div>
                <Typography
                  variant="inherit"
                  color="inherit"
                  style={{ margin: 10, padding: 10 }}
                >
                  Event source: {this.state.eventToShow.source}
                </Typography>
              </div>
              <div>
                <Typography
                  variant="inherit"
                  color="inherit"
                  style={{ margin: 10, padding: 10 }}
                >
                  Event channel: {this.state.eventToShow.channel}
                </Typography>
              </div>
              <div>
                <Typography
                  variant="inherit"
                  color="inherit"
                  style={{ margin: 10, padding: 10 }}
                >
                  Correlation ID: {this.state.eventToShow.correlationId}
                </Typography>
              </div>
              <div>
                <Typography
                  variant="inherit"
                  color="inherit"
                  style={{ margin: 10, padding: 10 }}
                >
                  Message ID: {this.state.eventToShow.messageId}
                </Typography>
              </div>
              <div>
                <Typography
                  variant="inherit"
                  color="inherit"
                  style={{ margin: 10, padding: 10 }}
                >
                  Event payload:
                  <JSONPretty
                    id="json-pretty"
                    data={this.state.eventToShow.payload}
                    style={{ margin: 10, padding: 10 }}
                  />
                </Typography>
              </div>
              <Button
                onClick={this.closeEventDialog}
                variant="contained"
                style={{ margin: 10 }}
              >
                Cancel
              </Button>
            </DialogContent>
          </Dialog>
        )}
        <Dialog open={this.state.showDeactivaveDialog}>
          <DialogTitle id="alert-dialog-title">
            {"Confirm deactivate?"}
          </DialogTitle>
          <DialogContent>
            <DialogContentText id="alert-dialog-description">
              Are you sure you want to deactivate this UAC?
            </DialogContentText>
          </DialogContent>
          <div style={{ textAlign: "center" }}>
            <Button
              onClick={this.confirmDeactivate}
              variant="contained"
              style={{ margin: 10 }}
            >
              Yes
            </Button>
            <Button
              onClick={this.cancelDeactivate}
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

export default CaseDetails;
