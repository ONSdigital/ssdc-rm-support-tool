import React, { Component } from 'react';
import { Button, Box, Typography, AppBar, Toolbar } from '@material-ui/core';
import LandingPage from './LandingPage'
import SurveyDetails from './SurveyDetails'
import CollectionExerciseDetails from './CollectionExerciseDetails'
import CaseSearch from './CaseSearch';
import CaseDetails from './CaseDetails';
import SurveyCaseSearch from './SurveyCaseSearch'

class App extends Component {
  state = {
    selectedSurveyId: null,
    selectedSurveyName: '',
    selectedCollexId: null,
    selectedCollexName: '',
    caseSearchActive: false,
    caseSearchResults: [],
    selectedCaseId: null,
  }

  onOpenSurveyDetails = (survey) => {
    const surveyId = survey._links.self.href.split('/')[4]
    this.setState({
      selectedSurveyId: surveyId,
      selectedSurveyName: survey.name
    })
  }

  onOpenCollectionExercise = (collectionExercise) => {
    const collexId = collectionExercise._links.self.href.split('/')[4]
    this.setState({
      selectedCollexId: collexId,
      selectedCollexName: collectionExercise.name
    })
  }

  onOpenSurveyCaseSearch = (searchSurveyId) => {
    this.setState({
      selectedSearchSurveyId: searchSurveyId
    })
  }

  onOpenCaseSearch = () => {
    this.setState({
      caseSearchResults: [], // Clear previous search results
      caseSearchActive: true
    })
  }

  onCaseSearchResults = (caseSearchResults) => {
    this.setState({
      caseSearchResults: caseSearchResults
    })
  }

  onOpenCaseDetails = (caseId) => {
    this.setState({
      selectedCaseId: caseId
    })
  }

  onBackToSurveys = () => {
    this.setState({ selectedSurveyId: null })
  }

  onBackToCollectionExercises = () => {
    this.setState({ selectedCollexId: null })
  }

  onBackToCollexDetails = () => {
    this.setState({ caseSearchActive: false })
  }

  onBackToCaseSearch = () => {
    this.setState({
      selectedCaseId: null
    })
  }

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
        {!this.state.selectedSurveyId &&
          <LandingPage onOpenSurveyDetails={this.onOpenSurveyDetails} />
        }
        {(this.state.selectedSurveyId && !this.state.selectedCollexId && !this.state.selectedCaseId) &&
          <div>
            <Button onClick={this.onBackToSurveys}>Back</Button>
            <SurveyDetails
              surveyId={this.state.selectedSurveyId}
              surveyName={this.state.selectedSurveyName}
              onOpenCollectionExercise={this.onOpenCollectionExercise}
              onOpenSurveyCaseSearch={this.onOpenSurveyCaseSearch} />
          </div>
        }
        {(this.state.selectedSearchSurveyId) &&
          <div>
            <SurveyCaseSearch
                surveyId={this.state.selectedSearchSurveyId} />
          </div>

        }
        {(this.state.selectedCollexId && !this.state.caseSearchActive && !this.state.selectedCaseId) &&
          <div>
            <Button onClick={this.onBackToCollectionExercises}>Back</Button>
            <CollectionExerciseDetails
              surveyId={this.state.selectedSurveyId}
              collectionExerciseId={this.state.selectedCollexId}
              collectionExerciseName={this.state.selectedCollexName}
              onOpenCaseSearch={this.onOpenCaseSearch} />
          </div>
        }
        {(this.state.caseSearchActive && !this.state.selectedCaseId) &&
          <div>
            <Button onClick={this.onBackToCollexDetails}>Back</Button>
            <CaseSearch
              onOpenCaseDetails={this.onOpenCaseDetails}
              onCaseSearchResults={this.onCaseSearchResults}
              caseSearchResults={this.state.caseSearchResults}
              collectionExerciseId={this.state.selectedCollexId}
              collectionExerciseName={this.state.selectedCollexName} />
          </div>
        }
        {this.state.selectedCaseId &&
          <div>
            <Button onClick={this.onBackToCaseSearch}>Back</Button>
            <CaseDetails caseId={this.state.selectedCaseId} />
          </div>
        }
      </Box>
    )
  }
}

export default App