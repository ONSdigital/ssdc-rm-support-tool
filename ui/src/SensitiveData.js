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
import { getSensitiveSampleColumns } from "./Utils";

class SensitiveData extends Component {
  state = {
    columnToUpdate: "",
    newValue: "",
    newValueValidationError: "",
    showDialog: false,
    allowableSensitiveDataColumns: [],
    validationError: false,
  };

  componentDidMount() {
    this.getAuthorisedBackendData();
  }

  getAuthorisedBackendData = async () => {
    const authorisedActivities = await this.getAuthorisedActivities(); // Only need to do this once; don't refresh it repeatedly as it changes infrequently
    this.getSensitiveSampleColumns(authorisedActivities, this.props.surveyId);
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

  getSensitiveSampleColumns = async (authorisedActivities) => {
    const sensitiveColumns = await getSensitiveSampleColumns(
      authorisedActivities,
      this.props.surveyId
    );
    this.setState({
      allowableSensitiveDataColumns: sensitiveColumns,
    });
  };

  openDialog = () => {
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

  onSensitiveDataColumnChange = (event) => {
    this.setState({ columnToUpdate: event.target.value });
  };

  onChangeValue = (event) => {
    this.setState({
      newValue: event.target.value,
    });
  };

  onUpdateSensitiveData = async () => {
    if (!this.state.columnToUpdate) {
      this.setState({
        newValueValidationError: "You must select a column to update",
        validationError: true,
      });
      return;
    }
    const updateSampleSensitive = {
      caseId: this.props.caseId,
      sampleSensitive: { [this.state.columnToUpdate]: this.state.newValue },
    };

    const response = await fetch(
      `/api/cases/${this.props.caseId}/action/updateSensitiveField`,
      {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(updateSampleSensitive),
      }
    );

    if (response.ok) {
      this.closeDialog();
      return;
    }

    const data = await response.json();
    this.setState({
      newValueValidationError: data.errors,
      validationError: true,
    });
  };

  render() {
    const sensitiveDataColumnMenuItems =
      this.state.allowableSensitiveDataColumns.map((columnName) => (
        <MenuItem key={columnName} value={columnName}>
          {columnName}
        </MenuItem>
      ));

    return (
      <div>
        <Button
          onClick={this.openDialog}
          variant="contained"
          style={{ marginTop: 10 }}
        >
          Modify Sensitive Data
        </Button>
        <Dialog open={this.state.showDialog}>
          <DialogContent style={{ padding: 30 }}>
            <div>
              <FormControl required fullWidth={true}>
                <InputLabel>Sensitive Data Column</InputLabel>
                <Select
                  onChange={this.onSensitiveDataColumnChange}
                  value={this.state.columnToUpdate}
                  error={this.state.newValueValidationError}
                >
                  {sensitiveDataColumnMenuItems}
                </Select>
              </FormControl>
              <TextField
                required
                error={this.state.validationError}
                style={{ minWidth: 200 }}
                label="new value, blank is valid"
                onChange={this.onChangeValue}
                value={this.state.newValue}
                helperText={this.state.newValueValidationError}
              />
            </div>
            <div></div>
            <div style={{ marginTop: 10 }}>
              <Button
                onClick={this.onUpdateSensitiveData}
                variant="contained"
                style={{ margin: 10 }}
              >
                Modify Sensitive Data
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

export default SensitiveData;
