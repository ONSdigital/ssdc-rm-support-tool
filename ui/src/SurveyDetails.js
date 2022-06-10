import React, { Component } from "react";
import "@fontsource/roboto";
import { Typography } from "@material-ui/core";
import { Link } from "react-router-dom";
import CollectionExerciseList from "./CollectionExerciseList";
import AllowedExportFileTemplatesActionRulesList from "./AllowedExportFileTemplatesActionRulesList";
import AllowedSMSTemplatesActionRulesList from "./AllowedSMSTemplatesActionRulesList";
import AllowedEmailTemplatesOnActionRulesList from "./AllowedEmailTemplatesOnActionRulesList";
import AllowedExportFileTemplatesOnFulfilmentsList from "./AllowedExportFileTemplatesOnFulfilmentsList";
import AllowedSMSTemplatesOnFulfilmentsList from "./AllowedSMSTemplatesOnFulfilmentsList";
import AllowedEmailTemplatesOnFulfilments from "./AllowedEmailTemplatesOnFulfilments";
import {errorAlert} from "./Utils";

class SurveyDetails extends Component {
  state = {
    authorisedActivities: [],
    surveyName: "",
  };

  componentDidMount() {
    this.getAuthorisedBackendData();
  }

  getAuthorisedBackendData = async () => {
    const authorisedActivities = await this.getAuthorisedActivities(); // Only need to do this once; don't refresh it repeatedly as it changes infrequently
    this.getSurveyName(authorisedActivities); // Only need to do this once; don't refresh it repeatedly as it changes infrequently
  };

  getAuthorisedActivities = async () => {
    const response = await fetch(`/api/auth?surveyId=${this.props.surveyId}`);

    // TODO: We need more elegant error handling throughout the whole application, but this will at least protect temporarily
    if (!response.ok) {
      errorAlert(response)
      return;
    }

    const authJson = await response.json();

    this.setState({ authorisedActivities: authJson });

    return authJson;
  };

  getSurveyName = async (authorisedActivities) => {
    if (!authorisedActivities.includes("VIEW_SURVEY")) return;

    const response = await fetch(`/api/surveys/${this.props.surveyId}`);

    const surveyJson = await response.json();

    this.setState({ surveyName: surveyJson.name });
  };

  render() {
    return (
      <div style={{ padding: 20 }}>
        <Link to="/">← Back to home</Link>
        <Typography variant="h4" color="inherit" style={{ marginBottom: 20 }}>
          Survey: {this.state.surveyName}
        </Typography>
        {this.state.authorisedActivities.includes("SEARCH_CASES") && (
          <div style={{ marginBottom: 20 }}>
            <Link to={`/search?surveyId=${this.props.surveyId}`}>
              Search cases
            </Link>
          </div>
        )}

        <CollectionExerciseList surveyId={this.props.surveyId} />
        <AllowedExportFileTemplatesActionRulesList
          surveyId={this.props.surveyId}
        />
        <AllowedSMSTemplatesActionRulesList surveyId={this.props.surveyId} />
        <AllowedEmailTemplatesOnActionRulesList
          surveyId={this.props.surveyId}
        />
        <AllowedExportFileTemplatesOnFulfilmentsList
          surveyId={this.props.surveyId}
        />
        <AllowedSMSTemplatesOnFulfilmentsList surveyId={this.props.surveyId} />
        <AllowedEmailTemplatesOnFulfilments surveyId={this.props.surveyId} />
      </div>
    );
  }
}

export default SurveyDetails;
