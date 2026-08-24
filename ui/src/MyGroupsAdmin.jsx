import React, { Component } from "react";
import { Link } from "react-router";
import { Typography, Paper } from "@mui/material";
import Table from "@mui/material/Table";
import TableBody from "@mui/material/TableBody";
import TableCell from "@mui/material/TableCell";
import TableContainer from "@mui/material/TableContainer";
import TableHead from "@mui/material/TableHead";
import TableRow from "@mui/material/TableRow";
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
