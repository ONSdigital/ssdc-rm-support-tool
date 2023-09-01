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
  getActionRuleExportFilePackCodesForSurvey,
  getAllExportFilePackCodes,
} from "./Utils";

class AllowedExportFileTemplatesActionRulesList extends Component {
  state = {
    actionRuleExportFilePackCodes: [],
    allowableActionRuleExportFilePackCodes: [],
    authorisedActivities: [],
    allowActionRuleExportFileTemplateDialogDisplayed: false,
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
    const allExportFileFulfilmentTemplates =
      await getAllExportFilePackCodes(authorisedActivities);

    const actionRuleExportFilePackCodes =
      await getActionRuleExportFilePackCodesForSurvey(
        authorisedActivities,
        this.props.surveyId,
      );

    let allowableActionRuleExportFilePackCodes = [];

    allExportFileFulfilmentTemplates.forEach((packCode) => {
      if (!actionRuleExportFilePackCodes.includes(packCode)) {
        allowableActionRuleExportFilePackCodes.push(packCode);
      }
    });

    this.setState({
      allowableActionRuleExportFilePackCodes:
        allowableActionRuleExportFilePackCodes,
      actionRuleExportFilePackCodes: actionRuleExportFilePackCodes,
    });
  };

  openActionRuleExportFileTemplateDialog = () => {
    this.allowActionRuleExportFileTemplateInProgress = false;
    this.refreshDataFromBackend(this.state.authorisedActivities);
    this.setState({
      allowActionRuleExportFileTemplateDialogDisplayed: true,
      exportFileTemplateToAllow: "",
      exportFileTemplateValidationError: false,
      allowExportFileTemplateError: "",
    });
  };

  onExportFileTemplateChange = (event) => {
    this.setState({ exportFileTemplateToAllow: event.target.value });
  };

  onAllowActionRuleExportFileTemplate = async () => {
    if (this.allowActionRuleExportFileTemplateInProgress) {
      return;
    }

    this.allowActionRuleExportFileTemplateInProgress = true;

    if (!this.state.exportFileTemplateToAllow) {
      this.setState({
        exportFileTemplateValidationError: true,
      });

      this.allowActionRuleExportFileTemplateInProgress = false;
      return;
    }

    const newAllowExportFileTemplate = {
      surveyId: this.props.surveyId,
      packCode: this.state.exportFileTemplateToAllow,
    };

    const response = await fetch("/api/actionRuleSurveyExportFileTemplates", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(newAllowExportFileTemplate),
    });

    if (response.ok) {
      this.setState({
        allowActionRuleExportFileTemplateDialogDisplayed: false,
      });
    } else {
      const errorMessage = await response.text();
      this.setState({
        allowExportFileTemplateError: errorMessage,
      });
      this.allowActionRuleExportFileTemplateInProgress = false;
    }
    this.refreshDataFromBackend(this.state.authorisedActivities);
  };

  closeAllowActionRuleExportFileTemplateDialog = () => {
    this.setState({ allowActionRuleExportFileTemplateDialogDisplayed: false });
  };

  render() {
    const actionRuleExportFileTemplateTableRows =
      this.state.actionRuleExportFilePackCodes.map((packCode) => (
        <TableRow key={packCode}>
          <TableCell component="th" scope="row">
            {packCode}
          </TableCell>
        </TableRow>
      ));

    const actionRuleExportFileTemplateMenuItems =
      this.state.allowableActionRuleExportFilePackCodes.map((packCode) => (
        <MenuItem key={packCode} value={packCode} id={packCode}>
          {packCode}
        </MenuItem>
      ));

    return (
      <>
        {this.state.authorisedActivities.includes(
          "LIST_ALLOWED_EXPORT_FILE_TEMPLATES_ON_ACTION_RULES",
        ) && (
          <>
            <Typography variant="h6" color="inherit" style={{ marginTop: 20 }}>
              Export File Templates Allowed on Action Rules
            </Typography>
            <TableContainer component={Paper}>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>Pack Code</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>{actionRuleExportFileTemplateTableRows}</TableBody>
              </Table>
            </TableContainer>
          </>
        )}
        {this.state.authorisedActivities.includes(
          "ALLOW_EXPORT_FILE_TEMPLATE_ON_ACTION_RULE",
        ) && (
          <Button
            variant="contained"
            onClick={this.openActionRuleExportFileTemplateDialog}
            style={{ marginTop: 10 }}
            id="actionRuleExportFileTemplateBtn"
          >
            Allow Export File Template on Action Rule
          </Button>
        )}
        <Dialog
          open={this.state.allowActionRuleExportFileTemplateDialogDisplayed}
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
                    id="allowExportFileTemplateSelect"
                  >
                    {actionRuleExportFileTemplateMenuItems}
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
                  onClick={this.onAllowActionRuleExportFileTemplate}
                  variant="contained"
                  style={{ margin: 10 }}
                  id="addAllowExportFileTemplateBtn"
                >
                  Allow
                </Button>
                <Button
                  onClick={this.closeAllowActionRuleExportFileTemplateDialog}
                  variant="contained"
                  style={{ margin: 10 }}
                  id="closeAllowExportFileTemplateBtn"
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

export default AllowedExportFileTemplatesActionRulesList;
