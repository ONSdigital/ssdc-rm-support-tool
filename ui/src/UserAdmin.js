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

class UserAdmin extends Component {
  state = {
    authorisedActivities: [],
    isLoading: true,
    users: [],
    groups: [],
    showUserDialog: false,
    email: "",
    emailValidationError: false,
    showGroupDialog: false,
    groupName: "",
    groupNameValidationError: false,
    groupNameValidationMessage: null,
  };

  componentDidMount() {
    this.getAuthorisedBackendData();
  }

  componentWillUnmount() {
    clearInterval(this.interval);
  }

  getAuthorisedBackendData = async () => {
    const authorisedActivities = await this.getAuthorisedActivities(); // Only need to do this once; don't refresh it repeatedly as it changes infrequently
    this.refreshDataFromBackend(authorisedActivities);

    this.interval = setInterval(
      () => this.refreshDataFromBackend(authorisedActivities),
      1000
    );
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

  openCreateUserDialog = () => {
    this.setState({
      email: "",
      emailValidationError: false,
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
    });
  };

  onCreateUser = async () => {
    if (!this.state.email.trim()) {
      this.setState({ emailValidationError: true });
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
  };

  openCreateGroupDialog = () => {
    this.setState({
      groupName: "",
      groupNameValidationError: false,
      groupNameValidationMessage: null,
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
  };

  onCreateGroup = async () => {
    if (!this.state.groupName.trim()) {
      this.setState({
        groupNameValidationError: true,
        groupNameValidationMessage: "Group name is required",
      });
      return;
    }

    if (
      this.state.groups
        .map((group) => group.name)
        .includes(this.state.groupName)
    ) {
      this.setState({
        groupNameValidationError: true,
        groupNameValidationMessage: "Group already exists",
      });
      return;
    }

    const newGroup = {
      name: this.state.groupName,
    };

    await fetch("/api/userGroups", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(newGroup),
    });

    this.setState({ showGroupDialog: false });
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
                helperText={this.state.groupNameValidationMessage}
                value={this.state.groupName}
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
