import React, { Component } from "react";
import "@fontsource/roboto";
import { Link } from "react-router-dom";
import Surveys from "./SurveysList";
import ExportFileTemplate from "./ExportFileTemplatesList";
import SmsTemplatesList from "./SmsTemplatesList";
import EmailTemplateList from "./EmailTemplateList";
import ConfigureFulfilmentTrigger from "./ConfigureFulfilmentTrigger";

import { getAuthorisedActivities } from "./Utils";

class LandingPage extends Component {
  state = {
    authorisedActivities: [],
    thisUserAdminGroups: [],
  };

  componentDidMount() {
    this.getAuthorisedBackendData();
  }

  // componentWillUnmount() {
  //   clearInterval(this.interval);
  // }

  getAuthorisedBackendData = async () => {
    this.getThisUserAdminGroups(); // Only need to do this once; don't refresh it repeatedly as it changes infrequently
    const authorisedActivities = await getAuthorisedActivities();
    this.setState({ authorisedActivities: authorisedActivities });

    // TODO: make this work with the new Components..
    // Might just need to cut & paste, or stick in Utilities?
    // this.interval = setInterval(
    //   () => this.refreshDataFromBackend(authorisedActivities),
    //   1000
    // );
  };

  getThisUserAdminGroups = async () => {
    const response = await fetch("/api/userGroups/thisUserAdminGroups");

    // TODO: We need more elegant error handling throughout the whole application, but this will at least protect temporarily
    if (!response.ok) {
      return;
    }

    const responseJson = await response.json();

    this.setState({ thisUserAdminGroups: responseJson });
  };



  render() {


    return (
      <div style={{ padding: 20 }}>
        <Surveys></Surveys>
        <ExportFileTemplate></ExportFileTemplate>
        <SmsTemplatesList></SmsTemplatesList>
        <EmailTemplateList></EmailTemplateList>
        <ConfigureFulfilmentTrigger></ConfigureFulfilmentTrigger>

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
          "EXCEPTION_MANAGER_VIEWER"
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
