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
    showDialog: false,
    phoneNumber: "",
    newValueValidationError: "",
    validationError: false,
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
      allowableSmsFulfilmentTemplates: [],
      packCodeValidationError: false,
      showDialog: false,
      newValueValidationError: "",
      phoneNumber: "",
      validationError: false,
    });
  };

  onChangeValue = (event) => {
    this.setState({
      phoneNumber: event.target.value,
    });
  };
  refreshDataFromBackend = async () => {
    const fulfilmentPrintTemplates = await getSmsFulfilmentTemplates(
      this.props.surveyId
    ).then((smsTemplates) => {
      return smsTemplates;
    });

    this.setState({
      allowableSmsFulfilmentTemplates: fulfilmentPrintTemplates,
    });
  };
  onSmsTemplateChange = (event) => {
    this.setState({ packCode: event.target.value });
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

    const smsFulfilment = {
      packCode: this.state.packCode,
      phoneNumber: this.state.phoneNumber,
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
      const errorMessage = await response.text();
      this.setState({
        newValueValidationError: errorMessage,
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
