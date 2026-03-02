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
} from "@material-ui/core";

class Refusal extends Component {
  state = {
    type: "",
    typeValidationError: false,
    showDialog: false,
  };

  openDialog = () => {
    this.createInProgress = false;

    this.setState({
      showDialog: true,
    });
  };

  closeDialog = () => {
    this.setState({
      type: "",
      typeValidationError: false,
      showDialog: false,
    });
  };

  onTypeChange = (event) => {
    this.setState({
      type: event.target.value,
      typeValidationError: false,
    });
  };

  onCreate = async () => {
    if (this.createInProgress) {
      return;
    }

    this.createInProgress = true;

    if (!this.state.type) {
      this.setState({ typeValidationError: true });
      this.createInProgress = false;
      return;
    }

    const newRefusal = {
      type: this.state.type,
    };

    const response = await fetch(
      `/api/cases/${this.props.caseId}/action/refusal`,
      {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(newRefusal),
      },
    );

    if (response.ok) {
      this.closeDialog();
    }
  };

  render() {
    return (
      <div>
        <Button onClick={this.openDialog} variant="contained">
          Refuse this case
        </Button>
        <Dialog open={this.state.showDialog}>
          <DialogContent style={{ padding: 30 }}>
            <div>
              <FormControl required fullWidth={true}>
                <InputLabel>Refusal Type</InputLabel>
                <Select
                  onChange={this.onTypeChange}
                  value={this.state.type}
                  error={this.state.typeValidationError}
                >
                  <MenuItem value={"EXTRAORDINARY_REFUSAL"}>
                    EXTRAORDINARY REFUSAL
                  </MenuItem>
                  <MenuItem value={"HARD_REFUSAL"}>HARD REFUSAL</MenuItem>
                </Select>
              </FormControl>
            </div>
            <div style={{ marginTop: 20 }}>
              <Button
                onClick={this.onCreate}
                variant="contained"
                style={{ margin: 10 }}
              >
                Refuse this case
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

export default Refusal;
