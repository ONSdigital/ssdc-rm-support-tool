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
import { errorAlert, getSampleColumns } from "./Utils";

class SampleData extends Component {
  state = {
    columnToUpdate: "",
    newValue: "",
    newValueValidationError: "",
    showDialog: false,
    allowableSampleDataColumns: [],
    validationError: false,
  };

  componentDidMount() {
    this.getAuthorisedBackendData();
  }

  getAuthorisedBackendData = async () => {
    const authorisedActivities = await this.getAuthorisedActivities(); // Only need to do this once; don't refresh it repeatedly as it changes infrequently
    this.getSampleColumns(authorisedActivities, this.props.surveyId);
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

  getSampleColumns = async (authorisedActivities) => {
    if (!authorisedActivities.includes("VIEW_SURVEY")) return;

    const nonSensitiveColumns = await getSampleColumns(
      authorisedActivities,
      this.props.surveyId,
    );
    this.setState({
      allowableSampleDataColumns: nonSensitiveColumns,
    });
  };

  openDialog = () => {
    this.updateSampleDataInProgress = false;

    this.setState({
      showDialog: true,
    });
  };

  closeDialog = () => {
    this.setState({
      columnToUpdate: "",
      newValue: "",
      newValueValidationError: "",
      showDialog: false,
      validationError: false,
    });
  };

  onSampleDataColumnChange = (event) => {
    this.setState({ columnToUpdate: event.target.value });
  };

  onChangeValue = (event) => {
    this.setState({
      newValue: event.target.value,
    });
  };

  onUpdateSampleData = async () => {
    if (this.updateSampleDataInProgress) {
      return;
    }

    this.updateSampleDataInProgress = true;

    if (!this.state.columnToUpdate) {
      this.setState({
        newValueValidationError: "You must select a column to update",
        validationError: true,
      });
      this.updateSampleDataInProgress = false;
      return;
    }
    const updateSample = {
      caseId: this.props.caseId,
      sample: { [this.state.columnToUpdate]: this.state.newValue },
    };

    const response = await fetch(
      `/api/cases/${this.props.caseId}/action/updateSampleField`,
      {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(updateSample),
      },
    );

    if (response.ok) {
      this.closeDialog();
    } else {
      const data = await response.json();
      this.setState({
        newValueValidationError: data.errors,
        validationError: true,
      });
      this.updateSampleDataInProgress = false;
    }
  };

  render() {
    const sampleDataColumnMenuItems = this.state.allowableSampleDataColumns.map(
      (columnName) => (
        <MenuItem key={columnName} value={columnName}>
          {columnName}
        </MenuItem>
      ),
    );

    return (
      <div>
        <Button
          onClick={this.openDialog}
          variant="contained"
          style={{ marginTop: 10 }}
        >
          Modify Sample Data
        </Button>
        <Dialog open={this.state.showDialog}>
          <DialogContent style={{ padding: 30 }}>
            <div>
              <FormControl required fullWidth={true}>
                <InputLabel>Sample Data Column</InputLabel>
                <Select
                  onChange={this.onSampleDataColumnChange}
                  value={this.state.columnToUpdate}
                  error={this.state.newValueValidationError}
                >
                  {sampleDataColumnMenuItems}
                </Select>
              </FormControl>
              <TextField
                required
                error={this.state.validationError}
                style={{ minWidth: 200 }}
                label="new value"
                onChange={this.onChangeValue}
                value={this.state.newValue}
                helperText={this.state.newValueValidationError}
              />
            </div>
            <div></div>
            <div style={{ marginTop: 10 }}>
              <Button
                onClick={this.onUpdateSampleData}
                variant="contained"
                style={{ margin: 10 }}
              >
                Modify Sample Data
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

export default SampleData;
