import React, { Component } from "react";
import "@fontsource/roboto";
import { Link } from "react-router-dom";
import SurveysList from "./SurveysList";
import ExportFileTemplateList from "./ExportFileTemplatesList";
import SmsTemplatesList from "./SmsTemplatesList";
import EmailTemplateList from "./EmailTemplateList";
import ConfigureFulfilmentTrigger from "./ConfigureFulfilmentTrigger";

import { errorAlert, getAuthorisedActivities } from "./Utils";

class LandingPage extends Component {
  state = {
    authorisedActivities: [],
    thisUserAdminGroups: [],
  };

  componentDidMount() {
    this.getAuthorisedBackendData();
  }

  getAuthorisedBackendData = async () => {
    this.getThisUserAdminGroups(); // Only need to do this once; don't refresh it repeatedly as it changes infrequently
    const authorisedActivities = await getAuthorisedActivities();
    this.setState({ authorisedActivities: authorisedActivities });
  };

  getThisUserAdminGroups = async () => {
    const response = await fetch("/api/userGroups/thisUserAdminGroups");

    // TODO: We need more elegant error handling throughout the whole application, but this will at least protect temporarily
    const responseJson = await response.json();
    if (!response.ok) {
      errorAlert(responseJson);
      return;
    }

    this.setState({ thisUserAdminGroups: responseJson });
  };

  render() {
    return (
      <div style={{ padding: 20 }}>
        <SurveysList />
        <ExportFileTemplateList />
        <SmsTemplatesList />
        <EmailTemplateList />
        <ConfigureFulfilmentTrigger />

        {this.state.authorisedActivities.includes("SUPER_USER") && (
          <>
            <div style={{ marginTop: 20 }}>
              <Link to="/userAdmin">User and Groups Admin</Link>
            </div>
          </>
        )}
        {this.state.thisUserAdminGroups.length > 0 && (
          <>
            <div style={{ marginTop: 20 }}>
              <Link to="/myGroupsAdmin">My Groups Admin</Link>
            </div>
          </>
        )}
        {this.state.authorisedActivities.includes(
          "EXCEPTION_MANAGER_VIEWER",
        ) && (
          <>
            <div style={{ marginTop: 20 }}>
              <Link to="/exceptionManager">Exception Manager</Link>
            </div>
          </>
        )}
      </div>
    );
  }
}

export default LandingPage;
