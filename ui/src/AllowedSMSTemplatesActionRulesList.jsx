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
  getActionRuleSmsPackCodesForSurvey,
} from "./Utils";

class AllowedSMSTemplatesActionRulesList extends Component {
  state = {
    authorisedActivities: [],
    actionRuleSmsPackCodes: [],
    allowableActionRuleSmsPackCodes: [],
    smsTemplateToAllow: "",
    smsTemplateValidationError: false,
    allowSmsTemplateError: "",
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

    const actionRuleSmsPackCodes = await getActionRuleSmsPackCodesForSurvey(
      authorisedActivities,
      this.props.surveyId,
    );

    let allowableActionRuleSmsPackCodes = [];

    allSmsFulfilmentPackCodes.forEach((packCode) => {
      if (!actionRuleSmsPackCodes.includes(packCode)) {
        allowableActionRuleSmsPackCodes.push(packCode);
      }
    });

    this.setState({
      allowableActionRuleSmsPackCodes: allowableActionRuleSmsPackCodes,
      actionRuleSmsPackCodes: actionRuleSmsPackCodes,
    });
  };

  onAllowActionRuleSmsTemplate = async () => {
    if (this.allowActionRuleSmsTemplateInProgress) {
      return;
    }

    this.allowActionRuleSmsTemplateInProgress = true;

    if (!this.state.smsTemplateToAllow) {
      this.setState({
        smsTemplateValidationError: true,
      });

      this.allowActionRuleSmsTemplateInProgress = false;
      return;
    }

    const newAllowSmsTemplate = {
      surveyId: this.props.surveyId,
      packCode: this.state.smsTemplateToAllow,
    };

    const response = await fetch("/api/actionRuleSurveySmsTemplates", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(newAllowSmsTemplate),
    });

    if (response.ok) {
      this.setState({
        allowActionRuleSmsTemplateDialogDisplayed: false,
      });
    } else {
      const errorMessage = await response.text();
      this.setState({
        allowSmsTemplateError: errorMessage,
      });
      this.allowActionRuleSmsTemplateInProgress = false;
    }
    this.refreshDataFromBackend(this.state.authorisedActivities);
  };

  openActionRuleSmsTemplateDialog = () => {
    this.allowActionRuleSmsTemplateInProgress = false;
    this.refreshDataFromBackend(this.state.authorisedActivities);
    this.setState({
      allowActionRuleSmsTemplateDialogDisplayed: true,
      smsTemplateToAllow: "",
      smsTemplateValidationError: false,
      allowSmsTemplateError: "",
    });
  };

  onSmsTemplateChange = (event) => {
    this.setState({ smsTemplateToAllow: event.target.value });
  };

  closeAllowActionRuleSmsTemplateDialog = () => {
    this.setState({ allowActionRuleSmsTemplateDialogDisplayed: false });
  };

  render() {
    const actionRuleSmsTemplateTableRows =
      this.state.actionRuleSmsPackCodes.map((packCode) => (
        <TableRow key={packCode}>
          <TableCell component="th" scope="row">
            {packCode}
          </TableCell>
        </TableRow>
      ));

    const actionRuleSmsTemplateMenuItems =
      this.state.allowableActionRuleSmsPackCodes.map((packCode) => (
        <MenuItem key={packCode} value={packCode}>
          {packCode}
        </MenuItem>
      ));

    return (
      <>
        {this.state.authorisedActivities.includes(
          "LIST_ALLOWED_SMS_TEMPLATES_ON_ACTION_RULES",
        ) && (
          <>
            <Typography variant="h6" color="inherit" style={{ marginTop: 20 }}>
              SMS Templates Allowed on Action Rules
            </Typography>
            <TableContainer component={Paper}>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>Pack Code</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>{actionRuleSmsTemplateTableRows}</TableBody>
              </Table>
            </TableContainer>
          </>
        )}

        {this.state.authorisedActivities.includes(
          "ALLOW_SMS_TEMPLATE_ON_ACTION_RULE",
        ) && (
          <Button
            variant="contained"
            onClick={this.openActionRuleSmsTemplateDialog}
            style={{ marginTop: 10 }}
          >
            Allow SMS Template on Action Rule
          </Button>
        )}

        <Dialog open={this.state.allowActionRuleSmsTemplateDialogDisplayed}>
          <DialogContent style={{ padding: 30 }}>
            <div>
              <div>
                <FormControl required fullWidth={true}>
                  <InputLabel>SMS Template</InputLabel>
                  <Select
                    onChange={this.onSmsTemplateChange}
                    value={this.state.smsTemplateToAllow}
                    error={this.state.smsTemplateValidationError}
                  >
                    {actionRuleSmsTemplateMenuItems}
                  </Select>
                </FormControl>
              </div>
              {this.state.allowSmsTemplateError && (
                <div>
                  <p style={{ color: "red" }}>
                    {this.state.allowSmsTemplateError}
                  </p>
                </div>
              )}
              <div style={{ marginTop: 10 }}>
                <Button
                  onClick={this.onAllowActionRuleSmsTemplate}
                  variant="contained"
                  style={{ margin: 10 }}
                >
                  Allow
                </Button>
                <Button
                  onClick={this.closeAllowActionRuleSmsTemplateDialog}
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

export default AllowedSMSTemplatesActionRulesList;
