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
  getAllSmsPackCodes,
  getSmsFulfilmentTemplatesForSurvey,
} from "./Utils";

class AllowedSMSTemplatesOnFulfilmentsList extends Component {
  state = {
    authorisedActivities: [],
    allowSmsFulfilmentTemplateDialogDisplayed: false,
    smsFulfilmentPackCodes: [],
    allowableSmsFulfilmentPackCodes: [],
    smsTemplateToAllow: "",
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
    const allSmsFulfilmentPackCodes =
      await getAllSmsPackCodes(authorisedActivities);

    const smsFulfilmentTemplates = await getSmsFulfilmentTemplatesForSurvey(
      authorisedActivities,
      this.props.surveyId,
    );
    const smsFulfilmentPackCodes = smsFulfilmentTemplates.map(
      (template) => template.packCode,
    );

    let allowableSmsFulfilmentPackCodes = [];

    allSmsFulfilmentPackCodes.forEach((packCode) => {
      if (!smsFulfilmentPackCodes.includes(packCode)) {
        allowableSmsFulfilmentPackCodes.push(packCode);
      }
    });

    this.setState({
      smsFulfilmentPackCodes: smsFulfilmentPackCodes,
      allowableSmsFulfilmentPackCodes: allowableSmsFulfilmentPackCodes,
    });
  };

  onAllowSmsFulfilmentTemplate = async () => {
    if (this.allowSmsFulfilmentTemplateInProgress) {
      return;
    }

    this.allowSmsFulfilmentTemplateInProgress = true;

    if (!this.state.smsTemplateToAllow) {
      this.setState({
        exportFileTemplateValidationError: true,
      });

      this.allowSmsFulfilmentTemplateInProgress = false;
      return;
    }

    const newAllowSmsTemplate = {
      surveyId: this.props.surveyId,
      packCode: this.state.smsTemplateToAllow,
    };

    const response = await fetch("/api/fulfilmentSurveySmsTemplates", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(newAllowSmsTemplate),
    });

    if (response.ok) {
      this.setState({ allowSmsFulfilmentTemplateDialogDisplayed: false });
    } else {
      const errorMessage = await response.text();
      this.setState({
        allowExportFileTemplateError: errorMessage,
      });
      this.allowSmsFulfilmentTemplateInProgress = false;
    }
    this.refreshDataFromBackend(this.state.authorisedActivities);
  };

  closeAllowSmsFulfilmentTemplateDialog = () => {
    this.setState({ allowSmsFulfilmentTemplateDialogDisplayed: false });
  };

  onSmsTemplateChange = (event) => {
    this.setState({ smsTemplateToAllow: event.target.value });
  };

  openSmsFulfilmentTemplateDialog = () => {
    this.allowSmsFulfilmentTemplateInProgress = false;
    this.refreshDataFromBackend(this.state.authorisedActivities);
    this.setState({
      allowSmsFulfilmentTemplateDialogDisplayed: true,
      smsTemplateToAllow: "",
      exportFileTemplateValidationError: false,
      allowExportFileTemplateError: "",
    });
  };

  render() {
    const smsFulfilmentTemplateTableRows =
      this.state.smsFulfilmentPackCodes.map((packCode) => (
        <TableRow key={packCode}>
          <TableCell component="th" scope="row">
            {packCode}
          </TableCell>
        </TableRow>
      ));

    const smsFulfilmentTemplateMenuItems =
      this.state.allowableSmsFulfilmentPackCodes.map((packCode) => (
        <MenuItem key={packCode} value={packCode}>
          {packCode}
        </MenuItem>
      ));

    return (
      <>
        {this.state.authorisedActivities.includes(
          "LIST_ALLOWED_SMS_TEMPLATES_ON_FULFILMENTS",
        ) && (
          <>
            <Typography variant="h6" color="inherit" style={{ marginTop: 20 }}>
              SMS Templates Allowed on Fulfilments
            </Typography>
            <TableContainer component={Paper}>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>Pack Code</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>{smsFulfilmentTemplateTableRows}</TableBody>
              </Table>
            </TableContainer>
          </>
        )}

        {this.state.authorisedActivities.includes(
          "ALLOW_SMS_TEMPLATE_ON_FULFILMENT",
        ) && (
          <Button
            variant="contained"
            onClick={this.openSmsFulfilmentTemplateDialog}
            style={{ marginTop: 10 }}
          >
            Allow SMS Template on Fulfilment
          </Button>
        )}

        <Dialog open={this.state.allowSmsFulfilmentTemplateDialogDisplayed}>
          <DialogContent style={{ padding: 30 }}>
            <div>
              <div>
                <FormControl required fullWidth={true}>
                  <InputLabel>SMS Template</InputLabel>
                  <Select
                    onChange={this.onSmsTemplateChange}
                    value={this.state.smsTemplateToAllow}
                    error={this.state.exportFileTemplateValidationError}
                  >
                    {smsFulfilmentTemplateMenuItems}
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
                  onClick={this.onAllowSmsFulfilmentTemplate}
                  variant="contained"
                  style={{ margin: 10 }}
                >
                  Allow
                </Button>
                <Button
                  onClick={this.closeAllowSmsFulfilmentTemplateDialog}
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

export default AllowedSMSTemplatesOnFulfilmentsList;
