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
import { errorAlert, getFulfilmentExportFileTemplatesForSurvey } from "./Utils";
import FulfilmentPersonalisationForm from "./FulfilmentPersonalisationForm";

class PrintFulfilment extends Component {
  state = {
    selectedTemplate: "",
    allowableFulfilmentExportFileTemplates: [],
    templateValidationError: false,
    printUacQidMetadataValidationError: false,
    showDialog: false,
    newPrintUacQidMetadata: "",
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
      selectedTemplate: null,
      personalisationFormItems: "",
      packCodeValidationError: false,
      printUacQidMetadataValidationError: false,
      showDialog: false,
      newPrintUacQidMetadata: "",
      personalisationValues: null,
    });
  };

  onPrintTemplateChange = (event) => {
    this.setState({ selectedTemplate: event.target.value });
  };

  getTemplateRequestPersonalisationKeys = (templateKeys) => {
    return templateKeys
      .filter((templateKey) => templateKey.startsWith("__request__."))
      .map((templateKey) => templateKey.replace("__request__.", ""));
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

    var uacMetadataJson = null;

    if (this.state.newPrintUacQidMetadata.length > 0) {
      try {
        uacMetadataJson = JSON.parse(this.state.newPrintUacQidMetadata);
        if (Object.keys(uacMetadataJson).length === 0) {
          this.setState({ printUacQidMetadataValidationError: true });
          validationFailed = true;
        }
      } catch (err) {
        this.setState({ printUacQidMetadataValidationError: true });
        validationFailed = true;
      }
    }

    if (validationFailed) {
      this.createInProgress = false;
      return;
    }

    const printFulfilment = {
      packCode: this.state.selectedTemplate.packCode,
      uacMetadata: uacMetadataJson,
      personalisation: this.state.personalisationValues,
    };

    const response = await fetch(
      `/api/cases/${this.props.caseId}/action/printFulfilment`,
      {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(printFulfilment),
      },
    );

    if (response.ok) {
      this.closeDialog();
    }
  };

  onPersonalisationValueChange = (event) => {
    let updatedPersonalisationValues = this.state.personalisationValues
      ? this.state.personalisationValues
      : {};
    updatedPersonalisationValues[event.target.name] = event.target.value;
    this.setState({ personalisationValues: updatedPersonalisationValues });
  };

  // TODO: Need to handle errors from Promises
  refreshDataFromBackend = async (authorisedActivities) => {
    if (
      !authorisedActivities.includes(
        "LIST_ALLOWED_EXPORT_FILE_TEMPLATES_ON_FULFILMENTS",
      )
    )
      return;

    const fulfilmentPrintTemplates =
      await getFulfilmentExportFileTemplatesForSurvey(
        authorisedActivities,
        this.props.surveyId,
      );

    this.setState({
      allowableFulfilmentExportFileTemplates: fulfilmentPrintTemplates,
    });
  };

  render() {
    const fulfilmentPrintTemplateMenuItems =
      this.state.allowableFulfilmentExportFileTemplates.map((template) => (
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
          Request paper fulfilment
        </Button>
        <Dialog open={this.state.showDialog}>
          <DialogContent style={{ padding: 30 }}>
            <div>
              <FormControl required fullWidth={true}>
                <InputLabel>Export File Template</InputLabel>
                <Select
                  onChange={this.onPrintTemplateChange}
                  value={this.state.selectedTemplate}
                  error={this.state.templateValidationError}
                >
                  {fulfilmentPrintTemplateMenuItems}
                </Select>
              </FormControl>
              <FormControl fullWidth={true}>
                <TextField
                  style={{ minWidth: 200, marginBottom: 20 }}
                  error={this.state.printUacQidMetadataValidationError}
                  label="UAC QID Metadata"
                  id="standard-required"
                  onChange={this.onNewActionRulePrintUacQidMetadataChange}
                  value={this.state.newPrintUacQidMetadata}
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
