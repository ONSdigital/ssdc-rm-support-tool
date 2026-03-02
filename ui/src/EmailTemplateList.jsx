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
  MenuItem,
  FormControl,
  InputLabel,
  Select,
} from "@material-ui/core";
import { errorAlert, getAuthorisedActivities } from "./Utils";

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
    notifyServiceRefs: [],
    notifyServiceRef: "",
    packCodeValidationError: "",
    descriptionValidationError: false,
    templateValidationError: false,
    templateValidationErrorMessage: "",
    newTemplateMetadataValidationError: false,
    notifyServiceRefValidationError: false,
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
  };

  refreshDataFromBackend = async (authorisedActivities) => {
    this.getEmailTemplates(authorisedActivities);
    this.getNotifyServiceRefs(authorisedActivities);
  };

  getNotifyServiceRefs = async (authorisedActivities) => {
    // TODO Create new activity called LIST_NOTIFY_SERVICES
    if (!authorisedActivities.includes("LIST_EMAIL_TEMPLATES")) return;

    const supplierResponse = await fetch("/api/notifyServiceRefs");
    const supplierJson = await supplierResponse.json();

    this.setState({
      notifyServiceRefs: supplierJson,
    });
  };

  getEmailTemplates = async (authorisedActivities) => {
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
      notifyServiceRef: "",
      packCodeValidationError: false,
      descriptionValidationError: false,
      templateValidationError: false,
      templateValidationErrorMessage: "",
      newTemplateMetadataValidationError: false,
      notifyTemplateIdValidationError: false,
      notifyServiceRefValidationError: false,
      createEmailTemplateDialogDisplayed: true,
      createEmailTemplateError: "",
      notifyTemplateIdErrorMessage: "",
      notifyServiceRefErrorMessage: "",
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

  onNotifyServiceRefChange = (event) => {
    this.setState({
      notifyServiceRef: event.target.value,
      notifyServiceRefValidationError: false,
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
          this.state.packCode.toUpperCase(),
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
        const hasDuplicateTemplateColumns =
          new Set(parsedJson).size !== parsedJson.length;
        if (!Array.isArray(parsedJson) || hasDuplicateTemplateColumns) {
          this.setState({ templateValidationError: true });
          failedValidation = true;
          this.setState({
            templateValidationErrorMessage:
              "Email template must be JSON array with one or more unique elements",
          });
        }
      } catch (err) {
        this.setState({ templateValidationError: true });
        failedValidation = true;
        this.setState({
          templateValidationErrorMessage:
            "Email template must be JSON array with one or more unique elements",
        });
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
    if (!this.state.notifyServiceRef.trim()) {
      this.setState({ notifyServiceRefValidationError: true });
      failedValidation = true;
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
      notifyServiceRef: this.state.notifyServiceRef,
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
      const responseJson = await response.json();
      errorAlert(responseJson);
    } else {
      this.setState({ createEmailTemplateDialogDisplayed: false });
      this.setState({ createEmailTemplateDialogDisplayed: false });
    }
    this.refreshDataFromBackend(this.state.authorisedActivities);
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
        <TableCell component="th" scope="row">
          {emailTemplate.notifyServiceRef}
        </TableCell>
      </TableRow>
    ));

    const notifyConfigMenuItems = this.state.notifyServiceRefs.map(
      (supplier) => (
        <MenuItem key={supplier} value={supplier} id={supplier}>
          {supplier}
        </MenuItem>
      ),
    );

    return (
      <>
        {this.state.authorisedActivities.includes("LIST_EMAIL_TEMPLATES") && (
          <>
            <Typography variant="h6" color="inherit" style={{ marginTop: 10 }}>
              Email Templates
            </Typography>
            <TableContainer component={Paper}>
              <Table id="emailTemplateTable">
                <TableHead>
                  <TableRow>
                    <TableCell>Pack Code</TableCell>
                    <TableCell>Description</TableCell>
                    <TableCell>Template</TableCell>
                    <TableCell>Gov Notify Template ID</TableCell>
                    <TableCell>Metadata</TableCell>
                    <TableCell>Gov Notify Service Ref</TableCell>
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
            id="openCreateEmailTemplateBtn"
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
                  id="EmailPackcodeTextField"
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
                  id="EmailDescriptionTextField"
                />
                <TextField
                  required
                  fullWidth={true}
                  style={{ marginTop: 10 }}
                  error={this.state.notifyTemplateIdValidationError}
                  label="Notify Template ID (UUID)"
                  onChange={this.onNotifyTemplateIdChange}
                  value={this.state.notifyTemplateId}
                  id="EmailNotifyTemplateIdTextField"
                />
                <TextField
                  fullWidth={true}
                  style={{ marginTop: 10 }}
                  error={this.state.templateValidationError}
                  label="Template"
                  onChange={this.onTemplateChange}
                  value={this.state.template}
                  helperText={this.state.templateValidationErrorMessage}
                  id="EmailTemplateTextField"
                />
                <TextField
                  fullWidth={true}
                  style={{ marginTop: 10 }}
                  error={this.state.newTemplateMetadataValidationError}
                  label="Metadata"
                  onChange={this.onNewTemplateMetadataChange}
                  value={this.state.newTemplateMetadata}
                />
                <FormControl
                  required
                  fullWidth={true}
                  style={{ marginTop: 10 }}
                  id="form"
                >
                  <InputLabel>Notify services</InputLabel>
                  <Select
                    onChange={this.onNotifyServiceRefChange}
                    value={this.state.notifyServiceRef}
                    error={this.state.notifyServiceRefValidationError}
                    id="EmailNotifyServiceRef"
                  >
                    {notifyConfigMenuItems}
                  </Select>
                </FormControl>
              </div>
              <div style={{ marginTop: 10 }}>
                <Button
                  onClick={this.onCreateEmailTemplate}
                  variant="contained"
                  style={{ margin: 10 }}
                  id="createEmailTemplateBtn"
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
