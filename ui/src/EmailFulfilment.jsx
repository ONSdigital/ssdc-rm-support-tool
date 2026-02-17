import React, { Component } from "react";
import {
  Button,
  Dialog,
  DialogContent,
  FormControl,
  InputLabel,
  MenuItem,
  Select,
  TextField,
} from "@material-ui/core";
import { errorAlert, getEmailFulfilmentTemplatesForSurvey } from "./Utils";
import FulfilmentPersonalisationForm from "./FulfilmentPersonalisationForm";

class EmailFulfilment extends Component {
  state = {
    selectedTemplate: "",
    allowableEmailFulfilmentTemplates: [],
    templateValidationError: false,
    templateValidationErrorDesc: "",
    emailUacQidMetadataValidationError: false,
    showDialog: false,
    email: "",
    newValueValidationError: "",
    validationError: false,
    newEmailUacQidMetadata: "",
    personalisationFormItems: "",
    personalisationValues: null,
  };

  componentDidMount() {
    this.getAuthorisedBackendData();
  }

  getAuthorisedBackendData = async () => {
    const authorisedActivities = await this.getAuthorisedActivities(); // Only need to do this once; don't refresh it repeatedly as it changes infrequently
    this.refreshDataFromBackend(authorisedActivities);
  };

  getAuthorisedActivities = async () => {
    const response = await fetch(`/api/auth?surveyId=${this.props.surveyId}`);

    // TODO: We need more elegant error handling throughout the whole application, but this will at least protect temporarily
    const responseJson = await response.json();
    if (!response.ok) {
      errorAlert(responseJson);
      return;
    }

    this.setState({ authorisedActivities: responseJson });

    return responseJson;
  };

  openDialog = () => {
    this.createInProgress = false;

    this.setState({
      showDialog: true,
    });
  };

  closeDialog = () => {
    this.setState({
      selectedTemplate: "",
      templateValidationError: false,
      showDialog: false,
      newValueValidationError: "",
      email: "",
      validationError: false,
      newEmailUacQidMetadata: "",
      personalisationFormItems: "",
      personalisationValues: null,
    });
  };

  onChangeValue = (event) => {
    this.setState({
      email: event.target.value,
    });
  };

  refreshDataFromBackend = async (authorisedActivities) => {
    if (
      !authorisedActivities.includes(
        "LIST_ALLOWED_EMAIL_TEMPLATES_ON_FULFILMENTS",
      )
    )
      return;

    const fulfilmentEmailTemplates = await getEmailFulfilmentTemplatesForSurvey(
      authorisedActivities,
      this.props.surveyId,
    );

    this.setState({
      allowableEmailFulfilmentTemplates: fulfilmentEmailTemplates,
    });
  };

  onEmailTemplateChange = (event) => {
    this.setState({ selectedTemplate: event.target.value });
  };

  onNewActionRuleEmailUacQidMetadataChange = (event) => {
    this.setState({
      newEmailUacQidMetadata: event.target.value,
      emailUacQidMetadataValidationError: false,
    });
  };

  onCreate = async () => {
    if (this.createInProgress) {
      return;
    }

    this.createInProgress = true;

    let validationFailed = false;

    if (!this.state.selectedTemplate) {
      this.setState({
        templateValidationError: true,
      });
      validationFailed = true;
    }

    if (!this.state.email) {
      this.setState({
        validationError: true,
        newValueValidationError: "Please enter email",
      });
      validationFailed = true;
    }

    var uacMetadataJson = null;

    if (this.state.newEmailUacQidMetadata.length > 0) {
      try {
        uacMetadataJson = JSON.parse(this.state.newEmailUacQidMetadata);
        if (Object.keys(uacMetadataJson).length === 0) {
          this.setState({ emailUacQidMetadataValidationError: true });
          validationFailed = true;
        }
      } catch (err) {
        this.setState({ emailUacQidMetadataValidationError: true });
        validationFailed = true;
      }
    }

    if (validationFailed) {
      this.createInProgress = false;
      return;
    }

    const emailFulfilment = {
      packCode: this.state.selectedTemplate.packCode,
      email: this.state.email,
      uacMetadata: uacMetadataJson,
      personalisation: this.state.personalisationValues,
    };

    const response = await fetch(
      `/api/cases/${this.props.caseId}/action/email-fulfilment`,
      {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(emailFulfilment),
      },
    );

    if (response.ok) {
      this.closeDialog();
    } else {
      const errorMessageJson = await response.json();
      this.setState({
        newValueValidationError: errorMessageJson.error,
        validationError: true,
      });

      this.createInProgress = false;
    }
  };

  onPersonalisationValueChange = (event) => {
    let updatedPersonalisationValues = this.state.personalisationValues
      ? this.state.personalisationValues
      : {};
    updatedPersonalisationValues[event.target.name] = event.target.value;
    this.setState({ personalisationValues: updatedPersonalisationValues });
  };

  render() {
    const fulfilmentEmailTemplateMenuItems =
      this.state.allowableEmailFulfilmentTemplates.map((template) => (
        <MenuItem key={template.packCode} value={template}>
          {template.packCode}
        </MenuItem>
      ));

    return (
      <div>
        <Button
          style={{ marginTop: 10 }}
          onClick={this.openDialog}
          variant="contained"
        >
          Request Email fulfilment
        </Button>
        <Dialog open={this.state.showDialog}>
          <DialogContent style={{ padding: 30 }}>
            <div>
              <FormControl required fullWidth={true}>
                <InputLabel>Email Template</InputLabel>
                <Select
                  onChange={this.onEmailTemplateChange}
                  value={this.state.selectedTemplate}
                  error={this.state.templateValidationError}
                >
                  {fulfilmentEmailTemplateMenuItems}
                </Select>
              </FormControl>
              <TextField
                required
                error={this.state.validationError}
                style={{ minWidth: 200 }}
                label="Email"
                onChange={this.onChangeValue}
                value={this.state.email}
                helperText={this.state.newValueValidationError}
              />
              <FormControl fullWidth={true}>
                <TextField
                  style={{ minWidth: 200, marginBottom: 20 }}
                  error={this.state.emailUacQidMetadataValidationError}
                  label="UAC QID Metadata"
                  id="standard-required"
                  onChange={this.onNewActionRuleEmailUacQidMetadataChange}
                  value={this.state.newEmailUacQidMetadata}
                />
              </FormControl>
              {this.state.showDialog && (
                <FulfilmentPersonalisationForm
                  template={this.state.selectedTemplate}
                  onPersonalisationValueChange={
                    this.onPersonalisationValueChange
                  }
                />
              )}
            </div>
            <div style={{ marginTop: 10 }}>
              <Button
                onClick={this.onCreate}
                variant="contained"
                style={{ margin: 10 }}
              >
                Request Email fulfilment
              </Button>
              <Button
                onClick={this.closeDialog}
                variant="contained"
                style={{ margin: 10 }}
              >
                Cancel
              </Button>
            </div>
          </DialogContent>
        </Dialog>
      </div>
    );
  }
}

export default EmailFulfilment;
