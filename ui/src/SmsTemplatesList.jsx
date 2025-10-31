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

class SmsTemplatesList extends Component {
  state = {
    smsTemplates: [],
    createSmsTemplateDialogDisplayed: false,
    createSmsTemplateError: "",
    createSMSTemplatePackCodeError: "",
    authorisedActivities: [],
    notifyServiceRef: "",
    notifyServiceRefs: [],
    notifyTemplateId: "",
    packCode: "",
    description: "",
    template: "",
    metadata: "",
    packCodeValidationError: false,
    descriptionValidationError: false,
    templateValidationError: false,
    templateValidationErrorMessage: "",
    newTemplateMetadataValidationError: false,
    notifyTemplateIdValidationError: false,
    notifyTemplateIdErrorMessage: "",
    createSMSTemplateDescriptionError: "",
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
    this.getSmsTemplates(authorisedActivities);
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
  getSmsTemplates = async (authorisedActivities) => {
    if (!authorisedActivities.includes("LIST_SMS_TEMPLATES")) return;

    const response = await fetch("/api/smsTemplates");
    const templateJson = await response.json();

    this.setState({ smsTemplates: templateJson });
  };

  openSmsTemplateDialog = () => {
    this.createSmsTemplateInProgress = false;

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
      createSmsTemplateDialogDisplayed: true,
      notifyServiceRefValidationError: false,
      createSmsTemplateError: "",
      notifyTemplateIdErrorMessage: "",
      createSMSTemplatePackCodeError: "",
      createSMSTemplateDescriptionError: "",
    });
  };

  closeSmsTemplateDialog = () => {
    // No. Do not. Do not put anything extra in here. This method ONLY deals with closing the dialog.
    this.setState({
      createSmsTemplateDialogDisplayed: false,
    });
  };

  onCreateSmsTemplate = async () => {
    if (this.createSmsTemplateInProgress) {
      return;
    }

    this.createSmsTemplateInProgress = true;

    this.setState({
      createSMSTemplatePackCodeError: "",
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
      this.state.smsTemplates.some(
        (smsTemplate) =>
          smsTemplate.packCode.toUpperCase() ===
          this.state.packCode.toUpperCase(),
      )
    ) {
      this.setState({
        createSMSTemplatePackCodeError: "PackCode already in use",
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
              "SMS template must be JSON array with one or more unique elements",
          });
        }
      } catch (err) {
        this.setState({ templateValidationError: true });
        failedValidation = true;
        this.setState({
          templateValidationErrorMessage:
            "SMS template must be JSON array with one or more unique elements",
        });
      }
    }
    if (!this.state.notifyServiceRef.trim()) {
      this.setState({ notifyServiceRefValidationError: true });
      failedValidation = true;
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
      this.createSmsTemplateInProgress = false;
      return;
    }

    const newSmsTemplate = {
      notifyTemplateId: this.state.notifyTemplateId,
      packCode: this.state.packCode,
      description: this.state.description,
      template: JSON.parse(this.state.template),
      notifyServiceRef: this.state.notifyServiceRef,
      metadata: metadata,
    };

    const response = await fetch("/api/smsTemplates", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(newSmsTemplate),
    });

    if (!response.ok) {
      this.setState({
        createSmsTemplateError: "Error Creating SMSTemplate",
      });
      this.createSmsTemplateInProgress = false;
      const responseJson = await response.json();
      errorAlert(responseJson);
    } else {
      this.setState({ createSmsTemplateDialogDisplayed: false });
    }

    this.refreshDataFromBackend(this.state.authorisedActivities);
  };

  onPackCodeChange = (event) => {
    const resetValidation = !event.target.value.trim();

    this.setState({
      packCode: event.target.value,
      packCodeValidationError: resetValidation,
    });
  };

  onNotifyServiceRefChange = (event) => {
    this.setState({
      notifyServiceRef: event.target.value,
      notifyServiceRefValidationError: false,
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

  render() {
    const smsTemplateRows = this.state.smsTemplates.map((smsTemplate) => (
      <TableRow key={smsTemplate.packCode}>
        <TableCell component="th" scope="row">
          {smsTemplate.packCode}
        </TableCell>
        <TableCell component="th" scope="row">
          {smsTemplate.description}
        </TableCell>
        <TableCell component="th" scope="row">
          {JSON.stringify(smsTemplate.template)}
        </TableCell>
        <TableCell component="th" scope="row">
          {smsTemplate.notifyTemplateId}
        </TableCell>
        <TableCell component="th" scope="row">
          {JSON.stringify(smsTemplate.metadata)}
        </TableCell>
        <TableCell component="th" scope="row">
          {smsTemplate.notifyServiceRef}
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
        {this.state.authorisedActivities.includes("LIST_SMS_TEMPLATES") && (
          <>
            <Typography variant="h6" color="inherit" style={{ marginTop: 10 }}>
              SMS Templates
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
                    <TableCell>Gov Notify Service Ref</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>{smsTemplateRows}</TableBody>
              </Table>
            </TableContainer>
          </>
        )}

        {this.state.authorisedActivities.includes("CREATE_SMS_TEMPLATE") && (
          <Button
            variant="contained"
            onClick={this.openSmsTemplateDialog}
            style={{ marginTop: 10 }}
          >
            Create SMS Template
          </Button>
        )}

        <Dialog
          open={this.state.createSmsTemplateDialogDisplayed}
          fullWidth={true}
        >
          <DialogContent style={{ padding: 30 }}>
            {this.state.createSmsTemplateError && (
              <div style={{ color: "red" }}>
                {this.state.createSmsTemplateError}
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
                  helperText={this.state.createSMSTemplatePackCodeError}
                />
                <TextField
                  required
                  fullWidth={true}
                  style={{ marginTop: 10 }}
                  error={this.state.descriptionValidationError}
                  label="Description"
                  onChange={this.onDescriptionChange}
                  value={this.state.description}
                  helperText={this.state.createSMSTemplateDescriptionError}
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
                    id="SmsNotifyServiceRef"
                  >
                    {notifyConfigMenuItems}
                  </Select>
                </FormControl>
              </div>
              <div style={{ marginTop: 10 }}>
                <Button
                  onClick={this.onCreateSmsTemplate}
                  variant="contained"
                  style={{ margin: 10 }}
                >
                  Create SMS template
                </Button>
                <Button
                  onClick={this.closeSmsTemplateDialog}
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

export default SmsTemplatesList;
