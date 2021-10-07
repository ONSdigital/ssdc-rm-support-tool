import React, { Component } from "react";
import "@fontsource/roboto";
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
import { getFulfilmentPrintTemplates } from "./Utils";

class PrintFulfilment extends Component {
  state = {
    packCode: "",
    allowableFulfilmentPrintTemplates: [],
    packCodeValidationError: false,
    printUacQidMetadataValidationError: false,
    showDialog: false,
    newPrintUacQidMetadata: "",
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
      packCodeValidationError: false,
      showDialog: false,
      newPrintUacQidMetadata: "",
    });
  };

  onPrintTemplateChange = (event) => {
    this.setState({ packCode: event.target.value });
  };

  onNewActionRulePrintUacQidMetadataChange = (event) => {
    this.setState({
      newPrintUacQidMetadata: event.target.value,
      printUacQidMetadataValidationError: false,
    });
  };

  onCreate = async () => {
    if (!this.state.packCode) {
      this.setState({
        packCodeValidationError: true,
      });

      return;
    }

    var uacMetadataJson = null;

    if (this.state.newPrintUacQidMetadata.length > 0) {
      try {
        const parsedJson = JSON.parse(this.state.newPrintUacQidMetadata);
        if (Object.keys(parsedJson).length === 0) {
          this.setState({ printUacQidMetadataValidationError: true });
          return;
        }
      } catch (err) {
        this.setState({ printUacQidMetadataValidationError: true });
        return;
      }
      uacMetadataJson = JSON.parse(this.state.newPrintUacQidMetadata);
    }

    const printFulfilment = {
      packCode: this.state.packCode,
      uacMetadata: uacMetadataJson,
    };

    const response = await fetch(
      `/api/cases/${this.props.caseId}/action/print-fulfilment`,
      {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(printFulfilment),
      }
    );

    if (response.ok) {
      this.closeDialog();
    }
  };

  // TODO: Need to handle errors from Promises
  refreshDataFromBackend = async (authorisedActivities) => {
    if (
      !authorisedActivities.includes(
        "LIST_ALLOWED_PRINT_TEMPLATES_ON_FULFILMENTS"
      )
    )
      return;

    const fulfilmentPrintTemplates = await getFulfilmentPrintTemplates(
      authorisedActivities,
      this.props.surveyId
    );

    this.setState({
      allowableFulfilmentPrintTemplates: fulfilmentPrintTemplates,
    });
  };

  render() {
    const fulfilmentPrintTemplateMenuItems =
      this.state.allowableFulfilmentPrintTemplates.map((packCode) => (
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
          Request paper fulfilment
        </Button>
        <Dialog open={this.state.showDialog}>
          <DialogContent style={{ padding: 30 }}>
            <div>
              <FormControl required fullWidth={true}>
                <InputLabel>Print Template</InputLabel>
                <Select
                  onChange={this.onPrintTemplateChange}
                  value={this.state.packCode}
                  error={this.state.packCodeValidationError}
                >
                  {fulfilmentPrintTemplateMenuItems}
                </Select>
              </FormControl>
              <FormControl fullWidth={true}>
                <TextField
                  style={{ minWidth: 200 }}
                  error={this.state.printUacQidMetadataValidationError}
                  label="UAC QID Metadata"
                  id="standard-required"
                  onChange={this.onNewActionRulePrintUacQidMetadataChange}
                  value={this.state.newPrintUacQidMetadata}
                />
              </FormControl>
            </div>
            <div style={{ marginTop: 10 }}>
              <Button
                onClick={this.onCreate}
                variant="contained"
                style={{ margin: 10 }}
              >
                Request paper fulfilment
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

export default PrintFulfilment;
