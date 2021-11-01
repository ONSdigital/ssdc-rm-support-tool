import React, { Component } from "react";
import { Box, Typography, AppBar, Toolbar } from "@material-ui/core";
import LandingPage from "./LandingPage";
import SurveyDetails from "./SurveyDetails";
import CollectionExerciseDetails from "./CollectionExerciseDetails";
import SurveyCaseSearch from "./SurveyCaseSearch";
import UserAdmin from "./UserAdmin";
import UserDetails from "./UserDetails";
import GroupDetails from "./GroupDetails";
import ExceptionManager from "./ExceptionManager";
import MyGroupsAdmin from "./MyGroupsAdmin";
import MyGroupUserAdmin from "./MyGroupUserAdmin";
import BulkUploads from "./BulkUploads";
import {
  BrowserRouter as Router,
  Switch,
  Route,
  useLocation,
} from "react-router-dom";

class App extends Component {
  render() {
    return (
      <Router>
        <Box>
          <AppBar position="static">
            <Toolbar>
              <Typography variant="h6" color="inherit">
                RM Support Tool
              </Typography>
            </Toolbar>
          </AppBar>

          <QueryRouting />
        </Box>
      </Router>
    );
  }
}

function useQuery() {
  return new URLSearchParams(useLocation().search);
}

function QueryRouting() {
  let query = useQuery();

  return (
    <Switch>
      <Route exact path="/">
        <LandingPage />
      </Route>
      <Route path="/survey">
        <SurveyDetails surveyId={query.get("surveyId")} />
      </Route>
      <Route path="/search">
        <SurveyCaseSearch
          surveyId={query.get("surveyId")}
          caseId={query.get("caseId")}
        />
      </Route>
      <Route path="/collex">
        <CollectionExerciseDetails
          surveyId={query.get("surveyId")}
          collectionExerciseId={query.get("collexId")}
        />
      </Route>
      <Route path="/userAdmin">
        <UserAdmin />
      </Route>
      <Route path="/userDetails">
        <UserDetails userId={query.get("userId")} />
      </Route>
      <Route path="/groupDetails">
        <GroupDetails groupId={query.get("groupId")} />
      </Route>
      <Route path="/exceptionManager">
        <ExceptionManager />
      </Route>
      <Route path="/myGroupsAdmin">
        <MyGroupsAdmin />
      </Route>
      <Route path="/myGroupUserAdmin">
        <MyGroupUserAdmin groupId={query.get("groupId")} />
      </Route>
      <Route path="/bulkUploads">
        <BulkUploads
          surveyId={query.get("surveyId")}
          collectionExerciseId={query.get("collexId")} />
      </Route>
    </Switch>
  );
}

export default App;
