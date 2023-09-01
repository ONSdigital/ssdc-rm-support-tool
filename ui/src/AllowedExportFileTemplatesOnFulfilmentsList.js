import React, { Component } from "react";
import "@fontsource/roboto";
import {
  Button,
  Dialog,
  DialogContent,
  Paper,
  Typography,
  MenuItem,
  FormControl,
  InputLabel,
  Select,
} from "@material-ui/core";
import Table from "@material-ui/core/Table";
import TableBody from "@material-ui/core/TableBody";
import TableCell from "@material-ui/core/TableCell";
import TableContainer from "@material-ui/core/TableContainer";
import TableHead from "@material-ui/core/TableHead";
import TableRow from "@material-ui/core/TableRow";
import {
  getAuthorisedActivities,
  getAllExportFilePackCodes,
  getFulfilmentExportFileTemplatesForSurvey,
} from "./Utils";

class AllowedExportFileTemplatesOnFulfilmentsList extends Component {
  state = {
    authorisedActivities: [],
    fulfilmentExportFilePackCodes: [],
    allowableFulfilmentExportFilePackCodes: [],
    allowFulfilmentExportFileTemplateDialogDisplayed: false,
    exportFileTemplateToAllow: "",
    exportFileTemplateValidationError: false,
    allowEmailFulfilmentTemplateDialogDisplayed: true,
    allowExportFileTemplateError: "",
  };

  componentDidMount() {
    this.getAuthorisedBackendData();
  }

  getAuthorisedBackendData = async () => {
    const authorisedActivities = await getAuthorisedActivities();
    this.setState({ authorisedActivities: authorisedActivities });
    this.refreshDataFromBackend(authorisedActivities);
  };

  refreshDataFromBackend = async (authorisedActivities) => {
    const allExportFileFulfilmentPackCodes =
      await getAllExportFilePackCodes(authorisedActivities);

    const fulfilmentExportFileTemplates =
      await getFulfilmentExportFileTemplatesForSurvey(
        authorisedActivities,
        this.props.surveyId,
      );
    const fulfilmentExportFilePackCodes = fulfilmentExportFileTemplates.map(
      (template) => template.packCode,
    );
    let allowableFulfilmentExportFilePackCodes = [];

    allExportFileFulfilmentPackCodes.forEach((packCode) => {
      if (!fulfilmentExportFilePackCodes.includes(packCode)) {
        allowableFulfilmentExportFilePackCodes.push(packCode);
      }
    });

    this.setState({
      fulfilmentExportFilePackCodes: fulfilmentExportFilePackCodes,
      allowableFulfilmentExportFilePackCodes:
        allowableFulfilmentExportFilePackCodes,
    });
  };

  onExportFileTemplateChange = (event) => {
    this.setState({ exportFileTemplateToAllow: event.target.value });
  };

  onAllowFulfilmentExportFileTemplate = async () => {
    if (this.allowFulfilmentExportFileTemplateInProgress) {
      return;
    }

    this.allowFulfilmentExportFileTemplateInProgress = true;

    if (!this.state.exportFileTemplateToAllow) {
      this.setState({
        exportFileTemplateValidationError: true,
      });

      this.allowFulfilmentExportFileTemplateInProgress = false;
      return;
    }

    const newAllowExportFileTemplate = {
      surveyId: this.props.surveyId,
      packCode: this.state.exportFileTemplateToAllow,
    };

    const response = await fetch("/api/fulfilmentSurveyExportFileTemplates", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(newAllowExportFileTemplate),
    });

    if (response.ok) {
      this.setState({
        allowFulfilmentExportFileTemplateDialogDisplayed: false,
      });
    } else {
      const errorMessage = await response.text();
      this.setState({
        allowExportFileTemplateError: errorMessage,
      });
      this.allowFulfilmentExportFileTemplateInProgress = false;
    }
    this.refreshDataFromBackend(this.state.authorisedActivities);
  };

  openFulfilmentExportFileTemplateDialog = () => {
    this.allowFulfilmentExportFileTemplateInProgress = false;
    this.refreshDataFromBackend(this.state.authorisedActivities);
    this.setState({
      allowFulfilmentExportFileTemplateDialogDisplayed: true,
      exportFileTemplateToAllow: "",
      exportFileTemplateValidationError: false,
      allowExportFileTemplateError: "",
    });
  };

  closeAllowFulfilmentExportFileTemplateDialog = () => {
    this.setState({ allowFulfilmentExportFileTemplateDialogDisplayed: false });
  };

  render() {
    const fulfilmentExportFileTemplateMenuItems =
      this.state.allowableFulfilmentExportFilePackCodes.map((packCode) => (
        <MenuItem key={packCode} value={packCode}>
          {packCode}
        </MenuItem>
      ));

    const fulfilmentExportFileTemplateTableRows =
      this.state.fulfilmentExportFilePackCodes.map((packCode) => (
        <TableRow key={packCode}>
          <TableCell component="th" scope="row">
            {packCode}
          </TableCell>
        </TableRow>
      ));

    return (
      <>
        {this.state.authorisedActivities.includes(
          "LIST_ALLOWED_EXPORT_FILE_TEMPLATES_ON_FULFILMENTS",
        ) && (
          <>
            <Typography variant="h6" color="inherit" style={{ marginTop: 20 }}>
              Export File Templates Allowed on Fulfilments
            </Typography>
            <TableContainer component={Paper}>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>Pack Code</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>{fulfilmentExportFileTemplateTableRows}</TableBody>
              </Table>
            </TableContainer>
          </>
        )}
        {this.state.authorisedActivities.includes(
          "ALLOW_EXPORT_FILE_TEMPLATE_ON_FULFILMENT",
        ) && (
          <Button
            variant="contained"
            onClick={this.openFulfilmentExportFileTemplateDialog}
            style={{ marginTop: 10 }}
          >
            Allow Export File Template on Fulfilment
          </Button>
        )}

        <Dialog
          open={this.state.allowFulfilmentExportFileTemplateDialogDisplayed}
        >
          <DialogContent style={{ padding: 30 }}>
            <div>
              <div>
                <FormControl required fullWidth={true}>
                  <InputLabel>Export File Template</InputLabel>
                  <Select
                    onChange={this.onExportFileTemplateChange}
                    value={this.state.exportFileTemplateToAllow}
                    error={this.state.exportFileTemplateValidationError}
                  >
                    {fulfilmentExportFileTemplateMenuItems}
                  </Select>
                </FormControl>
              </div>
              {this.state.allowExportFileTemplateError && (
                <div>
                  <p style={{ color: "red" }}>
                    {this.state.allowExportFileTemplateError}
                  </p>
                </div>
              )}
              <div style={{ marginTop: 10 }}>
                <Button
                  onClick={this.onAllowFulfilmentExportFileTemplate}
                  variant="contained"
                  style={{ margin: 10 }}
                >
                  Allow
                </Button>
                <Button
                  onClick={this.closeAllowFulfilmentExportFileTemplateDialog}
                  variant="contained"
                  style={{ margin: 10 }}
                >
                  Cancel
                </Button>
              </div>
            </div>
          </DialogContent>
        </Dialog>
      </>
    );
  }
}

export default AllowedExportFileTemplatesOnFulfilmentsList;
