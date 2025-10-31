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
  getAllEmailPackCodes,
  getEmailFulfilmentTemplatesForSurvey,
} from "./Utils";

class AllowedEmailTemplatesOnFulfilments extends Component {
  state = {
    authorisedActivities: [],
    allowEmailFulfilmentTemplateDialogDisplayed: false,
    emailFulfilmentPackCodes: [],
    allowableEmailFulfilmentPackCodes: [],
    exportFileTemplateToAllow: "",
    exportFileTemplateValidationError: false,
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
    const allEmailFulfilmentPackCodes =
      await getAllEmailPackCodes(authorisedActivities);

    const emailFulfilmentTemplates = await getEmailFulfilmentTemplatesForSurvey(
      authorisedActivities,
      this.props.surveyId,
    );

    const emailFulfilmentPackCodes = emailFulfilmentTemplates.map(
      (template) => template.packCode,
    );

    let allowableEmailFulfilmentPackCodes = [];

    allEmailFulfilmentPackCodes.forEach((packCode) => {
      if (!emailFulfilmentPackCodes.includes(packCode)) {
        allowableEmailFulfilmentPackCodes.push(packCode);
      }
    });

    this.setState({
      emailFulfilmentPackCodes: emailFulfilmentPackCodes,
      allowableEmailFulfilmentPackCodes: allowableEmailFulfilmentPackCodes,
    });
  };

  openEmailFulfilmentTemplateDialog = () => {
    this.allowEmailFulfilmentTemplateInProgress = false;
    this.refreshDataFromBackend(this.state.authorisedActivities);
    this.setState({
      allowEmailFulfilmentTemplateDialogDisplayed: true,
      emailTemplateToAllow: "",
      exportFileTemplateValidationError: false,
      allowExportFileTemplateError: "",
    });
  };

  onAllowEmailFulfilmentTemplate = async () => {
    if (this.allowEmailFulfilmentTemplateInProgress) {
      return;
    }

    this.allowEmailFulfilmentTemplateInProgress = true;

    if (!this.state.emailTemplateToAllow) {
      this.setState({
        exportFileTemplateValidationError: true,
      });

      this.allowEmailFulfilmentTemplateInProgress = false;
      return;
    }

    const newAllowEmailTemplate = {
      surveyId: this.props.surveyId,
      packCode: this.state.emailTemplateToAllow,
    };

    const response = await fetch("/api/fulfilmentSurveyEmailTemplates", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(newAllowEmailTemplate),
    });

    if (response.ok) {
      this.setState({ allowEmailFulfilmentTemplateDialogDisplayed: false });
    } else {
      const errorMessage = await response.text();
      this.setState({
        allowExportFileTemplateError: errorMessage,
      });
      this.allowEmailFulfilmentTemplateInProgress = false;
    }
    this.refreshDataFromBackend(this.state.authorisedActivities);
  };

  closeAllowEmailFulfilmentTemplateDialog = () => {
    this.setState({ allowEmailFulfilmentTemplateDialogDisplayed: false });
  };

  onEmailTemplateChange = (event) => {
    this.setState({ emailTemplateToAllow: event.target.value });
  };

  render() {
    const emailFulfilmentTemplateTableRows =
      this.state.emailFulfilmentPackCodes.map((packCode) => (
        <TableRow key={packCode}>
          <TableCell component="th" scope="row">
            {packCode}
          </TableCell>
        </TableRow>
      ));

    const emailFulfilmentTemplateMenuItems =
      this.state.allowableEmailFulfilmentPackCodes.map((packCode) => (
        <MenuItem key={packCode} value={packCode}>
          {packCode}
        </MenuItem>
      ));

    return (
      <>
        {this.state.authorisedActivities.includes(
          "LIST_ALLOWED_EMAIL_TEMPLATES_ON_FULFILMENTS",
        ) && (
          <>
            <Typography variant="h6" color="inherit" style={{ marginTop: 20 }}>
              Email Templates Allowed on Fulfilments
            </Typography>
            <TableContainer component={Paper}>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>Pack Code</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>{emailFulfilmentTemplateTableRows}</TableBody>
              </Table>
            </TableContainer>
          </>
        )}

        {this.state.authorisedActivities.includes(
          "ALLOW_EMAIL_TEMPLATE_ON_FULFILMENT",
        ) && (
          <Button
            variant="contained"
            onClick={this.openEmailFulfilmentTemplateDialog}
            style={{ marginTop: 10 }}
          >
            Allow Email Template on Fulfilment
          </Button>
        )}
        <Dialog open={this.state.allowEmailFulfilmentTemplateDialogDisplayed}>
          <DialogContent style={{ padding: 30 }}>
            <div>
              <div>
                <FormControl required fullWidth={true}>
                  <InputLabel>Email Template</InputLabel>
                  <Select
                    onChange={this.onEmailTemplateChange}
                    value={this.state.emailTemplateToAllow}
                    error={this.state.exportFileTemplateValidationError}
                  >
                    {emailFulfilmentTemplateMenuItems}
                  </Select>
                </FormControl>
              </div>
              {this.state.allowExportFileTemplateError && (
                <p style={{ color: "red" }}>
                  {this.state.allowExportFileTemplateError}
                </p>
              )}
              <div style={{ marginTop: 10 }}>
                <Button
                  onClick={this.onAllowEmailFulfilmentTemplate}
                  variant="contained"
                  style={{ margin: 10 }}
                >
                  Allow
                </Button>
                <Button
                  onClick={this.closeAllowEmailFulfilmentTemplateDialog}
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

export default AllowedEmailTemplatesOnFulfilments;
