import React, { Component } from "react";
import "@fontsource/roboto";
import { Typography, Paper, Button } from "@material-ui/core";
import Table from "@material-ui/core/Table";
import TableBody from "@material-ui/core/TableBody";
import TableCell from "@material-ui/core/TableCell";
import TableContainer from "@material-ui/core/TableContainer";
import TableHead from "@material-ui/core/TableHead";
import TableRow from "@material-ui/core/TableRow";
import Refusal from "./Refusal";
import InvalidCase from "./InvalidCase";
import PrintFulfilment from "./PrintFulfilment";
import SensitiveData from "./SensitiveData";
import { Link } from "react-router-dom";
import SmsFulfilment from "./SmsFulfilment";

class CaseDetails extends Component {
  state = {
    authorisedActivities: [],
    case: null,
    events: [],
    uacQidLinks: [],
    surveyName: "",
    collectionExerciseName: "",
  };

  componentDidMount() {
    this.getSurveyName(); // Only need to do this once; don't refresh it repeatedly as it changes infrequently
    this.getCollectionExerciseName();
    this.getAuthorisedActivities(); // Only need to do this once; don't refresh it repeatedly as it changes infrequently
    this.getAllBackendData();

    this.interval = setInterval(() => this.getAllBackendData(), 1000);
  }

  componentWillUnmount() {
    clearInterval(this.interval);
  }

  getSurveyName = async () => {
    const response = await fetch(`/api/surveys/${this.props.surveyId}`);

    const surveyJson = await response.json();

    this.setState({ surveyName: surveyJson.name });
  };

  getCollectionExerciseName = async () => {
    const response = await fetch(
        `api/collectionExercises/${this.props.collectionExerciseId}`);

    if (!response.ok) {
      return;
    }

    const collectionExerciseJson = await response.json();

    this.setState({ collectionExerciseName: collectionExerciseJson.name });
  };

  getAuthorisedActivities = async () => {
    const response = await fetch(`/api/auth?surveyId=${this.props.surveyId}`);

    // TODO: We need more elegant error handling throughout the whole application, but this will at least protect temporarily
    if (!response.ok) {
      return;
    }

    const authJson = await response.json();

    this.setState({ authorisedActivities: authJson });
  };

  getAllBackendData = async () => {
    const response = await fetch(`/api/cases/${this.props.caseId}`);
    const caseJson = await response.json();

    if (response.ok) {
      this.setState({ case: caseJson });

      const uacQidLinksResponse = await fetch(
        `/api/cases/${this.props.caseId}/uacQidLinks`
      );
      const uacQidLinksJson = await uacQidLinksResponse.json();

      const uacQidLinks = uacQidLinksJson._embedded.uacQidLinks;

      let events = caseJson.events;
      for (let i = 0; i < uacQidLinks.length; i++) {
        events = events.concat(uacQidLinks[i].events);
      }

      if (uacQidLinksResponse.ok) {
        this.setState({
          case: caseJson,
          uacQidLinks: uacQidLinks,
          events: events,
        });
      }
    }
  };

  onDeactivate = (qid) => {
    fetch(`/api/deactivateUac/${qid}`);
  };

  render() {
    const caseEvents = this.state.events.map((event, index) => (
      <TableRow key={index}>
        <TableCell component="th" scope="row">
          {event.dateTime}
        </TableCell>
        <TableCell component="th" scope="row">
          {event.description}
        </TableCell>
        <TableCell component="th" scope="row">
          {event.source}
        </TableCell>
      </TableRow>
    ));

    const uacQids = this.state.uacQidLinks.map((uacQidLink, index) => (
      <TableRow key={index}>
        <TableCell component="th" scope="row">
          {uacQidLink.qid}
        </TableCell>
        <TableCell component="th" scope="row">
          {uacQidLink.createdAt}
        </TableCell>
        <TableCell component="th" scope="row">
          {uacQidLink.lastUpdatedAt}
        </TableCell>
        <TableCell component="th" scope="row">
          {uacQidLink.active ? "Yes" : "No"}
        </TableCell>
        <TableCell>
          {this.state.authorisedActivities.includes("DEACTIVATE_UAC") &&
            uacQidLink.active && (
              <Button
                onClick={() => this.onDeactivate(uacQidLink.qid)}
                variant="contained"
              >
                Deactivate
              </Button>
            )}
        </TableCell>
      </TableRow>
    ));

    return (
      <div>
        <Link to={`/search?surveyId=${this.props.surveyId}`}>
          ← Back to Search
        </Link>
        <Typography variant="h4" color="inherit" style={{ marginBottom: 20 }}>
          Case Details
        </Typography>
        {this.state.case && (
          <div>
            <TableContainer component={Paper} style={{ marginTop: 20 }}>
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
                    <div>Collection Exercise name: {this.state.collectionExerciseName}</div>
                    <div>Created at: {this.state.case.createdAt}</div>
                    <div>Last updated at: {this.state.case.lastUpdatedAt}</div>
                    <div>
                      Receipted:{" "}
                      {this.state.case.receiptReceived ? "Yes" : "No"}
                    </div>
                    <div>
                      Refused:{" "}
                      {this.state.case.refusalReceived
                        ? this.state.case.refusalReceived
                        : "No"}
                    </div>
                    <div>
                      Invalid: {this.state.case.addressInvalid ? "Yes" : "No"}
                    </div>
                    <div>
                      Launched EQ:{" "}
                      {this.state.case.surveyLaunched ? "Yes" : "No"}
                    </div>
                  </TableCell>
                  <TableCell align="right">
                    {this.state.authorisedActivities.includes(
                      "CREATE_CASE_REFUSAL"
                    ) && (
                      <Refusal
                        caseId={this.props.caseId}
                        case={this.state.case}
                      />
                    )}
                    {this.state.authorisedActivities.includes(
                      "CREATE_CASE_INVALID_CASE"
                    ) && <InvalidCase caseId={this.props.caseId} />}
                    {this.state.authorisedActivities.includes(
                      "CREATE_CASE_FULFILMENT"
                    ) && (
                      <PrintFulfilment
                        caseId={this.props.caseId}
                        surveyId={this.props.surveyId}
                      />
                    )}
                    {this.state.authorisedActivities.includes(
                      "UPDATE_SAMPLE_SENSITIVE"
                    ) && (
                      <SensitiveData
                        caseId={this.props.caseId}
                        surveyId={this.props.surveyId}
                      />
                    )}
                    {this.state.authorisedActivities.includes(
                      "CREATE_CASE_SMS_FULFILMENT"
                    ) && (
                      <SmsFulfilment
                        caseId={this.props.caseId}
                        surveyId={this.props.surveyId}
                      />
                    )}
                  </TableCell>
                </TableBody>
              </Table>
            </TableContainer>
            <TableContainer component={Paper} style={{ marginTop: 20 }}>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>Date</TableCell>
                    <TableCell>Description</TableCell>
                    <TableCell>Source</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>{caseEvents}</TableBody>
              </Table>
            </TableContainer>
            <TableContainer component={Paper} style={{ marginTop: 20 }}>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>QID</TableCell>
                    <TableCell>Created At</TableCell>
                    <TableCell>Last Updated At</TableCell>
                    <TableCell>Active</TableCell>
                    <TableCell>Action</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>{uacQids}</TableBody>
              </Table>
            </TableContainer>
          </div>
        )}
      </div>
    );
  }
}

export default CaseDetails;
