import React, { Component } from "react";
import { Link } from "react-router-dom";
import { Paper } from "@material-ui/core";
import Table from "@material-ui/core/Table";
import TableBody from "@material-ui/core/TableBody";
import TableCell from "@material-ui/core/TableCell";
import TableContainer from "@material-ui/core/TableContainer";
import TableHead from "@material-ui/core/TableHead";
import TableRow from "@material-ui/core/TableRow";

class UserAdmin extends Component {
  state = {
    authorisedActivities: [],
    users: [],
    groups: [],
  };

  componentDidMount() {
    this.getAuthorisedActivities(); // Only need to do this once; don't refresh it repeatedly as it changes infrequently
    this.refreshDataFromBackend();

    this.interval = setInterval(() => this.refreshDataFromBackend(), 1000);
  }

  componentWillUnmount() {
    clearInterval(this.interval);
  }

  refreshDataFromBackend = () => {
    this.getUsers();
    this.getGroups();
  };

  getUsers = async () => {
    const authResponse = await fetch("/api/users");

    const usersJson = await authResponse.json();

    this.setState({
      users: usersJson._embedded.users,
    });
  };

  getGroups = async () => {
    const authResponse = await fetch("/api/userGroups");

    const groupsJson = await authResponse.json();

    this.setState({
      groups: groupsJson._embedded.userGroups,
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
    });
  };

  render() {
    const usersTableRows = this.state.users.map((user) => {
      const userId = user._links.self.href.split("/").pop();

      return (
        <TableRow key={user.email}>
          <TableCell component="th" scope="row">
            <Link to={`/misterFlibbles`}>{user.email}</Link>
          </TableCell>
        </TableRow>
      );
    });

    const groupsTableRows = this.state.groups.map((group, index) => {
      const groupId = group._links.self.href.split("/").pop();

      return (
        <TableRow key={groupId}>
          <TableCell component="th" scope="row">
            <Link to={`/misterFlibbles`}>{group.name}</Link>
          </TableCell>
        </TableRow>
      );
    });

    return (
      <div style={{ padding: 20 }}>
        <Link to="/">← Back to home</Link>
        <h1>User Admin</h1>
        {!this.state.authorisedActivities.includes("SUPER_USER") && (
          <h1 style={{ color: "red" }}>YOU ARE NOT AUTHORISED</h1>
        )}
        {this.state.authorisedActivities.includes("SUPER_USER") && (
          <>
            <>
              <TableContainer component={Paper} style={{ marginTop: 20 }}>
                <Table>
                  <TableHead>
                    <TableRow>
                      <TableCell>Email</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>{usersTableRows}</TableBody>
                </Table>
              </TableContainer>
            </>
            <>
              <TableContainer component={Paper} style={{ marginTop: 20 }}>
                <Table>
                  <TableHead>
                    <TableRow>
                      <TableCell>Name</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>{groupsTableRows}</TableBody>
                </Table>
              </TableContainer>
            </>
          </>
        )}
      </div>
    );
  }
}

export default UserAdmin;
