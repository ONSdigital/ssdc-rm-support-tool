import React, { Component } from "react";
import "@fontsource/roboto";
import {
  Button,
  Dialog,
  DialogContent,
  FormControl,
  InputLabel,
  MenuItem,
  Select, TextField,
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

  openDialog = () => {
    this.refreshDataFromBackend();
    this.setState({
      showDialog: true,
    });
  };

  closeDialog = () => {
    this.setState({
      packCode: "",
      allowableFulfilmentPrintTemplates: [],
      packCodeValidationError: false,
      showDialog: false,
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
    var failedValidation = false;

    if (!this.state.packCode) {
      this.setState({
        packCodeValidationError: true,
      });

      return;
    }

    if (!this.state.newPrintUacQidMetadata.trim()) {
      this.setState({ printUacQidMetadataValidationError: true });
      failedValidation = true;
    } else {
      try {
        const parsedJson = JSON.parse(this.state.newPrintUacQidMetadata);
        if (Object.keys(parsedJson).length === 0) {
          this.setState({ printUacQidMetadataValidationError: true });
          failedValidation = true;
        }
      } catch (err) {
        this.setState({ printUacQidMetadataValidationError: true });
        failedValidation = true;
      }
    }

    const printFulfilment = {
      packCode: this.state.packCode,
      printUacMetadata: JSON.parse(this.state.newPrintUacQidMetadata),
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
  refreshDataFromBackend = async () => {
    const fulfilmentPrintTemplates = await getFulfilmentPrintTemplates(
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
              <FormControl required fullWidth={true}>
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
