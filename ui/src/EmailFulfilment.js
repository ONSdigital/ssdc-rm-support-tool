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
import { getEmailFulfilmentTemplates } from "./Utils";

class EmailFulfilment extends Component {
  state = {
    packCode: "",
    allowableEmailFulfilmentTemplates: [],
    packCodeValidationError: false,
    packCodeValidationErrorDesc: "",
    emailUacQidMetadataValidationError: false,
    showDialog: false,
    email: "",
    newValueValidationError: "",
    validationError: false,
    newEmailUacQidMetadata: "",
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
    this.createInProgress = false;

    this.setState({
      showDialog: true,
    });
  };

  closeDialog = () => {
    this.setState({
      packCode: "",
      packCodeValidationError: false,
      showDialog: false,
      newValueValidationError: "",
      email: "",
      validationError: false,
      newEmailUacQidMetadata: "",
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
        "LIST_ALLOWED_EMAIL_TEMPLATES_ON_FULFILMENTS"
      )
    )
      return;

    const fulfilmentEmailTemplates = await getEmailFulfilmentTemplates(
      authorisedActivities,
      this.props.surveyId
    );

    this.setState({
      allowableEmailFulfilmentTemplates: fulfilmentEmailTemplates,
    });
  };

  onEmailTemplateChange = (event) => {
    this.setState({ packCode: event.target.value });
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

    if (!this.state.packCode) {
      this.setState({
        packCodeValidationError: true,
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
      packCode: this.state.packCode,
      email: this.state.email,
      uacMetadata: uacMetadataJson,
    };

    const response = await fetch(
      `/api/cases/${this.props.caseId}/action/email-fulfilment`,
      {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(emailFulfilment),
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
    const fulfilmentEmailTemplateMenuItems =
      this.state.allowableEmailFulfilmentTemplates.map((packCode) => (
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
          Request Email fulfilment
        </Button>
        <Dialog open={this.state.showDialog}>
          <DialogContent style={{ padding: 30 }}>
            <div>
              <FormControl required fullWidth={true}>
                <InputLabel>Email Template</InputLabel>
                <Select
                  onChange={this.onEmailTemplateChange}
                  value={this.state.packCode}
                  error={this.state.packCodeValidationError}
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
                  style={{ minWidth: 200 }}
                  error={this.state.emailUacQidMetadataValidationError}
                  label="UAC QID Metadata"
                  id="standard-required"
                  onChange={this.onNewActionRuleEmailUacQidMetadataChange}
                  value={this.state.newEmailUacQidMetadata}
                />
              </FormControl>
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
