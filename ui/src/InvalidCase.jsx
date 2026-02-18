import React, { Component } from "react";
import "@fontsource/roboto";
import { Button, Dialog, DialogContent, TextField } from "@material-ui/core";

class InvalidCase extends Component {
  state = {
    reason: "",
    reasonValidationError: false,
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
      reason: "",
      reasonValidationError: false,
      showDialog: false,
    });
  };

  onReasonChange = (event) => {
    this.setState({
      reason: event.target.value,
      reasonValidationError: false,
    });
  };

  onCreate = async () => {
    if (this.createInProgress) {
      return;
    }

    this.createInProgress = true;

    if (!this.state.reason) {
      this.setState({ reasonValidationError: true });

      this.createInProgress = false;
      return;
    }

    const invalidCase = {
      reason: this.state.reason,
    };

    const response = await fetch(
      `/api/cases/${this.props.caseId}/action/invalid-case`,
      {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(invalidCase),
      },
    );

    if (response.ok) {
      this.closeDialog();
    }
  };

  render() {
    return (
      <div>
        <Button
          style={{ marginTop: 10 }}
          onClick={this.openDialog}
          variant="contained"
        >
          Invalidate this case
        </Button>
        <Dialog open={this.state.showDialog}>
          <DialogContent style={{ padding: 30 }}>
            <div>
              <TextField
                required
                fullWidth={true}
                style={{ marginTop: 20 }}
                label="Reason"
                onChange={this.onReasonChange}
                error={this.state.reasonValidationError}
                value={this.state.reason}
              />
            </div>
            <div style={{ marginTop: 10 }}>
              <Button
                onClick={this.onCreate}
                variant="contained"
                style={{ margin: 10 }}
              >
                Invalidate this case
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

export default InvalidCase;
