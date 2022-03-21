import React, { Component } from "react";
import Table from "@material-ui/core/Table";
import TableBody from "@material-ui/core/TableBody";
import TableCell from "@material-ui/core/TableCell";
import TableRow from "@material-ui/core/TableRow";
import TableContainer from "@material-ui/core/TableContainer";
import TableHead from "@material-ui/core/TableHead";
import {
  Button,
  Dialog,
  DialogContent,
  TextField,
  Paper,
  Typography,
} from "@material-ui/core";
import { getAuthorisedActivities } from "./Utils";

class EmailTemplateList extends Component {
  state = {
    emailTemplates: [],
    createEmailTemplateDialogDisplayed: false,
    createEmailTemplateError: "",
    createEmailTemplatePackCodeError: "",
    authorisedActivities: [],
    notifyTemplateId: "",
    packCode: "",
    description: "",
    template: "",
    metadata: "",
    packCodeValidationError: "",
    descriptionValidationError: false,
    templateValidationError: false,
    templateValidationErrorMessage: "",
    newTemplateMetadataValidationError: false,
    notifyTemplateIdValidationError: false,
    notifyTemplateIdErrorMessage: "",
    createEmailTemplateDescriptionError: "",
  };

  componentDidMount() {
    this.getBackEndData();
  }

  getBackEndData = async () => {
    const authorisedActivities = await getAuthorisedActivities();
    this.setState({ authorisedActivities: authorisedActivities });
    this.refreshDataFromBackend(authorisedActivities);

    this.interval = setInterval(
      () => this.refreshDataFromBackend(authorisedActivities),
      1000
    );
  };

  refreshDataFromBackend = async (authorisedActivities) => {
    if (!authorisedActivities.includes("LIST_EMAIL_TEMPLATES")) return;

    const response = await fetch("/api/emailTemplates");
    const templateJson = await response.json();

    this.setState({ emailTemplates: templateJson });
  };

  openEmailTemplateDialog = () => {
    this.createEmailTemplateInProgress = false;

    // Yes. Yes here. Here is the one and ONLY place where you should be preparing the dialog
    this.setState({
      description: "",
      packCode: "",
      template: "",
      newTemplateMetadata: "",
      notifyTemplateId: "",
      packCodeValidationError: false,
      descriptionValidationError: false,
      templateValidationError: false,
      templateValidationErrorMessage: "",
      newTemplateMetadataValidationError: false,
      notifyTemplateIdValidationError: false,
      createEmailTemplateDialogDisplayed: true,
      createEmailTemplateError: "",
      notifyTemplateIdErrorMessage: "",
      createEmailTemplatePackCodeError: "",
      createEmailTemplateDescriptionError: "",
    });
  };

  closeEmailTemplateDialog = () => {
    // No. Do not. Do not put anything extra in here. This method ONLY deals with closing the dialog.
    this.setState({
      createEmailTemplateDialogDisplayed: false,
    });
  };

  onPackCodeChange = (event) => {
    const resetValidation = !event.target.value.trim();

    this.setState({
      packCode: event.target.value,
      packCodeValidationError: resetValidation,
    });
  };

  onDescriptionChange = (event) => {
    const resetValidation = !event.target.value.trim();

    this.setState({
      description: event.target.value,
      descriptionValidationError: resetValidation,
    });
  };

  onNotifyTemplateIdChange = (event) => {
    const resetValidation = !event.target.value.trim();

    this.setState({
      notifyTemplateId: event.target.value,
      notifyTemplateIdValidationError: resetValidation,
    });
  };

  onTemplateChange = (event) => {
    const resetValidation = !event.target.value.trim();

    this.setState({
      template: event.target.value,
      templateValidationError: resetValidation,
    });
  };

  onNewTemplateMetadataChange = (event) => {
    this.setState({
      newTemplateMetadata: event.target.value,
      newTemplateMetadataValidationError: false,
    });
  };

  onCreateEmailTemplate = async () => {
    if (this.createEmailTemplateInProgress) {
      return;
    }

    this.createEmailTemplateInProgress = true;

    this.setState({
      createEmailTemplatePackCodeError: "",
      packCodeValidationError: false,
    });

    var failedValidation = false;

    if (!this.state.packCode.trim()) {
      this.setState({ packCodeValidationError: true });
      failedValidation = true;
    }

    if (!this.state.description.trim()) {
      this.setState({ descriptionValidationError: true });
      failedValidation = true;
    }

    if (
      this.state.emailTemplates.some(
        (emailTemplate) =>
          emailTemplate.packCode.toUpperCase() ===
          this.state.packCode.toUpperCase()
      )
    ) {
      this.setState({
        createEmailTemplatePackCodeError: "Pack code already in use",
        packCodeValidationError: true,
      });
      failedValidation = true;
    }

    if (!this.state.notifyTemplateId) {
      this.setState({
        notifyTemplateIdValidationError: true,
      });
      failedValidation = true;
    } else {
      const regexExp =
        /^[0-9a-fA-F]{8}\b-[0-9a-fA-F]{4}\b-[0-9a-fA-F]{4}\b-[0-9a-fA-F]{4}\b-[0-9a-fA-F]{12}$/gi;
      if (!regexExp.test(this.state.notifyTemplateId)) {
        this.setState({
          notifyTemplateIdValidationError: true,
          notifyTemplateIdErrorMessage: "Not a valid UUID",
        });
        failedValidation = true;
      }
    }



    if (!this.state.template.trim()) {
      this.setState({ templateValidationError: true });
      failedValidation = true;
    } else {
      try {
        const parsedJson = JSON.parse(this.state.template);
        const hasDuplicateTemplateColumns = new Set(parsedJson).size !== parsedJson.length;
        if (!Array.isArray(parsedJson) || hasDuplicateTemplateColumns) {
          this.setState({ templateValidationError: true });
          failedValidation = true;
          this.setState({ templateValidationErrorMessage: "Email template must be JSON array with one or more unique elements" });
        }
      } catch (err) {
        this.setState({ templateValidationError: true });
        failedValidation = true;
        this.setState({ templateValidationErrorMessage: "Email template must be JSON array with one or more unique elements" });
      }
    }

    let metadata = null;

    if (this.state.newTemplateMetadata) {
      try {
        metadata = JSON.parse(this.state.newTemplateMetadata);
        if (Object.keys(metadata).length === 0) {
          this.setState({ newTemplateMetadataValidationError: true });
          failedValidation = true;
        }
      } catch (err) {
        this.setState({ newTemplateMetadataValidationError: true });
        failedValidation = true;
      }
    }

    if (failedValidation) {
      this.createEmailTemplateInProgress = false;
      return;
    }

    const newEmailTemplate = {
      notifyTemplateId: this.state.notifyTemplateId,
      packCode: this.state.packCode,
      description: this.state.description,
      template: JSON.parse(this.state.template),
      metadata: metadata,
    };

    const response = await fetch("/api/emailTemplates", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(newEmailTemplate),
    });

    if (!response.ok) {
      this.setState({
        createEmailTemplateError: "Error creating email template",
      });
      this.createEmailTemplateInProgress = false;
    } else {
      this.setState({ createEmailTemplateDialogDisplayed: false });
    }
  };

  render() {
    const emailTemplateRows = this.state.emailTemplates.map((emailTemplate) => (
      <TableRow key={emailTemplate.packCode}>
        <TableCell component="th" scope="row">
          {emailTemplate.packCode}
        </TableCell>
        <TableCell component="th" scope="row">
          {emailTemplate.description}
        </TableCell>
        <TableCell component="th" scope="row">
          {JSON.stringify(emailTemplate.template)}
        </TableCell>
        <TableCell component="th" scope="row">
          {emailTemplate.notifyTemplateId}
        </TableCell>
        <TableCell component="th" scope="row">
          {JSON.stringify(emailTemplate.metadata)}
        </TableCell>
      </TableRow>
    ));

    return (
      <>
        {this.state.authorisedActivities.includes("LIST_EMAIL_TEMPLATES") && (
          <>
            <Typography variant="h6" color="inherit" style={{ marginTop: 10 }}>
              Email Templates
            </Typography>
            <TableContainer component={Paper}>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>Pack Code</TableCell>
                    <TableCell>Description</TableCell>
                    <TableCell>Template</TableCell>
                    <TableCell>Gov Notify Template ID</TableCell>
                    <TableCell>Metadata</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>{emailTemplateRows}</TableBody>
              </Table>
            </TableContainer>
          </>
        )}

        {this.state.authorisedActivities.includes("CREATE_EMAIL_TEMPLATE") && (
          <Button
            variant="contained"
            onClick={this.openEmailTemplateDialog}
            style={{ marginTop: 10 }}
          >
            Create Email Template
          </Button>
        )}

        <Dialog
          open={this.state.createEmailTemplateDialogDisplayed}
          fullWidth={true}
        >
          <DialogContent style={{ padding: 30 }}>
            {this.state.createEmailTemplateError && (
              <div style={{ color: "red" }}>
                {this.state.createEmailTemplateError}
              </div>
            )}
            <div>
              <div>
                <TextField
                  required
                  fullWidth={true}
                  error={this.state.packCodeValidationError}
                  label="Pack Code"
                  onChange={this.onPackCodeChange}
                  value={this.state.packCode}
                  helperText={this.state.createEmailTemplatePackCodeError}
                />
                <TextField
                  required
                  fullWidth={true}
                  style={{ marginTop: 10 }}
                  error={this.state.descriptionValidationError}
                  label="Description"
                  onChange={this.onDescriptionChange}
                  value={this.state.description}
                  helperText={this.state.createEmailTemplateDescriptionError}
                />
                <TextField
                  required
                  fullWidth={true}
                  style={{ marginTop: 10 }}
                  error={this.state.notifyTemplateIdValidationError}
                  label="Notify Template ID (UUID)"
                  onChange={this.onNotifyTemplateIdChange}
                  value={this.state.notifyTemplateId}
                />
                <TextField
                  fullWidth={true}
                  style={{ marginTop: 10 }}
                  error={this.state.templateValidationError}
                  label="Template"
                  onChange={this.onTemplateChange}
                  value={this.state.template}
                  helperText={this.state.templateValidationErrorMessage}
                />
                <TextField
                  fullWidth={true}
                  style={{ marginTop: 10 }}
                  error={this.state.newTemplateMetadataValidationError}
                  label="Metadata"
                  onChange={this.onNewTemplateMetadataChange}
                  value={this.state.newTemplateMetadata}
                />
              </div>
              <div style={{ marginTop: 10 }}>
                <Button
                  onClick={this.onCreateEmailTemplate}
                  variant="contained"
                  style={{ margin: 10 }}
                >
                  Create Email template
                </Button>
                <Button
                  onClick={this.closeEmailTemplateDialog}
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

export default EmailTemplateList;
