import React, { Component } from "react";
import { Link } from "react-router-dom";
import {
  Typography,
  Paper,
  Button,
  Dialog,
  DialogContent,
  TextField,
} from "@material-ui/core";
import Table from "@material-ui/core/Table";
import TableBody from "@material-ui/core/TableBody";
import TableCell from "@material-ui/core/TableCell";
import TableContainer from "@material-ui/core/TableContainer";
import TableHead from "@material-ui/core/TableHead";
import TableRow from "@material-ui/core/TableRow";
import { errorAlert } from "./Utils";

class UserAdmin extends Component {
  state = {
    authorisedActivities: [],
    isLoading: true,
    users: [],
    groups: [],
    showUserDialog: false,
    email: "",
    emailValidationError: "",
    showGroupDialog: false,
    groupName: "",
    groupNameValidationError: "",
    groupDescription: "",
  };

  componentDidMount() {
    this.getAuthorisedBackendData();
  }

  getAuthorisedBackendData = async () => {
    const authorisedActivities = await this.getAuthorisedActivities(); // Only need to do this once; don't refresh it repeatedly as it changes infrequently
    this.refreshDataFromBackend(authorisedActivities);
  };

  refreshDataFromBackend = (authorisedActivities) => {
    this.getUsers(authorisedActivities);
    this.getGroups(authorisedActivities);
  };

  getUsers = async (authorisedActivities) => {
    if (!authorisedActivities.includes("SUPER_USER")) return;

    const usersResponse = await fetch("/api/users");

    const usersJson = await usersResponse.json();

    this.setState({
      users: usersJson,
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

  openCreateUserDialog = () => {
    this.createUserInProgress = false;

    this.setState({
      email: "",
      emailValidationError: "",
      showUserDialog: true,
    });
  };

  closeUserDialog = () => {
    this.setState({
      showUserDialog: false,
    });
  };

  onEmailChange = (event) => {
    this.setState({
      email: event.target.value,
      emailValidationError: "",
    });

    this.getAuthorisedBackendData();
  };

  validateEmail = (email) => {
    const emailRegex =
      /^(([^<>()[\]\\.,;:\s@"]+(\.[^<>()[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
    return emailRegex.test(email);
  };

  onCreateUser = async () => {
    if (this.createUserInProgress) {
      return;
    }

    this.createUserInProgress = true;

    if (
      this.state.users
        .map((user) => user.email.toLowerCase())
        .includes(this.state.email.toLowerCase())
    ) {
      this.setState({
        emailValidationError: "User already exists",
      });
      this.createUserInProgress = false;
      return;
    }

    if (!this.validateEmail(this.state.email)) {
      this.setState({
        emailValidationError: "Must be a valid email address",
      });
      this.createUserInProgress = false;
      return;
    }

    const newUser = {
      email: this.state.email,
    };

    await fetch("/api/users", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(newUser),
    });

    this.setState({ showUserDialog: false });

    this.getAuthorisedBackendData();
  };

  openCreateGroupDialog = () => {
    this.createGroupInProgress = false;

    this.setState({
      groupName: "",
      groupNameValidationError: "",
      groupDescription: "",
      showGroupDialog: true,
    });
  };

  closeGroupDialog = () => {
    this.setState({
      showGroupDialog: false,
    });
  };

  onGroupNameChange = (event) => {
    this.setState({
      groupName: event.target.value,
    });

    this.getAuthorisedBackendData();
  };

  onGroupDescriptionChange = (event) => {
    if (event.target.value.length > 255) {
      return;
    }

    this.setState({
      groupDescription: event.target.value,
    });

    this.getAuthorisedBackendData();
  };

  onCreateGroup = async () => {
    if (this.createGroupInProgress) {
      return;
    }

    this.createGroupInProgress = true;

    if (!this.state.groupName.trim()) {
      this.setState({
        groupNameValidationError: "Group name is required",
      });
      this.createGroupInProgress = false;
      return;
    }

    if (
      this.state.groups
        .map((group) => group.name)
        .includes(this.state.groupName)
    ) {
      this.setState({
        groupNameValidationError: "Group already exists",
      });
      this.createGroupInProgress = false;
      return;
    }

    const newGroup = {
      name: this.state.groupName,
      description: this.state.groupDescription,
    };

    await fetch("/api/userGroups", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(newGroup),
    });

    this.setState({ showGroupDialog: false });

    this.getAuthorisedBackendData();
  };

  render() {
    const usersTableRows = this.state.users.map((user) => (
      <TableRow key={user.email}>
        <TableCell component="th" scope="row">
          <Link to={`/userDetails?userId=${user.id}`}>{user.email}</Link>
        </TableCell>
      </TableRow>
    ));

    const groupsTableRows = this.state.groups.map((group) => {
      return (
        <TableRow key={group.id}>
          <TableCell component="th" scope="row">
            <Link to={`/groupDetails?groupId=${group.id}`}>{group.name}</Link>
          </TableCell>
          <TableCell component="th" scope="row">
            {group.description}
          </TableCell>
        </TableRow>
      );
    });

    return (
      <div style={{ padding: 20 }}>
        <Link to="/">← Back to home</Link>
        <Typography variant="h4" color="inherit">
          User Admin
        </Typography>
        {!this.state.authorisedActivities.includes("SUPER_USER") &&
          !this.state.isLoading && (
            <h1 style={{ color: "red", marginTop: 20 }}>
              YOU ARE NOT AUTHORISED
            </h1>
          )}
        {this.state.authorisedActivities.includes("SUPER_USER") && (
          <>
            <>
              <Typography variant="h6" color="inherit">
                Users
              </Typography>
              <TableContainer component={Paper}>
                <Table>
                  <TableHead>
                    <TableRow>
                      <TableCell>Email</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>{usersTableRows}</TableBody>
                </Table>
              </TableContainer>
              <Button
                variant="contained"
                onClick={this.openCreateUserDialog}
                style={{ marginTop: 10 }}
              >
                Create User
              </Button>
            </>
            <>
              <Typography
                variant="h6"
                color="inherit"
                style={{ marginTop: 10 }}
              >
                Groups
              </Typography>
              <TableContainer component={Paper}>
                <Table>
                  <TableHead>
                    <TableRow>
                      <TableCell>Name</TableCell>
                      <TableCell>Description</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>{groupsTableRows}</TableBody>
                </Table>
              </TableContainer>
              <Button
                variant="contained"
                onClick={this.openCreateGroupDialog}
                style={{ marginTop: 10 }}
              >
                Create Group
              </Button>
            </>
          </>
        )}
        <Dialog open={this.state.showUserDialog}>
          <DialogContent
            style={{ paddingLeft: 30, paddingRight: 30, paddingBottom: 10 }}
          >
            <div>
              <TextField
                required
                fullWidth={true}
                label="Email"
                onChange={this.onEmailChange}
                error={this.state.emailValidationError}
                helperText={this.state.emailValidationError}
                value={this.state.email}
              />
            </div>
            <div style={{ marginTop: 10 }}>
              <Button
                onClick={this.onCreateUser}
                variant="contained"
                style={{ margin: 10 }}
              >
                Create User
              </Button>
              <Button
                onClick={this.closeUserDialog}
                variant="contained"
                style={{ margin: 10 }}
              >
                Cancel
              </Button>
            </div>
          </DialogContent>
        </Dialog>
        <Dialog open={this.state.showGroupDialog}>
          <DialogContent
            style={{ paddingLeft: 30, paddingRight: 30, paddingBottom: 10 }}
          >
            <div>
              <TextField
                required
                fullWidth={true}
                label="Group name"
                onChange={this.onGroupNameChange}
                error={this.state.groupNameValidationError}
                helperText={this.state.groupNameValidationError}
                value={this.state.groupName}
              />
              <TextField
                fullWidth={true}
                label="Description"
                onChange={this.onGroupDescriptionChange}
                value={this.state.groupDescription}
              />
            </div>
            <div style={{ marginTop: 10 }}>
              <Button
                onClick={this.onCreateGroup}
                variant="contained"
                style={{ margin: 10 }}
              >
                Create Group
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
      </div>
    );
  }
}

export default UserAdmin;
