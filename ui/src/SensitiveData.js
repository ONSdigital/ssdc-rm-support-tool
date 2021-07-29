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


class SensitiveData extends Component {
  state = {
    columnToUpdate: "",
    newValue: "",
    newValueValidationError: "",
    showDialog: false,
    allowableSensitiveDataColumns: []
  };

  openDialog = () => {
    this.refreshDataFromBackend()
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
    });
  };

  getSensitiveSampleColumns = async () => {
    const response = await fetch(`/api/surveys/${this.props.surveyId}`);
    if (!response.ok) {
      return;
    }

    const surveyJson = await response.json();
    const sensitiveColumns = surveyJson.sampleValidationRules
      .filter((rule) => rule.sensitive)
      .map((rule) => rule.columnName);

    return sensitiveColumns;
  };

  onSensitiveDataColumnChange = (event) => {
    this.setState({ columnToUpdate: event.target.value });
  };

  onChangeValue = (event) => {
    this.setState({
      newValue: event.target.value,
    });
  };

  refreshDataFromBackend = async () => {
    const sensitiveDataColumns = await this.getSensitiveSampleColumns(this.props.surveyId
    ).then((sensitiveColumns) => {
      return sensitiveColumns;
    });

    this.setState({
      allowableSensitiveDataColumns: sensitiveDataColumns,
    });
  };

  onCreate = async () => {
    const updateSampleSensitive = {
      caseId: this.props.caseId,
      sampleSensitive: { [this.state.columnToUpdate]: this.state.newValue }
    }

    const response = await fetch(
      `/api/cases/${this.props.caseId}/action/updateSensitiveField`,
      {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(updateSampleSensitive),
      }
    );

    if (response.ok) {
      alert('Update msg sent successfully')
      this.closeDialog();
      return;
    }

    const data = await response.json();
    this.setState({ newValueValidationError: data.errors });
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
        <Button onClick={this.openDialog} variant="contained" style={{ marginTop: 10 }}>
          Modify Sensitive Data
        </Button>
        <Dialog open={this.state.showDialog}>
          <DialogContent style={{ padding: 30 }}>
            <div>
              <FormControl required fullWidth={true}>
                <InputLabel>Sensitive Data Columns</InputLabel>
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
                style={{ minWidth: 200 }}
                label="new value"
                onChange={this.onChangeValue}
                value={this.state.newValue}
              />
            </div>
            <div>
              {this.state.newValueValidationError}
            </div>
            <div style={{ marginTop: 10 }}>
              <Button
                onClick={this.onCreate}
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