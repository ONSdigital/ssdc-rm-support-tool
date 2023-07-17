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
import { errorAlert, getSmsFulfilmentTemplatesForSurvey } from "./Utils";
import FulfilmentPersonalisationForm from "./FulfilmentPersonalisationForm";

class SmsFulfilment extends Component {
  state = {
    selectedTemplate: "",
    allowableSmsFulfilmentTemplates: [],
    templateValidationError: false,
    templateValidationErrorDesc: "",
    smsUacQidMetadataValidationError: false,
    showDialog: false,
    phoneNumber: "",
    newValueValidationError: "",
    validationError: false,
    newSmsUacQidMetadata: "",
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
      phoneNumber: "",
      validationError: false,
      newSmsUacQidMetadata: "",
      personalisationFormItems: "",
      personalisationValues: null,
    });
  };

  onChangeValue = (event) => {
    this.setState({
      phoneNumber: event.target.value,
    });
  };

  refreshDataFromBackend = async (authorisedActivities) => {
    if (
      !authorisedActivities.includes(
        "LIST_ALLOWED_SMS_TEMPLATES_ON_FULFILMENTS",
      )
    )
      return;

    const fulfilmentSmsTemplates = await getSmsFulfilmentTemplatesForSurvey(
      authorisedActivities,
      this.props.surveyId,
    );

    this.setState({
      allowableSmsFulfilmentTemplates: fulfilmentSmsTemplates,
    });
  };

  onSmsTemplateChange = (event) => {
    this.setState({ selectedTemplate: event.target.value });
  };

  onNewActionRuleSmsUacQidMetadataChange = (event) => {
    this.setState({
      newSmsUacQidMetadata: event.target.value,
      smsUacQidMetadataValidationError: false,
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

    if (!this.state.phoneNumber) {
      this.setState({
        validationError: true,
        newValueValidationError: "Please enter phone number",
      });
      validationFailed = true;
    }

    var uacMetadataJson = null;

    if (this.state.newSmsUacQidMetadata.length > 0) {
      try {
        uacMetadataJson = JSON.parse(this.state.newSmsUacQidMetadata);
        if (Object.keys(uacMetadataJson).length === 0) {
          this.setState({ smsUacQidMetadataValidationError: true });
          validationFailed = true;
        }
      } catch (err) {
        this.setState({ smsUacQidMetadataValidationError: true });
        validationFailed = true;
      }
    }

    if (validationFailed) {
      this.createInProgress = false;
      return;
    }

    const smsFulfilment = {
      packCode: this.state.selectedTemplate.packCode,
      phoneNumber: this.state.phoneNumber,
      uacMetadata: uacMetadataJson,
      personalisation: this.state.personalisationValues,
    };

    const response = await fetch(
      `/api/cases/${this.props.caseId}/action/sms-fulfilment`,
      {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(smsFulfilment),
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
    const fulfilmentSmsTemplateMenuItems =
      this.state.allowableSmsFulfilmentTemplates.map((template) => (
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
          Request SMS fulfilment
        </Button>
        <Dialog open={this.state.showDialog}>
          <DialogContent style={{ padding: 30 }}>
            <div>
              <FormControl required fullWidth={true}>
                <InputLabel>SMS Template</InputLabel>
                <Select
                  onChange={this.onSmsTemplateChange}
                  value={this.state.selectedTemplate}
                  error={this.state.templateValidationError}
                >
                  {fulfilmentSmsTemplateMenuItems}
                </Select>
              </FormControl>
              <TextField
                required
                error={this.state.validationError}
                style={{ minWidth: 200 }}
                label="Phone number"
                onChange={this.onChangeValue}
                value={this.state.phoneNumber}
                helperText={this.state.newValueValidationError}
              />
              <FormControl fullWidth={true}>
                <TextField
                  style={{ minWidth: 200, marginBottom: 20 }}
                  error={this.state.smsUacQidMetadataValidationError}
                  label="UAC QID Metadata"
                  id="standard-required"
                  onChange={this.onNewActionRuleSmsUacQidMetadataChange}
                  value={this.state.newSmsUacQidMetadata}
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
                Request SMS fulfilment
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

export default SmsFulfilment;
