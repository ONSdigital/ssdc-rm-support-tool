import React, { Component } from 'react';
import { Button, Box, Typography, AppBar, Toolbar } from '@material-ui/core';
import Surveys from './Surveys'
import SurveyDetails from './SurveyDetails'
import CollectionExerciseDetails from './CollectionExerciseDetails'
import CaseSearch from './CaseSearch';

class App extends Component {
  state = {
    selectedSurveyId: null,
    selectedSurveyName: '',
    selectedCollexId: null,
    selectedCollexName: '',
    caseSearchActive: false
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

  onOpenCaseSearch = () => {
    this.setState({
      caseSearchActive: true
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
          <Surveys onOpenSurveyDetails={this.onOpenSurveyDetails} />
        }
        {this.state.selectedSurveyId && !this.state.selectedCollexId &&
          <div>
            <Button onClick={this.onBackToSurveys}>Back</Button>
            <SurveyDetails
              surveyId={this.state.selectedSurveyId}
              surveyName={this.state.selectedSurveyName}
              onOpenCollectionExercise={this.onOpenCollectionExercise} />
          </div>
        }
        {this.state.selectedCollexId && !this.state.caseSearchActive &&
          <div>
            <Button onClick={this.onBackToCollectionExercises}>Back</Button>
            <CollectionExerciseDetails
              collectionExerciseId={this.state.selectedCollexId}
              collectionExerciseName={this.state.selectedCollexName}
              onOpenCaseSearch={this.onOpenCaseSearch} />
          </div>
        }
        {this.state.caseSearchActive &&
          <div>
            <Button onClick={this.onBackToCollexDetails}>Back</Button>
            <CaseSearch
              collectionExerciseId={this.state.selectedCollexId}
              collectionExerciseName={this.state.selectedCollexName} />
          </div>
        }
      </Box>
    )
  }
}

export default App