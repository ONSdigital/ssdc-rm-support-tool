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
  getAllEmailTemplates,
  getEmailFulfilmentTemplates,
} from "./Utils";

class AllowedEmailTemplatesOnFulfilments extends Component {
  state = {
    authorisedActivities: [],
    allowEmailFulfilmentTemplateDialogDisplayed: false,
    emailFulfilmentTemplates: [],
    allowableEmailFulfilmentTemplates: [],
    exportFileTemplateToAllow: "",
    exportFileTemplateValidationError: false,
    allowExportFileTemplateError: "",
  };

  componentDidMount() {
    this.getAuthorisedBackendData();
  }

  componentWillUnmount() {
    clearInterval(this.interval);
  }

  getAuthorisedBackendData = async () => {
    const authorisedActivities = await getAuthorisedActivities();
    this.setState({ authorisedActivities: authorisedActivities });
    this.refreshDataFromBackend(authorisedActivities);

    this.interval = setInterval(
      () => this.refreshDataFromBackend(authorisedActivities),
      1000
    );
  };

  refreshDataFromBackend = async (authorisedActivities) => {
    const allEmailFulfilmentTemplates = await getAllEmailTemplates(
      authorisedActivities
    );

    const emailFulfilmentTemplates = await getEmailFulfilmentTemplates(
      authorisedActivities,
      this.props.surveyId
    );

    let allowableEmailFulfilmentTemplates = [];

    allEmailFulfilmentTemplates.forEach((packCode) => {
      if (!emailFulfilmentTemplates.includes(packCode)) {
        allowableEmailFulfilmentTemplates.push(packCode);
      }
    });

    this.setState({
      emailFulfilmentTemplates: emailFulfilmentTemplates,
      allowableEmailFulfilmentTemplates: allowableEmailFulfilmentTemplates,
    });
  };

  openEmailFulfilmentTemplateDialog = () => {
    this.allowEmailFulfilmentTemplateInProgress = false;

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
  };

  closeAllowEmailFulfilmentTemplateDialog = () => {
    this.setState({ allowEmailFulfilmentTemplateDialogDisplayed: false });
  };

  onEmailTemplateChange = (event) => {
    this.setState({ emailTemplateToAllow: event.target.value });
  };

  render() {
    const emailFulfilmentTemplateTableRows =
      this.state.emailFulfilmentTemplates.map((emailTemplate) => (
        <TableRow key={emailTemplate}>
          <TableCell component="th" scope="row">
            {emailTemplate}
          </TableCell>
        </TableRow>
      ));

    const emailFulfilmentTemplateMenuItems =
      this.state.allowableEmailFulfilmentTemplates.map((packCode) => (
        <MenuItem key={packCode} value={packCode}>
          {packCode}
        </MenuItem>
      ));

    return (
      <>
        {this.state.authorisedActivities.includes(
          "LIST_ALLOWED_EMAIL_TEMPLATES_ON_FULFILMENTS"
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
          "ALLOW_EMAIL_TEMPLATE_ON_FULFILMENT"
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
