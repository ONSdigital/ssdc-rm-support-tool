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
  TextField,
} from "@material-ui/core";
import Table from "@material-ui/core/Table";
import TableBody from "@material-ui/core/TableBody";
import TableCell from "@material-ui/core/TableCell";
import TableContainer from "@material-ui/core/TableContainer";
import TableHead from "@material-ui/core/TableHead";
import TableRow from "@material-ui/core/TableRow";
import Autocomplete from "@material-ui/lab/Autocomplete";
import { errorAlert } from "./Utils";

const globalSurveyId = "GLOBAL";
const globalSurveyLabel = "All Surveys - Global permission";

class GroupDetails extends Component {
  state = {
    authorisedActivities: [],
    isLoading: true,
    group: {},
    admins: [],
    groupActivities: [],
    allActivities: [],
    globalActivities: [],
    allSurveys: [],
    allUsersAutocompleteOptions: [],
    allowedSurveys: [],
    showAllowDialog: false,
    showRemoveDialog: false,
    activity: null,
    activityValidationError: false,
    surveyId: null,
    surveyName: null,
    surveyValidationError: false,
    userGroupPermissionId: null,
    adminIdToRemove: null,
    adminEmailToRemove: null,
    showRemoveAdminDialog: false,
    showAddAdminToGroupDialog: false,
    newAdminUserId: "",
    newAdminEmailValidationError: false,
  };

  componentDidMount() {
    this.getAuthorisedBackendData();
  }

  getAuthorisedBackendData = async () => {
    const authorisedActivities = await this.getAuthorisedActivities(); // Only need to do this once; don't refresh it repeatedly as it changes infrequently
    this.getGroup(authorisedActivities);
    const admins = await this.getAdmins(authorisedActivities);
    this.getAllActivities();
    this.getGlobalActivities();
    this.getAllSurveys(authorisedActivities);
    this.getUserGroupPermissions(authorisedActivities);
    const allUsers = await this.getAllUsers(authorisedActivities); // Only need to do this once... it's an expensive operation
    this.filterAllUsers(allUsers, admins);

    this.setState({
      authorisedActivities: authorisedActivities,
      allUsers: allUsers,
    });

    this.refreshBackendData(authorisedActivities, allUsers);
  };

  refreshBackendData = async (authorisedActivities, allUsers) => {
    this.getUserGroupPermissions(authorisedActivities);
    const admins = await this.getAdmins(authorisedActivities);
    this.filterAllUsers(allUsers, admins);
  };

  getGroup = async (authorisedActivities) => {
    if (!authorisedActivities.includes("SUPER_USER")) return;

    const groupResponse = await fetch(`/api/userGroups/${this.props.groupId}`);

    const groupJson = await groupResponse.json();

    this.setState({
      group: groupJson,
    });
  };

  getAdmins = async (authorisedActivities) => {
    if (!authorisedActivities.includes("SUPER_USER")) return;

    const response = await fetch(
      `/api/userGroupAdmins/findByGroup/${this.props.groupId}`,
    );

    const responseJson = await response.json();

    this.setState({
      admins: responseJson,
    });

    return responseJson;
  };

  getAllUsers = async (authorisedActivities) => {
    if (!authorisedActivities.includes("SUPER_USER")) return;

    const response = await fetch("/api/users");

    // TODO: We need more elegant error handling throughout the whole application, but this will at least protect temporarily
    const responseJson = await response.json();
    if (!response.ok) {
      errorAlert(responseJson);
      return [];
    }

    return responseJson;
  };

  filterAllUsers = (allUsers, groupAdmins) => {
    const allUsersAutocompleteOptions = allUsers.filter(
      (user) =>
        !groupAdmins.map((groupAdmin) => groupAdmin.userId).includes(user.id),
    );

    this.setState({
      allUsersAutocompleteOptions: allUsersAutocompleteOptions,
    });
  };

  getUserGroupPermissions = async (authorisedActivities) => {
    if (!authorisedActivities.includes("SUPER_USER")) return;

    const permissionsResponse = await fetch(
      `/api/userGroupPermissions?groupId=${this.props.groupId}`,
    );

    const permissionsJson = await permissionsResponse.json();

    this.setState({
      groupActivities: permissionsJson,
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

  getAllActivities = async () => {
    // This is not an RBAC protected endpoint
    const activitiesResponse = await fetch("/api/authorisedActivityTypes");
    const activitiesJson = await activitiesResponse.json();

    this.setState({
      allActivities: activitiesJson,
    });
  };

  getGlobalActivities = async () => {
    // This is not an RBAC protected endpoint
    const activitiesResponse = await fetch(
      "/api/authorisedActivityTypes?globalOnly=true",
    );
    const activitiesJson = await activitiesResponse.json();

    this.setState({
      globalActivities: activitiesJson,
    });
  };

  getAllSurveys = async (authorisedActivities) => {
    if (!authorisedActivities.includes("LIST_SURVEYS")) return;

    const surveysResponse = await fetch("/api/surveys");
    const surveysJson = await surveysResponse.json();

    this.setState({
      allSurveys: surveysJson,
    });
  };

  openAllowDialog = () => {
    this.allowInProgress = false;

    this.setState({
      activity: null,
      activityValidationError: false,
      surveyValidationError: false,
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
    this.removeActivityPermissionInProgress = false;

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
    if (this.removeActivityPermissionInProgress) {
      return;
    }

    this.removeActivityPermissionInProgress = true;

    await fetch(
      `/api/userGroupPermissions/${this.state.userGroupPermissionId}`,
      {
        method: "DELETE",
      },
    );
    this.closeRemoveDialog();
    this.refreshBackendData(
      this.state.authorisedActivities,
      this.state.allUsers,
    );
  };

  onActivityChange = (event) => {
    // If the selected activity is global, the selected survey HAS to be "all surveys - global"
    if (this.state.globalActivities.includes(event.target.value)) {
      this.setState({
        surveyId: globalSurveyId,
      });
    }

    const existingPermissionSurveyIds = this.state.groupActivities
      .filter((activity) => activity.authorisedActivity === event.target.value)
      .map((permission) => permission.surveyId);

    // Build the list of surveys this activity is not already allowed on
    let allowedSurveys = [];
    if (!existingPermissionSurveyIds.includes(null)) {
      // For global permissions
      allowedSurveys.push(null);
    }
    allowedSurveys.push(
      ...this.state.allSurveys
        .filter((survey) => !existingPermissionSurveyIds.includes(survey.id))
        .sort((a, b) => a.name.localeCompare(b.name)), // Sort by survey name alphabetically
    );

    this.setState({
      activity: event.target.value,
      allowedSurveys: allowedSurveys,
      surveyValidationError: false,
    });
  };

  onSurveyChange = (event) => {
    this.setState({
      surveyId: event.target.value,
    });
  };

  onAllow = async () => {
    if (this.allowInProgress) {
      return;
    }

    this.allowInProgress = true;

    let failedValidation = false;

    if (!this.state.activity) {
      this.setState({
        activityValidationError: true,
      });
      failedValidation = true;
    }

    if (!this.state.surveyId) {
      this.setState({
        surveyValidationError: true,
      });
      failedValidation = true;
    }

    if (failedValidation) {
      this.allowInProgress = false;
      return;
    }

    let surveyId;
    if (this.state.surveyId === globalSurveyId) {
      surveyId = null;
    } else {
      surveyId = this.state.surveyId;
    }

    const newUserGroupPermission = {
      authorisedActivity: this.state.activity,
      groupId: this.props.groupId,
      surveyId: surveyId,
    };

    const response = await fetch("/api/userGroupPermissions", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(newUserGroupPermission),
    });

    if (!response.ok) {
      this.setState({
        activityValidationError: true,
      });
      this.allowInProgress = false;
      return;
    }

    this.setState({ showAllowDialog: false });
    this.refreshBackendData(
      this.state.authorisedActivities,
      this.state.allUsers,
    );
  };

  openRemoveAdminDialog = (adminToRemove) => {
    this.removeAdminInProgress = false;

    this.setState({
      adminIdToRemove: adminToRemove.id,
      adminEmailToRemove: adminToRemove.userEmail,
      showRemoveAdminDialog: true,
    });
  };

  removeAdmin = async () => {
    if (this.removeAdminInProgress) {
      return;
    }

    this.removeAdminInProgress = true;

    const response = await fetch(
      `/api/userGroupAdmins/${this.state.adminIdToRemove}`,
      {
        method: "DELETE",
      },
    );

    if (response.ok) {
      this.closeRemoveAdminDialog();
    }

    this.refreshBackendData(
      this.state.authorisedActivities,
      this.state.allUsers,
    );
  };

  closeRemoveAdminDialog = () => {
    this.setState({
      adminIdToRemove: null,
      adminEmailToRemove: null,
      showRemoveAdminDialog: false,
    });
  };

  openAddAdminUserDialog = () => {
    this.setState({
      newAdminUserId: null,
      newAdminEmailValidationError: false,
      showAddAdminToGroupDialog: true,
    });
  };

  onAddAdmin = async () => {
    if (this.addAdminInProgress) {
      return;
    }

    this.addAdminInProgress = true;

    if (!this.state.newAdminUserId) {
      this.setState({ newAdminEmailValidationError: true });
      return;
    }

    const newGroupAdmin = {
      userId: this.state.newAdminUserId,
      groupId: this.props.groupId,
    };

    const response = await fetch("/api/userGroupAdmins", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(newGroupAdmin),
    });

    if (response.ok) {
      this.closeAddAdminDialog();
    }

    this.addAdminInProgress = false;
    this.refreshBackendData(
      this.state.authorisedActivities,
      this.state.allUsers,
    );
  };

  closeAddAdminDialog = () => {
    this.setState({
      showAddAdminToGroupDialog: false,
    });
  };

  onNewAdminEmailChange = (_, newValue) => {
    this.setState({
      newAdminUserId: newValue ? newValue.id : null,
      newAdminEmailValidationError: newValue ? false : true,
    });
  };

  buildSurveyMenuItem = (survey) => {
    if (survey === null) {
      return (
        <MenuItem key={globalSurveyId} value={globalSurveyId}>
          <i>{globalSurveyLabel}</i>
        </MenuItem>
      );
    }
    return (
      <MenuItem key={survey.id} value={survey.id}>
        {survey.name}
      </MenuItem>
    );
  };

  render() {
    const adminsTableRows = this.state.admins.map((admin) => (
      <TableRow key={admin.id}>
        <TableCell component="th" scope="row">
          {admin.userEmail}
        </TableCell>
        <TableCell component="th" scope="row">
          <Button
            variant="contained"
            onClick={() => this.openRemoveAdminDialog(admin)}
          >
            Remove
          </Button>
        </TableCell>
      </TableRow>
    ));

    const groupActivitiesTableRows = this.state.groupActivities.map(
      (groupActivity, index) => {
        const surveyName = groupActivity.surveyId
          ? groupActivity.surveyName
          : globalSurveyLabel;

        return (
          <TableRow key={index}>
            <TableCell component="th" scope="row">
              {groupActivity.authorisedActivity}
            </TableCell>
            <TableCell component="th" scope="row">
              {surveyName}
            </TableCell>
            <TableCell component="th" scope="row">
              <Button
                variant="contained"
                onClick={() =>
                  this.openRemoveDialog(
                    groupActivity.authorisedActivity,
                    surveyName,
                    groupActivity.id,
                  )
                }
              >
                Remove
              </Button>
            </TableCell>
          </TableRow>
        );
      },
    );

    const activityMenuItems = this.state.allActivities
      .sort()
      .map((activity) => {
        return (
          <MenuItem key={activity} value={activity}>
            {activity}
          </MenuItem>
        );
      });

    var surveyMenuItems = [];
    if (this.state.activity) {
      if (this.state.globalActivities.includes(this.state.activity)) {
        surveyMenuItems = [
          <MenuItem key={globalSurveyId} value={globalSurveyId}>
            <i>{globalSurveyLabel}</i>
          </MenuItem>,
        ];
      } else {
        surveyMenuItems = this.state.allowedSurveys.map((survey) =>
          this.buildSurveyMenuItem(survey),
        );
      }
    }

    return (
      <div style={{ padding: 20 }}>
        <Link to="/userAdmin">← Back to admin</Link>
        <Typography variant="h4" color="inherit">
          Group Details: {this.state.group.name}
        </Typography>
        <Typography variant="h5" color="inherit">
          Group Description: {this.state.group.description}
        </Typography>
        {!this.state.authorisedActivities.includes("SUPER_USER") &&
          !this.state.isLoading && (
            <h1 style={{ color: "red", marginTop: 20 }}>
              YOU ARE NOT AUTHORISED
            </h1>
          )}
        {this.state.authorisedActivities.includes("SUPER_USER") && (
          <>
            <Typography variant="h6" color="inherit">
              Admins
            </Typography>
            <TableContainer component={Paper}>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>Email</TableCell>
                    <TableCell>Action</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>{adminsTableRows}</TableBody>
              </Table>
            </TableContainer>
            <Button
              variant="contained"
              onClick={this.openAddAdminUserDialog}
              style={{ marginTop: 10 }}
            >
              Add Admin User
            </Button>
            <Typography variant="h6" color="inherit" style={{ marginTop: 10 }}>
              Allowed Activities
            </Typography>
            <TableContainer component={Paper}>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>Activity</TableCell>
                    <TableCell>Survey</TableCell>
                    <TableCell />
                  </TableRow>
                </TableHead>
                <TableBody>{groupActivitiesTableRows}</TableBody>
              </Table>
            </TableContainer>
            <Button
              variant="contained"
              onClick={this.openAllowDialog}
              style={{ marginTop: 10 }}
            >
              Allow Activity
            </Button>
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
                  {this.state.activity && (
                    <FormControl required fullWidth={true}>
                      <InputLabel>Survey</InputLabel>
                      <Select
                        onChange={this.onSurveyChange}
                        value={this.state.surveyId}
                        error={this.state.surveyValidationError}
                      >
                        {surveyMenuItems}
                      </Select>
                    </FormControl>
                  )}
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
            <Dialog open={this.state.showAddAdminToGroupDialog}>
              <DialogContent
                style={{ paddingLeft: 30, paddingRight: 30, paddingBottom: 10 }}
              >
                <div>
                  <Autocomplete
                    options={this.state.allUsersAutocompleteOptions}
                    getOptionLabel={(option) => option.email}
                    onChange={this.onNewAdminEmailChange}
                    renderInput={(params) => (
                      <TextField
                        required
                        {...params}
                        error={this.state.newAdminEmailValidationError}
                        label="Email"
                      />
                    )}
                  />{" "}
                </div>
                <div style={{ marginTop: 10 }}>
                  <Button
                    onClick={this.onAddAdmin}
                    variant="contained"
                    style={{ margin: 10 }}
                  >
                    Add Admin
                  </Button>
                  <Button
                    onClick={this.closeAddAdminDialog}
                    variant="contained"
                    style={{ margin: 10 }}
                  >
                    Cancel
                  </Button>
                </div>
              </DialogContent>
            </Dialog>
            <Dialog open={this.state.showRemoveAdminDialog}>
              <DialogTitle id="alert-dialog-title">
                {"Confirm remove admin"}
              </DialogTitle>
              <DialogContent>
                <DialogContentText id="alert-dialog-description">
                  Are you sure you wish to remove group admin{" "}
                  {this.state.adminEmailToRemove}?
                </DialogContentText>
              </DialogContent>
              <DialogActions>
                <Button onClick={this.removeAdmin} color="primary">
                  Yes
                </Button>
                <Button
                  onClick={this.closeRemoveAdminDialog}
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
