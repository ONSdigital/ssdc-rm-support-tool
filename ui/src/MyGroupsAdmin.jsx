import React, { Component } from "react";
import { Link } from "react-router-dom";
import { Typography, Paper } from "@material-ui/core";
import Table from "@material-ui/core/Table";
import TableBody from "@material-ui/core/TableBody";
import TableCell from "@material-ui/core/TableCell";
import TableContainer from "@material-ui/core/TableContainer";
import TableHead from "@material-ui/core/TableHead";
import TableRow from "@material-ui/core/TableRow";
import { errorAlert } from "./Utils";

class MyGroupsAdmin extends Component {
  state = {
    thisUserAdminGroups: [],
    isLoading: true,
  };

  componentDidMount() {
    this.getThisUserAdminGroups();
  }

  getThisUserAdminGroups = async () => {
    const response = await fetch("/api/userGroups/thisUserAdminGroups");

    // TODO: We need more elegant error handling throughout the whole application, but this will at least protect temporarily
    const responseJson = await response.json();
    if (!response.ok) {
      errorAlert(responseJson);
      return [];
    }

    this.setState({ thisUserAdminGroups: responseJson, isLoading: false });
  };

  render() {
    const groupsTableRows = this.state.thisUserAdminGroups.map((group) => {
      return (
        <TableRow key={group.id}>
          <TableCell component="th" scope="row">
            <Link to={`/myGroupUserAdmin?groupId=${group.id}`}>
              {group.name}
            </Link>
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
        <Typography variant="h4" color="inherit" style={{ marginBottom: 20 }}>
          My Groups Admin
        </Typography>
        {this.state.thisUserAdminGroups.length === 0 &&
          !this.state.isLoading && (
            <h1 style={{ color: "red" }}>YOU ARE NOT AUTHORISED</h1>
          )}
        {this.state.thisUserAdminGroups.length > 0 && (
          <>
            <>
              <TableContainer component={Paper} style={{ marginTop: 20 }}>
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
            </>
          </>
        )}
      </div>
    );
  }
}

export default MyGroupsAdmin;
