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
  getActionRuleExportFileTemplates,
  getAllExportFileTemplates,
} from "./Utils";

class AllowedExportFileTemplatesActionRulesList extends Component {
  state = {
    actionRuleExportFileTemplates: [],
    allowableActionRuleExportFileTemplates: [],
    authorisedActivities: [],
    allowActionRuleExportFileTemplateDialogDisplayed: false,
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
    const allExportFileFulfilmentTemplates = await getAllExportFileTemplates(
      authorisedActivities
    );

    const actionRuleExportFileTemplates =
      await getActionRuleExportFileTemplates(
        authorisedActivities,
        this.props.surveyId
      );

    let allowableActionRuleExportFileTemplates = [];

    allExportFileFulfilmentTemplates.forEach((packCode) => {
      if (!actionRuleExportFileTemplates.includes(packCode)) {
        allowableActionRuleExportFileTemplates.push(packCode);
      }
    });

    this.setState({
      allowableActionRuleExportFileTemplates:
        allowableActionRuleExportFileTemplates,
      actionRuleExportFileTemplates: actionRuleExportFileTemplates,
    });
  };

  openActionRuleExportFileTemplateDialog = () => {
    this.allowActionRuleExportFileTemplateInProgress = false;

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
  };

  closeAllowActionRuleExportFileTemplateDialog = () => {
    this.setState({ allowActionRuleExportFileTemplateDialogDisplayed: false });
  };

  render() {
    const actionRuleExportFileTemplateTableRows =
      this.state.actionRuleExportFileTemplates.map((exportFileTemplate) => (
        <TableRow key={exportFileTemplate}>
          <TableCell component="th" scope="row">
            {exportFileTemplate}
          </TableCell>
        </TableRow>
      ));

    const actionRuleExportFileTemplateMenuItems =
      this.state.allowableActionRuleExportFileTemplates.map((packCode) => (
        <MenuItem key={packCode} value={packCode}>
          {packCode}
        </MenuItem>
      ));

    return (
      <>
        {this.state.authorisedActivities.includes(
          "LIST_ALLOWED_EXPORT_FILE_TEMPLATES_ON_ACTION_RULES"
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
          "ALLOW_EXPORT_FILE_TEMPLATE_ON_ACTION_RULE"
        ) && (
          <Button
            variant="contained"
            onClick={this.openActionRuleExportFileTemplateDialog}
            style={{ marginTop: 10 }}
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
                >
                  Allow
                </Button>
                <Button
                  onClick={this.closeAllowActionRuleExportFileTemplateDialog}
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

export default AllowedExportFileTemplatesActionRulesList;
