import React, { Component } from 'react';
import { Button, Box, Typography, AppBar, Toolbar } from '@material-ui/core';
import Surveys from './Surveys'
import SurveyDetails from './SurveyDetails'
import CollectionExerciseDetails from './CollectionExerciseDetails'

class App extends Component {
  state = {
    selectedSurvey: null,
    selectedCollex: null
  }

  onOpenSurveyDetails = (survey) => {
    const surveyId = survey._links.self.href.split('/')[4]
    this.setState({ selectedSurvey: surveyId })
  }

  onBackToSurveys = () => {
    this.setState({ selectedSurvey: null })
  }

  onBackToCollectionExercises = () => {
    this.setState({ selectedCollex: null })
  }

  onOpenCollectionExercise = (collectionExercise) => {
    const collexId = collectionExercise._links.self.href.split('/')[4]
    this.setState({ selectedCollex: collexId })
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
        {!this.state.selectedSurvey &&
          <Surveys onOpenSurveyDetails={this.onOpenSurveyDetails} />
        }
        {this.state.selectedSurvey && !this.state.selectedCollex &&
          <div>
            <Button onClick={this.onBackToSurveys}>Back</Button>
            <SurveyDetails
              surveyId={this.state.selectedSurvey}
              onOpenCollectionExercise={this.onOpenCollectionExercise} />
          </div>
        }
        {this.state.selectedCollex &&
          <div>
            <Button onClick={this.onBackToCollectionExercises}>Back</Button>
          <CollectionExerciseDetails collectionExerciseId={this.state.selectedCollex} />
          </div>
        }
      </Box>
    )
  }
}

export default App