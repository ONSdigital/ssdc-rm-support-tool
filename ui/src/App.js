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
  Routes,
  Route,
  useLocation,
  Link,
} from "react-router-dom";

class App extends Component {
  componentDidMount() {
    document.title = "Support Tool";
  }

  render() {
    return (
      <Router>
        <Box>
          <AppBar position="static">
            <Toolbar>
              <Typography variant="h6" color="inherit">
                <Link
                  to="/"
                  style={{ color: "inherit", textDecoration: "inherit" }}
                >
                  RM Support Tool
                </Link>
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
    <Routes>
      <Route path="/" element={<LandingPage />} />
      <Route
        path="/survey"
        element={<SurveyDetails surveyId={query.get("surveyId")} />}
      />
      <Route
        path="/search"
        element={
          <SurveyCaseSearch
            surveyId={query.get("surveyId")}
            caseId={query.get("caseId")}
          />
        }
      />
      <Route
        path="/collex"
        element={
          <CollectionExerciseDetails
            surveyId={query.get("surveyId")}
            collectionExerciseId={query.get("collexId")}
          />
        }
      />
      <Route path="/userAdmin" element={<UserAdmin />} />
      <Route
        path="/userDetails"
        element={<UserDetails userId={query.get("userId")} />}
      />
      <Route
        path="/groupDetails"
        element={<GroupDetails groupId={query.get("groupId")} />}
      />
      <Route path="/exceptionManager" element={<ExceptionManager />} />
      <Route path="/myGroupsAdmin" element={<MyGroupsAdmin />} />
      <Route
        path="/myGroupUserAdmin"
        element={<MyGroupUserAdmin groupId={query.get("groupId")} />}
      />
      <Route
        path="/bulkUploads"
        element={
          <BulkUploads
            surveyId={query.get("surveyId")}
            collectionExerciseId={query.get("collexId")}
          />
        }
      />
    </Routes>
  );
}

export default App;
