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
import { errorAlert } from "./Utils";

class UserDetails extends Component {
  state = {
    authorisedActivities: [],
    isLoading: true,
    user: {},
    memberOfGroups: [],
    groups: [],
    showGroupDialog: false,
    groupId: null,
    groupValidationError: false,
    showRemoveDialog: false,
    groupName: null,
    userGroupMemberId: null,
  };

  componentDidMount() {
    this.getAuthorisedBackendData();
  }

  getAuthorisedBackendData = async () => {
    const authorisedActivities = await this.getAuthorisedActivities(); // Only need to do this once; don't refresh it repeatedly as it changes infrequently
    this.getUser(authorisedActivities);
    this.getGroups(authorisedActivities);

    this.getUserMemberOf(authorisedActivities);
  };

  getUser = async (authorisedActivities) => {
    if (!authorisedActivities.includes("SUPER_USER")) return;

    const userResponse = await fetch(`/api/users/${this.props.userId}`);

    const userJson = await userResponse.json();

    this.setState({
      user: userJson,
    });
  };

  getUserMemberOf = async (authorisedActivities) => {
    if (!authorisedActivities.includes("SUPER_USER")) return;

    const userMemberOfResponse = await fetch(
      `/api/userGroupMembers?userId=${this.props.userId}`,
    );

    const userMemberOfJson = await userMemberOfResponse.json();

    this.setState({
      memberOfGroups: userMemberOfJson,
    });
  };

  getGroups = async (authorisedActivities) => {
    if (!authorisedActivities.includes("SUPER_USER")) return;

    const groupsResponse = await fetch("/api/userGroups");

    const groupsJson = await groupsResponse.json();

    this.setState({
      groups: groupsJson,
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

  openJoinGroupDialog = () => {
    this.joinGroupInProgress = false;

    this.setState({
      groupValidationError: false,
      showGroupDialog: true,
    });
  };

  closeGroupDialog = () => {
    this.setState({
      showGroupDialog: false,
    });
  };

  openRemoveDialog = (groupName, userGroupMemberId) => {
    this.removeGroupInProgress = false;

    this.setState({
      showRemoveDialog: true,
      groupName: groupName,
      userGroupMemberId: userGroupMemberId,
    });
  };

  closeRemoveDialog = () => {
    this.setState({
      showRemoveDialog: false,
    });
  };

  removeGroup = async () => {
    if (this.removeGroupInProgress) {
      return;
    }

    this.removeGroupInProgress = true;

    await fetch(`/api/userGroupMembers/${this.state.userGroupMemberId}`, {
      method: "DELETE",
    });
    this.closeRemoveDialog();

    this.getAuthorisedBackendData();
  };

  onGroupChange = (event) => {
    this.setState({
      groupId: event.target.value,
    });

    this.getAuthorisedBackendData();
  };

  onJoinGroup = async () => {
    if (this.joinGroupInProgress) {
      return;
    }

    this.joinGroupInProgress = true;

    if (!this.state.groupId) {
      this.setState({
        groupValidationError: true,
      });

      this.joinGroupInProgress = false;
      return;
    }

    const newGroupMembership = {
      userId: this.props.userId,
      groupId: this.state.groupId,
    };

    await fetch("/api/userGroupMembers", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(newGroupMembership),
    });

    this.setState({ showGroupDialog: false });

    this.getAuthorisedBackendData();
  };

  render() {
    const memberOfGroupTableRows = this.state.memberOfGroups.map(
      (memberOfGroup) => {
        return (
          <TableRow key={memberOfGroup.groupId}>
            <TableCell component="th" scope="row">
              <Link to={`/groupDetails?groupId=${memberOfGroup.groupId}`}>
                {memberOfGroup.groupName}
              </Link>
            </TableCell>
            <TableCell component="th" scope="row">
              {memberOfGroup.groupDescription}
            </TableCell>
            <TableCell component="th" scope="row">
              <Button
                variant="contained"
                onClick={() =>
                  this.openRemoveDialog(
                    memberOfGroup.groupName,
                    memberOfGroup.id,
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

    const addGroupMenuItems = this.state.groups
      .filter(
        (group) =>
          !this.state.memberOfGroups
            .map((memberOfGroup) => memberOfGroup.groupId)
            .includes(group.id),
      )
      .sort((a, b) => a.name.localeCompare(b.name)) // Sort by group name alphabetically
      .map((group) => (
        <MenuItem key={group.id} value={group.id}>
          {group.name}
        </MenuItem>
      ));

    return (
      <div style={{ padding: 20 }}>
        <Link to="/userAdmin">← Back to admin</Link>
        <Typography variant="h4" color="inherit">
          User Details: {this.state.user.email}
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
              Groups
            </Typography>
            <TableContainer component={Paper}>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>Group Name</TableCell>
                    <TableCell>Description</TableCell>
                    <TableCell />
                  </TableRow>
                </TableHead>
                <TableBody>{memberOfGroupTableRows}</TableBody>
              </Table>
            </TableContainer>
            <Button
              variant="contained"
              onClick={this.openJoinGroupDialog}
              style={{ marginTop: 10 }}
            >
              Add User to Group
            </Button>
            <Dialog open={this.state.showGroupDialog}>
              <DialogContent
                style={{ paddingLeft: 30, paddingRight: 30, paddingBottom: 10 }}
              >
                <div>
                  <FormControl required fullWidth={true}>
                    <InputLabel>Group</InputLabel>
                    <Select
                      onChange={this.onGroupChange}
                      value={this.state.groupId}
                      error={this.state.groupValidationError}
                    >
                      {addGroupMenuItems}
                    </Select>
                  </FormControl>
                </div>
                <div style={{ marginTop: 10 }}>
                  <Button
                    onClick={this.onJoinGroup}
                    variant="contained"
                    style={{ margin: 10 }}
                  >
                    Join Group
                  </Button>
                  <Button
                    onClick={this.closeGroupDialog}
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
                  Are you sure you wish to remove group: "{this.state.groupName}
                  " from this user?
                </DialogContentText>
              </DialogContent>
              <DialogActions>
                <Button onClick={this.removeGroup} color="primary">
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

export default UserDetails;
