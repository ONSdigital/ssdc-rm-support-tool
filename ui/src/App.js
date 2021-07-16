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
    collectionExercises: [],
    selectedCollexId: null,
    selectedCollexName: '',
    caseSearchActive: false,
    caseSearchResults: [],
    caseSearchTerm: '',
    caseSearchDesc: '',
    selectedCaseId: null,
    showCaseSearch: false,
    hideSurvey: false
  }

  onOpenSurveyDetails = (survey) => {
    const surveyId = survey._links.self.href.split('/')[4]
    this.setState({
      selectedSurveyId: surveyId,
      selectedSurveyName: survey.name,
      collectionExercises: []
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
      selectedSearchSurveyId: searchSurveyId,
      showCaseSearch: true,
      hideSurvey: true,
      caseSearchResults: [], // Clear previous search results
      caseSearchTerm: '',
      caseSearchDesc: ''
    })
  }

  onOpenCollexCaseSearch = () => {
    this.setState({
      caseSearchResults: [], // Clear previous search results
      caseSearchActive: true
    })
  }

  onCaseSearchResults = (caseSearchResults, caseSearchTerm, caseSearchDesc) => {
    this.setState({
      caseSearchResults: caseSearchResults,
      caseSearchTerm: caseSearchTerm,
      caseSearchDesc: caseSearchDesc
    })
  }

  onOpenCaseDetails = (caseId) => {
    this.setState({
      selectedCaseId: caseId,
      showCaseSearch: false,
    })
  }

  onBackToSurveys = () => {
    this.setState({ selectedSurveyId: null })
  }

  onBackToSurveyDetails= () => {
    this.setState({ showCaseSearch: false, hideSurvey: false })
  }

  onBackToCollectionExercises = () => {
    this.setState({ selectedCollexId: null })
  }

  onBackToCollexDetails = () => {
    this.setState({ caseSearchActive: false })
  }

  onBackToSurveyCaseSearch = () => {
    this.setState({
      selectedCaseId: null,
      showCaseSearch: true,
    })
  }

  onFetchCollectionExercises = (collectionExercises) => {
    this.setState({
      collectionExercises: collectionExercises
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
          <LandingPage onOpenSurveyDetails={this.onOpenSurveyDetails}/>
        }
        {(this.state.selectedSurveyId && !this.state.selectedCollexId && !this.state.selectedCaseId && !this.state.hideSurvey) &&
          <div>
            <Button onClick={this.onBackToSurveys}>Back</Button>
            <SurveyDetails
              surveyId={this.state.selectedSurveyId}
              surveyName={this.state.selectedSurveyName}
              collectionExercises={this.state.collectionExercises}
              onOpenCollectionExercise={this.onOpenCollectionExercise}
              onOpenSurveyCaseSearch={this.onOpenSurveyCaseSearch}
              onFetchCollectionExercises={this.onFetchCollectionExercises}
            />
          </div>
        }
        {(this.state.showCaseSearch) &&
          <div>
            <Button onClick={this.onBackToSurveyDetails}>Back</Button>
            <SurveyCaseSearch
              surveyId={this.state.selectedSearchSurveyId}
              surveyName={this.state.selectedSurveyName}
              caseSearchResults={this.state.caseSearchResults}
              caseSearchTerm={this.state.caseSearchTerm}
              caseSearchDesc={this.state.caseSearchDesc}
              collectionExercises={this.state.collectionExercises}
              onOpenCaseDetails={this.onOpenCaseDetails}
              onCaseSearchResults={this.onCaseSearchResults}
            />
          </div>
        }
        {(this.state.selectedCollexId && !this.state.caseSearchActive && !this.state.selectedCaseId) &&
          <div>
            <Button onClick={this.onBackToCollectionExercises}>Back</Button>
            <CollectionExerciseDetails
              surveyId={this.state.selectedSurveyId}
              collectionExerciseId={this.state.selectedCollexId}
              collectionExerciseName={this.state.selectedCollexName}
              onOpenCaseSearch={this.onOpenCollexCaseSearch} />
          </div>
        }
        {(this.state.caseSearchActive && !this.state.selectedCaseId) &&
          <div>
            <Button onClick={this.onBackToCollexDetails}>Back</Button>
            <CaseSearch
              onOpenCaseDetails={this.onOpenCaseDetails}
              onCaseSearchResults={this.onCaseSearchResults}
              caseSearchResults={this.state.caseSearchResults}
              surveyId={this.state.selectedSurveyId}
              collectionExerciseId={this.state.selectedCollexId}
              collectionExerciseName={this.state.selectedCollexName} />
          </div>
        }
        {this.state.selectedCaseId &&
          <div>
            <Button onClick={this.onBackToSurveyCaseSearch}>Back</Button>
            <CaseDetails
              surveyId={this.state.selectedSurveyId}
              caseId={this.state.selectedCaseId} />
          </div>
        }
      </Box>
    )
  }
}

export default App
