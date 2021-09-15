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
    this.getAuthorisedActivities(); // Only need to do this once; don't refresh it repeatedly as it changes infrequently
    this.getUser();
    this.getGroups();

    this.getUserMemberOf();

    this.interval = setInterval(() => this.getUserMemberOf(), 1000);
  }

  componentWillUnmount() {
    clearInterval(this.interval);
  }

  getUser = async () => {
    const userResponse = await fetch(`/api/users/${this.props.userId}`);

    const userJson = await userResponse.json();

    this.setState({
      user: userJson,
    });
  };

  getUserMemberOf = async () => {
    const userMemberOfResponse = await fetch(
      `/api/users/${this.props.userId}/memberOf`
    );

    const userMemberOfJson = await userMemberOfResponse.json();

    const memberOfGroups = await this.getUserGroupMemberships(
      userMemberOfJson._embedded.userGroupMembers
    );

    this.setState({
      memberOfGroups: memberOfGroups,
    });
  };

  getGroups = async () => {
    const groupsResponse = await fetch("/api/userGroups");

    const groupsJson = await groupsResponse.json();

    this.setState({
      groups: groupsJson._embedded.userGroups,
    });
  };

  getGroup = async (userGroupMemberId) => {
    const groupResponse = await fetch(
      `/api/userGroupMembers/${userGroupMemberId}/group`
    );

    return await groupResponse.json();
  };

  getUserGroupMemberships = async (userGroupMembers) => {
    let groupMemberships = [];

    for (const userGroupMember of userGroupMembers) {
      const userGroupMemberId = userGroupMember._links.self.href
        .split("/")
        .pop();
      const group = await this.getGroup(userGroupMemberId);
      const groupId = group._links.self.href.split("/").pop();

      groupMemberships.push({
        groupId: groupId,
        name: group.name,
        userGroupMemberId: userGroupMemberId,
      });
    }

    return groupMemberships;
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

  openJoinGroupDialog = () => {
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
    await fetch(`/api/userGroupMembers/${this.state.userGroupMemberId}`, {
      method: "DELETE",
    });
    this.closeRemoveDialog();
  };

  onGroupChange = (event) => {
    this.setState({
      groupId: event.target.value,
    });
  };

  onJoinGroup = async () => {
    if (!this.state.groupId) {
      this.setState({
        groupValidationError: true,
      });

      return;
    }

    const newGroupMembership = {
      id: uuidv4(),
      user: `users/${this.props.userId}`,
      group: `userGroups/${this.state.groupId}`,
    };

    await fetch("/api/userGroupMembers", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(newGroupMembership),
    });

    this.setState({ showGroupDialog: false });
  };

  render() {
    const memberOfGroupTableRows = this.state.memberOfGroups.map(
      (memberOfGroup) => {
        return (
          <TableRow key={memberOfGroup.groupId}>
            <TableCell component="th" scope="row">
              <Link to={`/groupDetails?groupId=${memberOfGroup.groupId}`}>
                {memberOfGroup.name}
              </Link>
            </TableCell>
            <TableCell component="th" scope="row">
              <Button
                variant="contained"
                onClick={() =>
                  this.openRemoveDialog(
                    memberOfGroup.name,
                    memberOfGroup.userGroupMemberId
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

    const groupMenuItems = this.state.groups.map((group) => {
      const groupId = group._links.self.href.split("/").pop();

      return (
        <MenuItem key={groupId} value={groupId}>
          {group.name}
        </MenuItem>
      );
    });

    return (
      <div style={{ padding: 20 }}>
        <Link to="/userAdmin">← Back to admin</Link>
        <Typography variant="h4" color="inherit" style={{ marginBottom: 20 }}>
          User Details: {this.state.user.email}
        </Typography>
        {!this.state.authorisedActivities.includes("SUPER_USER") &&
          !this.state.isLoading && (
            <h1 style={{ color: "red" }}>YOU ARE NOT AUTHORISED</h1>
          )}
        {this.state.authorisedActivities.includes("SUPER_USER") && (
          <>
            <Button
              variant="contained"
              onClick={this.openJoinGroupDialog}
              style={{ marginTop: 20 }}
            >
              Add User to Group
            </Button>
            <TableContainer component={Paper} style={{ marginTop: 20 }}>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>Group Name</TableCell>
                    <TableCell></TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>{memberOfGroupTableRows}</TableBody>
              </Table>
            </TableContainer>
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
                      {groupMenuItems}
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
