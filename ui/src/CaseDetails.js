import React, { Component } from 'react';
import '@fontsource/roboto';
import { Typography, Grid, Button, Dialog, DialogContent, LinearProgress } from '@material-ui/core';

class CaseDetails extends Component {
  render() {

    return (

      <Dialog open={this.props.showDetails} onClose={this.props.onClickAway}>
        <DialogContent style={{ padding: 30 }}>
          <div>
            {this.props.case &&
              <div>
                <div>Case ref: {this.props.case.caseRef}</div>
                <div>Created at: {this.props.case.createdAt}</div>
                <div>Last updated at: {this.props.case.lastUpdatedAt}</div>
                <div>Receipted: {this.props.case.receiptReceived ? "Yes" : "No"}</div>
                <div>Refused: {this.props.case.refusalReceived ? this.props.case.refusalReceived : "No"}</div>
                <div>Invalid: {this.props.case.addressInvalid ? "Yes" : "No"}</div>
                <div>Launched EQ: {this.props.case.surveyLaunched ? "Yes" : "No"}</div>
              </div>
            }
            <Button onClick={this.props.onCloseDetails} variant="contained" style={{ margin: 10 }}>
              Close
            </Button>
          </div>
        </DialogContent>
      </Dialog>

    )
  }
}

export default CaseDetails