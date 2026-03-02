import React, { Component } from "react";

import { Button, Dialog, DialogContent, TextField } from "@material-ui/core";
import { getAuthorisedActivities } from "./Utils";

class ConfigureFulfilmentTrigger extends Component {
  state = {
    authorisedActivities: [],
    newTemplateMetadata: "",
    notifyTemplateId: "",
    notifyTemplateIdValidationError: false,
    notifyTemplateIdErrorMessage: "",
    nextFulfilmentTriggerDateTime: "1970-01-01T00:00:00.000Z",
    configureNextTriggerDisplayed: false,
  };

  componentDidMount() {
    this.getBackEndData();
  }

  getBackEndData = async () => {
    const authorisedActivities = await getAuthorisedActivities();
    this.setState({ authorisedActivities: authorisedActivities });
  };

  getDateTimeForDateTimePicker = (date) => {
    date.setMinutes(date.getMinutes() - date.getTimezoneOffset());
    return date.toJSON().slice(0, 16);
  };

  getFulfilmentTrigger = async () => {
    if (
      !this.state.authorisedActivities.includes("CONFIGURE_FULFILMENT_TRIGGER")
    )
      return;

    const response = await fetch(`/api/fulfilmentNextTriggers`);

    if (!response.ok) {
      this.setState({
        nextFulfilmentTriggerDateTime: this.getDateTimeForDateTimePicker(
          new Date(),
        ),
      });
    } else {
      const fulfilmentNextTriggerJson = await response.json();
      var dateOfTrigger = new Date(fulfilmentNextTriggerJson);
      this.setState({
        nextFulfilmentTriggerDateTime:
          this.getDateTimeForDateTimePicker(dateOfTrigger),
      });
    }
  };

  openFulfilmentTriggerDialog = () => {
    this.updateFulfilmentTriggerDateTimeInProgress = false;

    this.getFulfilmentTrigger();
    this.setState({
      configureNextTriggerDisplayed: true,
    });
  };

  closeNextTriggerDialog = () => {
    // No. Do not. Do not put anything extra in here. This method ONLY deals with closing the dialog.
    this.setState({ configureNextTriggerDisplayed: false });
  };

  onFulfilmentTriggerDateChange = (event) => {
    this.setState({ nextFulfilmentTriggerDateTime: event.target.value });
  };

  onUpdateFulfilmentTriggerDateTime = async () => {
    if (this.updateFulfilmentTriggerDateTimeInProgress) {
      return;
    }

    this.updateFulfilmentTriggerDateTimeInProgress = true;

    const triggerDateTime = new Date(
      this.state.nextFulfilmentTriggerDateTime,
    ).toISOString();

    const response = await fetch(
      `/api/fulfilmentNextTriggers?triggerDateTime=${triggerDateTime}`,
      {
        method: "POST",
      },
    );

    if (response.ok) {
      this.setState({ configureNextTriggerDisplayed: false });
    }
  };

  render() {
    return (
      <>
        {this.state.authorisedActivities.includes(
          "CONFIGURE_FULFILMENT_TRIGGER",
        ) && (
          <div>
            <Button
              variant="contained"
              onClick={this.openFulfilmentTriggerDialog}
              style={{ marginTop: 20 }}
            >
              Configure fulfilment trigger
            </Button>
          </div>
        )}
        <Dialog
          open={this.state.configureNextTriggerDisplayed}
          fullWidth={true}
        >
          <DialogContent style={{ padding: 30 }}>
            <div>
              <div>
                <TextField
                  label="Trigger Date"
                  type="datetime-local"
                  value={this.state.nextFulfilmentTriggerDateTime.slice(0, 16)}
                  onChange={this.onFulfilmentTriggerDateChange}
                  style={{ marginTop: 20 }}
                  InputLabelProps={{
                    shrink: true,
                  }}
                />
              </div>
              <div style={{ marginTop: 10 }}>
                <Button
                  onClick={this.onUpdateFulfilmentTriggerDateTime}
                  variant="contained"
                  style={{ margin: 10 }}
                >
                  Update fulfilment trigger
                </Button>
                <Button
                  onClick={this.closeNextTriggerDialog}
                  variant="contained"
                  style={{ margin: 10 }}
                >
                  Cancel
                </Button>
              </div>
            </div>
          </DialogContent>
        </Dialog>
      </>
    );
  }
}

export default ConfigureFulfilmentTrigger;
