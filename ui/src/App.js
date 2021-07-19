import React, { Component } from "react";
import { Button, Box, Typography, AppBar, Toolbar } from "@material-ui/core";
import LandingPage from "./LandingPage";
import SurveyDetails from "./SurveyDetails";
import CollectionExerciseDetails from "./CollectionExerciseDetails";
import CaseDetails from "./CaseDetails";
import SurveyCaseSearch from "./SurveyCaseSearch";

class App extends Component {
  state = {
    selectedSurveyId: null,
    selectedSurveyName: "",
    selectedCollexId: null,
    selectedCollexName: "",
    caseSearchResults: [],
    caseSearchTerm: "",
    caseSearchDesc: "",
    selectedCaseId: null,
    showCaseSearch: false,
  };

  onOpenSurveyDetails = (survey) => {
    const surveyId = survey._links.self.href.split("/")[4];
    this.setState({
      selectedSurveyId: surveyId,
      selectedSurveyName: survey.name,
    });
  };

  onOpenCollectionExercise = (collectionExercise) => {
    const collexId = collectionExercise._links.self.href.split("/")[4];
    this.setState({
      selectedCollexId: collexId,
      selectedCollexName: collectionExercise.name,
    });
  };

  onOpenSurveyCaseSearch = (searchSurveyId) => {
    this.setState({
      selectedSearchSurveyId: searchSurveyId,
      showCaseSearch: true,
      caseSearchResults: [], // Clear previous search results
      caseSearchTerm: "",
      caseSearchDesc: "",
    });
  };

  onCaseSearchResults = (caseSearchResults, caseSearchTerm, caseSearchDesc) => {
    this.setState({
      caseSearchResults: caseSearchResults,
      caseSearchTerm: caseSearchTerm,
      caseSearchDesc: caseSearchDesc,
    });
  };

  onOpenCaseDetails = (caseId) => {
    this.setState({
      selectedCaseId: caseId,
      showCaseSearch: false,
    });
  };

  onBackToSurveys = () => {
    this.setState({ selectedSurveyId: null });
  };

  onBackToSurveyDetails = () => {
    this.setState({ showCaseSearch: false });
  };

  onBackToCollectionExercises = () => {
    this.setState({ selectedCollexId: null });
  };

  onBackToSurveyCaseSearch = () => {
    this.setState({
      selectedCaseId: null,
      showCaseSearch: true,
    });
  };

  render() {
    return (
      <Box>
        <AppBar position="static">
          <Toolbar>
            <Typography variant="h6" color="inherit">
              RM Support Tool
            </Typography>
          </Toolbar>
        </AppBar>
        {!this.state.selectedSurveyId && (
          <LandingPage onOpenSurveyDetails={this.onOpenSurveyDetails} />
        )}
        {this.state.selectedSurveyId &&  !this.state.selectedCollexId && !this.state.selectedCaseId && !this.state.showCaseSearch && (
            <div>
              <Button onClick={this.onBackToSurveys}>Back</Button>
              <SurveyDetails
                surveyId={this.state.selectedSurveyId}
                surveyName={this.state.selectedSurveyName}
                onOpenCollectionExercise={this.onOpenCollectionExercise}
                onOpenSurveyCaseSearch={this.onOpenSurveyCaseSearch}
              />
            </div>
          )}
        {this.state.showCaseSearch && (
          <div>
            <Button onClick={this.onBackToSurveyDetails}>Back</Button>
            <SurveyCaseSearch
              surveyId={this.state.selectedSearchSurveyId}
              surveyName={this.state.selectedSurveyName}
              caseSearchResults={this.state.caseSearchResults}
              caseSearchTerm={this.state.caseSearchTerm}
              caseSearchDesc={this.state.caseSearchDesc}
              onOpenCaseDetails={this.onOpenCaseDetails}
              onCaseSearchResults={this.onCaseSearchResults}
            />
          </div>
        )}
        {this.state.selectedCollexId && !this.state.selectedCaseId && (
          <div>
            <Button onClick={this.onBackToCollectionExercises}>Back</Button>
            <CollectionExerciseDetails
              surveyId={this.state.selectedSurveyId}
              collectionExerciseId={this.state.selectedCollexId}
              collectionExerciseName={this.state.selectedCollexName}
            />
          </div>
        )}
        {this.state.selectedCaseId && (
          <div>
            <Button onClick={this.onBackToSurveyCaseSearch}>Back</Button>
            <CaseDetails
              surveyId={this.state.selectedSurveyId}
              caseId={this.state.selectedCaseId}
            />
          </div>
        )}
      </Box>
    );
  }
}

export default App;
