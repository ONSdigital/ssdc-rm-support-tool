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
import { getFulfilmentExportFileTemplates } from "./Utils";

class PrintFulfilment extends Component {
  state = {
    selectedTemplate: null,
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
      selectedTemplate: null,
      personalisationFormItems: "",
      packCodeValidationError: false,
      showDialog: false,
      newPrintUacQidMetadata: "",
      personalisationValues: null,
    });
  };

  onPrintTemplateChange = (event) => {
    this.setState({ selectedTemplate: event.target.value });
    this.updatePersonalisationFormItems(event.target.value);
  };

  onNewActionRulePrintUacQidMetadataChange = (event) => {
    this.setState({
      newPrintUacQidMetadata: event.target.value,
      printUacQidMetadataValidationError: false,
    });
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
      }
    );

    if (response.ok) {
      this.closeDialog();
    }
  };

  onPersonalisationValueChange = (event) => {
    let personalisationValuesToUpdate = this.state.personalisationValues
      ? this.state.personalisationValues
      : {};
    personalisationValuesToUpdate[event.target.id] = event.target.value;
    this.setState({ personalisationValues: personalisationValuesToUpdate });
  };

  updatePersonalisationFormItems = (selectedTemplate) => {
    const requestTemplateKeys = this.getTemplateRequestPersonalisationKeys(
      selectedTemplate.template
    );

    if (requestTemplateKeys.length === 0) {
      this.setState({ personalisationFormItems: "" });
    } else {
      this.setState({
        personalisationFormItems: requestTemplateKeys.map(
          (personalisationKey) => (
            <FormControl fullWidth={true} key={personalisationKey}>
              <TextField
                label={personalisationKey}
                id={personalisationKey}
                onChange={this.onPersonalisationValueChange}
                value={this.state.personalisationValues?.personalisationKey}
              />
            </FormControl>
          )
        ),
      });
    }
  };

  // TODO: Need to handle errors from Promises
  refreshDataFromBackend = async (authorisedActivities) => {
    if (
      !authorisedActivities.includes(
        "LIST_ALLOWED_EXPORT_FILE_TEMPLATES_ON_FULFILMENTS"
      )
    )
      return;

    const fulfilmentPrintTemplates = await getFulfilmentExportFileTemplates(
      authorisedActivities,
      this.props.surveyId
    );

    this.setState({
      allowableFulfilmentExportFileTemplates: fulfilmentPrintTemplates,
    });
  };

  render() {
    const fulfilmentPrintTemplateMenuItems =
      this.state.allowableFulfilmentExportFileTemplates.map(
        (selectedTemplate) => (
          <MenuItem key={selectedTemplate.packCode} value={selectedTemplate}>
            {selectedTemplate.packCode}
          </MenuItem>
        )
      );

    let fulfilmentPersonalisationFormItems =
      this.state.personalisationFormItems;

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
              {!(this.state.selectedTemplate === null) &&
                this.state.personalisationFormItems !== "" && (
                  <fieldset>
                    <label>Request Personalisation</label>
                    {fulfilmentPersonalisationFormItems}
                  </fieldset>
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
