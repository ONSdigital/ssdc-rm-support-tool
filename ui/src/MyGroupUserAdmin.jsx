import React, { Component } from "react";
import { Link } from "react-router-dom";
import {
  Typography,
  Paper,
  Button,
  Dialog,
  DialogContent,
  TextField,
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
import Autocomplete from "@material-ui/lab/Autocomplete";
import { errorAlert } from "./Utils";

class MyGroupUserAdmin extends Component {
  state = {
    groupName: "",
    groupDescription: "",
    groupMembers: [],
    showAddUserToGroupDialog: false,
    allUsersAutocompleteOptions: [],
    userId: "",
    emailValidationError: false,
    showRemoveDialog: false,
    groupMemberIdToRemove: null,
    groupMemberEmailToRemove: null,
    allUsers: [],
  };

  componentDidMount = async () => {
    this.getGroup(); // Changes infrequently
    const allUsers = await this.getAllUsers(); // Changes infrequently, but expensive to fetch

    this.setState({
      allUsers: allUsers,
    });

    this.refreshBackendData(allUsers);
  };

  refreshBackendData = async (allUsers) => {
    const groupMembers = await this.getGroupMembers();
    this.filterAllUsers(allUsers, groupMembers);
  };

  getGroup = async () => {
    const response = await fetch(`/api/userGroups/${this.props.groupId}`);

    const responseJson = await response.json();

    this.setState({
      groupName: responseJson.name,
      groupDescription: responseJson.description,
    });
  };

  getGroupMembers = async () => {
    const response = await fetch(
      `/api/userGroupMembers/findByGroup/${this.props.groupId}`,
    );

    const responseJson = await response.json();

    this.setState({
      groupMembers: responseJson,
    });

    return responseJson;
  };

  filterAllUsers = (allUsers, groupMembers) => {
    const allUsersAutocompleteOptions = allUsers.filter(
      (user) =>
        !groupMembers
          .map((memberOfGroup) => memberOfGroup.userId)
          .includes(user.id),
    );

    this.setState({
      allUsersAutocompleteOptions: allUsersAutocompleteOptions,
    });
  };

  getAllUsers = async () => {
    const response = await fetch("/api/users");

    // TODO: We need more elegant error handling throughout the whole application, but this will at least protect temporarily
    const responseJson = await response.json();
    if (!response.ok) {
      errorAlert(responseJson);
      return [];
    }

    return responseJson;
  };

  onEmailChange = (_, newValue) => {
    this.setState({
      userId: newValue ? newValue.id : null,
      emailValidationError: newValue ? false : true,
    });
  };

  openAddUserDialog = () => {
    this.addUserInProgress = false;

    this.setState({
      showAddUserToGroupDialog: true,
      userId: "",
      emailValidationError: false,
    });
  };

  closeAddUserDialog = () => {
    this.setState({
      showAddUserToGroupDialog: false,
    });
  };

  onAddUser = async () => {
    if (this.addUserInProgress) {
      return;
    }

    this.addUserInProgress = true;

    if (!this.state.userId) {
      this.setState({ emailValidationError: true });
      this.addUserInProgress = false;
      return;
    }

    const newGroupMembership = {
      userId: this.state.userId,
      groupId: this.props.groupId,
    };

    const response = await fetch("/api/userGroupMembers", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(newGroupMembership),
    });

    if (response.ok) {
      this.setState({ showAddUserToGroupDialog: false });
    }

    this.refreshBackendData(this.state.allUsers);
  };

  openRemoveDialog = (groupMemberToRemove) => {
    this.removeUserFromGroupInProgress = false;

    this.setState({
      groupMemberIdToRemove: groupMemberToRemove.id,
      groupMemberEmailToRemove: groupMemberToRemove.userEmail,
      showRemoveDialog: true,
    });
  };

  closeRemoveDialog = () => {
    this.setState({
      groupMemberIdToRemove: null,
      groupMemberEmailToRemove: null,
      showRemoveDialog: false,
    });
  };

  removeUserFromGroup = async () => {
    if (this.removeUserFromGroupInProgress) {
      return;
    }

    this.removeUserFromGroupInProgress = true;

    const response = await fetch(
      `/api/userGroupMembers/${this.state.groupMemberIdToRemove}`,
      {
        method: "DELETE",
      },
    );

    if (response.ok) {
      this.closeRemoveDialog();
    }

    this.refreshBackendData(this.state.allUsers);
  };

  render() {
    const usersTableRows = this.state.groupMembers.map((groupMember) => (
      <TableRow key={groupMember.id}>
        <TableCell component="th" scope="row">
          {groupMember.userEmail}
        </TableCell>
        <TableCell component="th" scope="row">
          <Button
            variant="contained"
            onClick={() => this.openRemoveDialog(groupMember)}
          >
            Remove
          </Button>
        </TableCell>
      </TableRow>
    ));

    return (
      <div style={{ padding: 20 }}>
        <Link to="/myGroupsAdmin">← Back to my groups</Link>
        <Typography variant="h4" color="inherit">
          My Group: {this.state.groupName}
        </Typography>
        <Typography variant="h5" color="inherit">
          My Group Description: {this.state.groupDescription}
        </Typography>
        <TableContainer component={Paper} style={{ marginTop: 10 }}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Email</TableCell>
                <TableCell>Action</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>{usersTableRows}</TableBody>
          </Table>
        </TableContainer>
        <Button
          variant="contained"
          onClick={this.openAddUserDialog}
          style={{ marginTop: 20 }}
        >
          Add User to Group
        </Button>
        <Dialog open={this.state.showAddUserToGroupDialog}>
          <DialogContent
            style={{ paddingLeft: 30, paddingRight: 30, paddingBottom: 10 }}
          >
            <div>
              <Autocomplete
                options={this.state.allUsersAutocompleteOptions}
                getOptionLabel={(option) => option.email}
                onChange={this.onEmailChange}
                renderInput={(params) => (
                  <TextField
                    required
                    {...params}
                    error={this.state.emailValidationError}
                    label="Email"
                  />
                )}
              />{" "}
            </div>
            <div style={{ marginTop: 10 }}>
              <Button
                onClick={this.onAddUser}
                variant="contained"
                style={{ margin: 10 }}
              >
                Add User to Group
              </Button>
              <Button
                onClick={this.closeAddUserDialog}
                variant="contained"
                style={{ margin: 10 }}
              >
                Cancel
              </Button>
            </div>
          </DialogContent>
        </Dialog>
        <Dialog open={this.state.showRemoveDialog}>
          <DialogTitle id="alert-dialog-title">{"Confirm remove?"}</DialogTitle>
          <DialogContent>
            <DialogContentText id="alert-dialog-description">
              Are you sure you wish to remove{" "}
              {this.state.groupMemberEmailToRemove} from group?
            </DialogContentText>
          </DialogContent>
          <DialogActions>
            <Button onClick={this.removeUserFromGroup} color="primary">
              Yes
            </Button>
            <Button onClick={this.closeRemoveDialog} color="primary" autoFocus>
              No
            </Button>
          </DialogActions>
        </Dialog>
      </div>
    );
  }
}

export default MyGroupUserAdmin;
