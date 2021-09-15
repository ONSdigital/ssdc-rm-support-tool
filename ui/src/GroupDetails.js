import React, { Component } from "react";
import { Link } from "react-router-dom";
import {
  Typography,
  Paper,
  Button,
  Dialog,
  DialogContent,
  MenuItem,
  FormControl,
  InputLabel,
  Select,
  DialogTitle,
  DialogContentText,
  DialogActions,
} from "@material-ui/core";
import Table from "@material-ui/core/Table";
import TableBody from "@material-ui/core/TableBody";
import TableCell from "@material-ui/core/TableCell";
import TableContainer from "@material-ui/core/TableContainer";
import TableHead from "@material-ui/core/TableHead";
import TableRow from "@material-ui/core/TableRow";
import { uuidv4 } from "./common";

class GroupDetails extends Component {
  state = {
    authorisedActivities: [],
    isLoading: true,
    group: {},
    groupActivities: [],
    allActivities: [],
    allSurveys: [],
    showAllowDialog: false,
    showRemoveDialog: false,
    activity: null,
    activityValidationError: false,
    surveyId: null,
    surveyName: null,
    userGroupPermissionId: null,
  };

  componentDidMount() {
    this.getAuthorisedActivities(); // Only need to do this once; don't refresh it repeatedly as it changes infrequently
    this.getGroup();
    this.getAllActivities();
    this.getAllSurveys();
    this.getUserGroupPermissions();

    this.interval = setInterval(() => this.getUserGroupPermissions(), 1000);
  }

  componentWillUnmount() {
    clearInterval(this.interval);
  }

  getGroup = async () => {
    const groupResponse = await fetch(`/api/userGroups/${this.props.groupId}`);

    const groupJson = await groupResponse.json();

    this.setState({
      group: groupJson,
    });
  };

  getUserGroupPermissions = async () => {
    const permissionsResponse = await fetch(
      `/api/userGroups/${this.props.groupId}/permissions`
    );

    const permissionsJson = await permissionsResponse.json();

    const groupActivities = await this.getGroupActivities(
      permissionsJson._embedded.userGroupPermissions
    );

    this.setState({
      groupActivities: groupActivities,
    });
  };

  getGroupActivities = async (userGroupPermissions) => {
    let groupActivities = [];

    for (const userGroupPermission of userGroupPermissions) {
      const userGroupPermissionId = userGroupPermission._links.self.href
        .split("/")
        .pop();

      const permissionsResponse = await fetch(
        `/api/userGroupPermissions/${userGroupPermissionId}`
      );

      const permissionsJson = await permissionsResponse.json();

      const activity = permissionsJson.authorisedActivity;

      const surveyResponse = await fetch(
        `/api/userGroupPermissions/${userGroupPermissionId}/survey`
      );

      let surveyName = "All Surveys - Global permission";

      if (surveyResponse.ok) {
        const surveyJson = await surveyResponse.json();
        surveyName = surveyJson.name;
      }

      groupActivities.push({
        activity: activity,
        surveyName: surveyName,
        userGroupPermissionId: userGroupPermissionId,
      });
    }

    return groupActivities;
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

  getAllActivities = async () => {
    const activitiesResponse = await fetch("/api/authorisedActivityTypes");
    const activitiesJson = await activitiesResponse.json();

    this.setState({
      allActivities: activitiesJson,
    });
  };

  getAllSurveys = async () => {
    const surveysResponse = await fetch("/api/surveys");
    const surveysJson = await surveysResponse.json();

    this.setState({
      allSurveys: surveysJson._embedded.surveys,
    });
  };

  openAllowDialog = () => {
    this.setState({
      activity: null,
      activityValidationError: false,
      surveyId: null,
      showAllowDialog: true,
    });
  };

  closeAllowDialog = () => {
    this.setState({
      showAllowDialog: false,
    });
  };

  openRemoveDialog = (activity, surveyName, userGroupPermissionId) => {
    this.setState({
      showRemoveDialog: true,
      activity: activity,
      surveyName: surveyName,
      userGroupPermissionId: userGroupPermissionId,
    });
  };

  closeRemoveDialog = () => {
    this.setState({
      showRemoveDialog: false,
    });
  };

  removeActivityPermission = async () => {
    await fetch(
      `/api/userGroupPermissions/${this.state.userGroupPermissionId}`,
      {
        method: "DELETE",
      }
    );
    this.closeRemoveDialog();
  };

  onActivityChange = (event) => {
    this.setState({
      activity: event.target.value,
    });
  };

  onSurveyChange = (event) => {
    this.setState({
      surveyId: event.target.value,
    });
  };

  onAllow = async () => {
    if (!this.state.activity) {
      this.setState({
        activityValidationError: true,
      });

      return;
    }

    var newUserGroupPermission;

    if (this.state.surveyId) {
      newUserGroupPermission = {
        id: uuidv4(),
        authorisedActivity: this.state.activity,
        group: `userGroups/${this.props.groupId}`,
        survey: `surveys/${this.state.surveyId}`,
      };
    } else {
      newUserGroupPermission = {
        id: uuidv4(),
        authorisedActivity: this.state.activity,
        group: `userGroups/${this.props.groupId}`,
      };
    }

    await fetch("/api/userGroupPermissions", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(newUserGroupPermission),
    });

    this.setState({ showAllowDialog: false });
  };

  render() {
    const groupActivitiesTableRows = this.state.groupActivities.map(
      (groupActivity, index) => {
        return (
          <TableRow key={index}>
            <TableCell component="th" scope="row">
              {groupActivity.activity}
            </TableCell>
            <TableCell component="th" scope="row">
              {groupActivity.surveyName}
            </TableCell>
            <TableCell component="th" scope="row">
              <Button
                variant="contained"
                onClick={() =>
                  this.openRemoveDialog(
                    groupActivity.activity,
                    groupActivity.surveyName,
                    groupActivity.userGroupPermissionId
                  )
                }
              >
                Remove
              </Button>
            </TableCell>
          </TableRow>
        );
      }
    );

    const activityMenuItems = this.state.allActivities.map((activity) => {
      return (
        <MenuItem key={activity} value={activity}>
          {activity}
        </MenuItem>
      );
    });

    const surveyMenuItems = this.state.allSurveys.map((survey) => {
      const surveyId = survey._links.self.href.split("/").pop();

      return (
        <MenuItem key={surveyId} value={surveyId}>
          {survey.name}
        </MenuItem>
      );
    });

    return (
      <div style={{ padding: 20 }}>
        <Link to="/userAdmin">← Back to admin</Link>
        <Typography variant="h4" color="inherit" style={{ marginBottom: 20 }}>
          Group Details: {this.state.group.name}
        </Typography>
        {!this.state.authorisedActivities.includes("SUPER_USER") &&
          !this.state.isLoading && (
            <h1 style={{ color: "red" }}>YOU ARE NOT AUTHORISED</h1>
          )}
        {this.state.authorisedActivities.includes("SUPER_USER") && (
          <>
            <Button
              variant="contained"
              onClick={this.openAllowDialog}
              style={{ marginTop: 20 }}
            >
              Allow Activity
            </Button>
            <TableContainer component={Paper} style={{ marginTop: 20 }}>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>Activity</TableCell>
                    <TableCell>Survey</TableCell>
                    <TableCell></TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>{groupActivitiesTableRows}</TableBody>
              </Table>
            </TableContainer>
            <Dialog open={this.state.showAllowDialog}>
              <DialogContent
                style={{ paddingLeft: 30, paddingRight: 30, paddingBottom: 10 }}
              >
                <div>
                  <FormControl required fullWidth={true}>
                    <InputLabel>Activity</InputLabel>
                    <Select
                      onChange={this.onActivityChange}
                      value={this.state.activity}
                      error={this.state.activityValidationError}
                    >
                      {activityMenuItems}
                    </Select>
                  </FormControl>
                  <FormControl fullWidth={true}>
                    <InputLabel>Survey</InputLabel>
                    <Select
                      onChange={this.onSurveyChange}
                      value={this.state.surveyId}
                    >
                      {surveyMenuItems}
                    </Select>
                  </FormControl>
                </div>
                <div style={{ marginTop: 10 }}>
                  <Button
                    onClick={this.onAllow}
                    variant="contained"
                    style={{ margin: 10 }}
                  >
                    Allow
                  </Button>
                  <Button
                    onClick={this.closeAllowDialog}
                    variant="contained"
                    style={{ margin: 10 }}
                  >
                    Cancel
                  </Button>
                </div>
              </DialogContent>
            </Dialog>
            <Dialog open={this.state.showRemoveDialog}>
              <DialogTitle id="alert-dialog-title">
                {"Confirm remove?"}
              </DialogTitle>
              <DialogContent>
                <DialogContentText id="alert-dialog-description">
                  Are you sure you wish to remove activity: "
                  {this.state.activity}" for survey: "{this.state.surveyName}"?
                </DialogContentText>
              </DialogContent>
              <DialogActions>
                <Button onClick={this.removeActivityPermission} color="primary">
                  Yes
                </Button>
                <Button
                  onClick={this.closeRemoveDialog}
                  color="primary"
                  autoFocus
                >
                  No
                </Button>
              </DialogActions>
            </Dialog>
          </>
        )}
      </div>
    );
  }
}

export default GroupDetails;
