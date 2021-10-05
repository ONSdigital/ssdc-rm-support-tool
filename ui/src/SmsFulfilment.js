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
import { getSmsFulfilmentTemplates } from "./Utils";

class SmsFulfilment extends Component {
  state = {
    packCode: "",
    allowableSmsFulfilmentTemplates: [],
    packCodeValidationError: false,
    packCodeValidationErrorDesc: "",
    smsUacQidMetadataValidationError: false,
    showDialog: false,
    phoneNumber: "",
    newValueValidationError: "",
    validationError: false,
    newSmsUacQidMetadata: "",
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
    if (!response.ok) {
      return;
    }

    const authJson = await response.json();

    this.setState({ authorisedActivities: authJson });

    return authJson;
  };

  openDialog = () => {
    this.setState({
      showDialog: true,
    });
  };

  closeDialog = () => {
    this.setState({
      packCode: "",
      allowableSmsFulfilmentTemplates: [],
      packCodeValidationError: false,
      showDialog: false,
      newValueValidationError: "",
      phoneNumber: "",
      validationError: false,
      newSmsUacQidMetadata: "",
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
        "LIST_ALLOWED_SMS_TEMPLATES_ON_FULFILMENTS"
      )
    )
      return;

    const fulfilmentPrintTemplates = await getSmsFulfilmentTemplates(
      authorisedActivities,
      this.props.surveyId
    );

    this.setState({
      allowableSmsFulfilmentTemplates: fulfilmentPrintTemplates,
    });
  };

  onSmsTemplateChange = (event) => {
    this.setState({ packCode: event.target.value });
  };

  onNewActionRuleSmsUacQidMetadataChange = (event) => {
    this.setState({
      newSmsUacQidMetadata: event.target.value,
      smsUacQidMetadataValidationError: false,
    });
  };

  onCreate = async () => {
    if (!this.state.packCode) {
      this.setState({
        packCodeValidationError: true,
      });
      return;
    }
    if (!this.state.phoneNumber) {
      this.setState({
        validationError: true,
        newValueValidationError: "Please enter phone number",
      });
      return;
    }

    var uacMetadataJson = null;

    if (this.state.newSmsUacQidMetadata.length > 0) {
      try {
        const parsedJson = JSON.parse(this.state.newSmsUacQidMetadata);
        if (Object.keys(parsedJson).length === 0) {
          this.setState({ smsUacQidMetadataValidationError: true });
          return;
        }
      } catch (err) {
        this.setState({ smsUacQidMetadataValidationError: true });
        return;
      }
      uacMetadataJson = JSON.parse(this.state.newSmsUacQidMetadata);
    }

    const smsFulfilment = {
      packCode: this.state.packCode,
      phoneNumber: this.state.phoneNumber,
      uacMetadata: uacMetadataJson,
    };

    const response = await fetch(
      `/api/cases/${this.props.caseId}/action/sms-fulfilment`,
      {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(smsFulfilment),
      }
    );

    if (response.ok) {
      this.closeDialog();
    } else {
      const errorMessageJson = await response.json();
      this.setState({
        newValueValidationError: errorMessageJson.error,
        validationError: true,
      });
    }
  };

  render() {
    const fulfilmentSmsTemplateMenuItems =
      this.state.allowableSmsFulfilmentTemplates.map((packCode) => (
        <MenuItem key={packCode} value={packCode}>
          {packCode}
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
                  value={this.state.packCode}
                  error={this.state.packCodeValidationError}
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
                  style={{ minWidth: 200 }}
                  error={this.state.smsUacQidMetadataValidationError}
                  label="UAC QID Metadata"
                  id="standard-required"
                  onChange={this.onNewActionRuleSmsUacQidMetadataChange}
                  value={this.state.newSmsUacQidMetadata}
                />
              </FormControl>
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
