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
  getActionRuleEmailPackCodesForSurvey,
} from "./Utils";

class AllowedEmailTemplatesOnActionRulesList extends Component {
  state = {
    authorisedActivities: [],
    actionRuleEmailPackCodes: [],
    allowableActionRuleEmailPackCodes: [],
    emailTemplateToAllow: "",
    emailTemplateValidationError: false,
    allowEmailTemplateError: "",
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

    const actionRuleEmailPackCodes = await getActionRuleEmailPackCodesForSurvey(
      authorisedActivities,
      this.props.surveyId,
    );

    let allowableActionRuleEmailPackCodes = [];

    allEmailFulfilmentPackCodes.forEach((packCode) => {
      if (!actionRuleEmailPackCodes.includes(packCode)) {
        allowableActionRuleEmailPackCodes.push(packCode);
      }
    });

    this.setState({
      actionRuleEmailPackCodes: actionRuleEmailPackCodes,
      allowableActionRuleEmailPackCodes: allowableActionRuleEmailPackCodes,
    });
  };

  onEmailTemplateChange = (event) => {
    this.setState({ emailTemplateToAllow: event.target.value });
  };

  onAllowActionRuleEmailTemplate = async () => {
    if (this.allowActionRuleEmailTemplateInProgress) {
      return;
    }

    this.allowActionRuleEmailTemplateInProgress = true;

    if (!this.state.emailTemplateToAllow) {
      this.setState({
        emailTemplateValidationError: true,
      });

      this.allowActionRuleEmailTemplateInProgress = false;
      return;
    }

    const newAllowEmailTemplate = {
      surveyId: this.props.surveyId,
      packCode: this.state.emailTemplateToAllow,
    };

    const response = await fetch("/api/actionRuleSurveyEmailTemplates", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(newAllowEmailTemplate),
    });

    if (response.ok) {
      this.setState({ allowActionRuleEmailTemplateDialogDisplayed: false });
    } else {
      const errorMessage = await response.text();
      this.setState({
        allowEmailTemplateError: errorMessage,
      });
      this.allowActionRuleEmailTemplateInProgress = false;
    }
    this.refreshDataFromBackend(this.state.authorisedActivities);
  };

  closeAllowActionRuleEmailTemplateDialog = () => {
    this.setState({ allowActionRuleEmailTemplateDialogDisplayed: false });
  };

  openActionRuleEmailTemplateDialog = () => {
    this.allowActionRuleEmailTemplateInProgress = false;
    this.refreshDataFromBackend(this.state.authorisedActivities);
    this.setState({
      allowActionRuleEmailTemplateDialogDisplayed: true,
      emailTemplateToAllow: "",
      emailTemplateValidationError: false,
      allowEmailTemplateError: "",
    });
  };

  render() {
    const actionRuleEmailTemplateTableRows =
      this.state.actionRuleEmailPackCodes.map((packCode) => (
        <TableRow key={packCode}>
          <TableCell component="th" scope="row">
            {packCode}
          </TableCell>
        </TableRow>
      ));

    const actionRuleEmailTemplateMenuItems =
      this.state.allowableActionRuleEmailPackCodes.map((packCode) => (
        <MenuItem key={packCode} value={packCode} id={packCode}>
          {packCode}
        </MenuItem>
      ));

    return (
      <>
        {this.state.authorisedActivities.includes(
          "LIST_ALLOWED_EMAIL_TEMPLATES_ON_ACTION_RULES",
        ) && (
          <>
            <Typography variant="h6" color="inherit" style={{ marginTop: 20 }}>
              Email Templates Allowed on Action Rules
            </Typography>
            <TableContainer component={Paper}>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>Pack Code</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>{actionRuleEmailTemplateTableRows}</TableBody>
              </Table>
            </TableContainer>
          </>
        )}

        {this.state.authorisedActivities.includes(
          "ALLOW_EMAIL_TEMPLATE_ON_ACTION_RULE",
        ) && (
          <Button
            variant="contained"
            onClick={this.openActionRuleEmailTemplateDialog}
            style={{ marginTop: 10 }}
            id="allowEmailTemplateDialogBtn"
          >
            Allow Email Template on Action Rule
          </Button>
        )}

        <Dialog open={this.state.allowActionRuleEmailTemplateDialogDisplayed}>
          <DialogContent style={{ padding: 30 }}>
            <div>
              <div>
                <FormControl required fullWidth={true}>
                  <InputLabel>Email Template</InputLabel>
                  <Select
                    onChange={this.onEmailTemplateChange}
                    value={this.state.emailTemplateToAllow}
                    error={this.state.emailTemplateValidationError}
                    id="selectEmailTemplate"
                  >
                    {actionRuleEmailTemplateMenuItems}
                  </Select>
                </FormControl>
              </div>
              {this.state.allowEmailTemplateError && (
                <div>
                  <p style={{ color: "red" }}>
                    {this.state.allowEmailTemplateError}
                  </p>
                </div>
              )}
              <div style={{ marginTop: 10 }}>
                <Button
                  onClick={this.onAllowActionRuleEmailTemplate}
                  variant="contained"
                  style={{ margin: 10 }}
                  id="allowEmailTemplateOnActionRule"
                >
                  Allow
                </Button>
                <Button
                  onClick={this.closeAllowActionRuleEmailTemplateDialog}
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

export default AllowedEmailTemplatesOnActionRulesList;
