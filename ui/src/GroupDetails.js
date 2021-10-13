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

class GroupDetails extends Component {
  state = {
    authorisedActivities: [],
    isLoading: true,
    group: {},
    admins: [],
    groupActivities: [],
    allActivities: [],
    allSurveys: [],
    allUsersAutocompleteOptions: [],
    showAllowDialog: false,
    showRemoveDialog: false,
    activity: null,
    activityValidationError: false,
    surveyId: null,
    surveyName: null,
    userGroupPermissionId: null,
    adminIdToRemove: null,
    showRemoveAdminDialog: false,
    showAddAdminToGroupDialog: false,
    newAdminUserId: "",
    newAdminEmailValidationError: false,
  };

  componentDidMount() {
    this.getAuthorisedBackendData();
  }

  componentWillUnmount() {
    clearInterval(this.interval);
  }

  getAuthorisedBackendData = async () => {
    const authorisedActivities = await this.getAuthorisedActivities(); // Only need to do this once; don't refresh it repeatedly as it changes infrequently
    this.getGroup(authorisedActivities);
    this.getAdmins(authorisedActivities);
    this.getAllActivities();
    this.getAllSurveys(authorisedActivities);
    this.getUserGroupPermissions(authorisedActivities);
    this.getAllUsers(authorisedActivities);

    this.interval = setInterval(
      () => this.refreshBackendData(authorisedActivities),
      1000
    );
  };

  refreshBackendData = (authorisedActivities) => {
    this.getUserGroupPermissions(authorisedActivities);
    this.getAdmins(authorisedActivities);
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
      `/api/userGroupAdmins/findByGroup/${this.props.groupId}`
    );

    const responseJson = await response.json();

    this.setState({
      admins: responseJson,
    });
  };

  getAllUsers = async (authorisedActivities) => {
    if (!authorisedActivities.includes("SUPER_USER")) return;

    const response = await fetch("/api/users");

    // TODO: We need more elegant error handling throughout the whole application, but this will at least protect temporarily
    if (!response.ok) {
      return;
    }

    const responseJson = await response.json();

    this.setState({ allUsersAutocompleteOptions: responseJson });
  };

  getUserGroupPermissions = async (authorisedActivities) => {
    if (!authorisedActivities.includes("SUPER_USER")) return;

    const permissionsResponse = await fetch(
      `/api/userGroupPermissions/?groupId=${this.props.groupId}`
    );

    const permissionsJson = await permissionsResponse.json();

    this.setState({
      groupActivities: permissionsJson,
    });
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

    return authorisedActivities;
  };

  getAllActivities = async () => {
    // This is not an RBAC protected endpoint
    const activitiesResponse = await fetch("/api/authorisedActivityTypes");
    const activitiesJson = await activitiesResponse.json();

    this.setState({
      allActivities: activitiesJson,
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

    const newUserGroupPermission = {
      authorisedActivity: this.state.activity,
      groupId: this.props.groupId,
      surveyId: this.state.surveyId,
    };

    await fetch("/api/userGroupPermissions", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(newUserGroupPermission),
    });

    this.setState({ showAllowDialog: false });
  };

  openRemoveAdminDialog = (adminIdToRemove) => {
    this.setState({
      adminIdToRemove: adminIdToRemove,
      showRemoveAdminDialog: true,
    });
  };

  removeAdmin = async () => {
    const response = await fetch(
      `/api/userGroupAdmins/${this.state.adminIdToRemove}`,
      {
        method: "DELETE",
      }
    );

    if (response.ok) {
      this.closeRemoveAdminDialog();
    }
  };

  closeRemoveAdminDialog = () => {
    this.setState({
      adminIdToRemove: null,
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

  render() {
    const adminsTableRows = this.state.admins.map((admin) => (
      <TableRow key={admin.id}>
        <TableCell component="th" scope="row">
          {admin.userEmail}
        </TableCell>
        <TableCell component="th" scope="row">
          <Button
            variant="contained"
            onClick={() => this.openRemoveAdminDialog(admin.id)}
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
          : "All Surveys - Global permission";

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
                    groupActivity.id
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

    const surveyMenuItems = this.state.allSurveys.map((survey) => (
      <MenuItem key={survey.id} value={survey.id}>
        {survey.name}
      </MenuItem>
    ));

    return (
      <div style={{ padding: 20 }}>
        <Link to="/userAdmin">← Back to admin</Link>
        <Typography variant="h4" color="inherit">
          Group Details: {this.state.group.name}
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
                    <TableCell></TableCell>
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
                  Are you sure you wish to remove group admin?
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
