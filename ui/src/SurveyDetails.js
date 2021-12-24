import React, { Component } from "react";
import "@fontsource/roboto";
import {
  Button,
  Dialog,
  DialogContent,
  Paper,
  Typography,
  MenuItem,
  FormControl,
  InputLabel,
  Select,
} from "@material-ui/core";
import Table from "@material-ui/core/Table";
import TableBody from "@material-ui/core/TableBody";
import TableCell from "@material-ui/core/TableCell";
import TableContainer from "@material-ui/core/TableContainer";
import TableHead from "@material-ui/core/TableHead";
import TableRow from "@material-ui/core/TableRow";
import {
  getActionRuleExportFileTemplates,
  getActionRuleSmsTemplates,
  getActionRuleEmailTemplates,
  getAllExportFileTemplates,
  getFulfilmentExportFileTemplates,
  getSmsFulfilmentTemplates,
  getAllSmsTemplates,
  getEmailFulfilmentTemplates,
  getAllEmailTemplates,
} from "./Utils";
import { Link } from "react-router-dom";
import CollectionExerciseList from "./CollectionExerciseList";

class SurveyDetails extends Component {
  state = {
    authorisedActivities: [],
    surveyName: "",
    allowActionRuleExportFileTemplateDialogDisplayed: false,
    allowActionRuleSmsTemplateDialogDisplayed: false,
    allowActionRuleEmailTemplateDialogDisplayed: false,
    allowFulfilmentExportFileTemplateDialogDisplayed: false,
    allowSmsFulfilmentTemplateDialogDisplayed: false,
    allowEmailFulfilmentTemplateDialogDisplayed: false,
    actionRuleExportFileTemplates: [],
    actionRuleSmsTemplates: [],
    actionRuleEmailTemplates: [],
    fulfilmentExportFileTemplates: [],
    smsFulfilmentTemplates: [],
    emailFulfilmentTemplates: [],
    allowableActionRuleExportFileTemplates: [],
    allowableActionRuleSmsTemplates: [],
    allowableActionRuleEmailTemplates: [],
    allowableFulfilmentExportFileTemplates: [],
    allowableSmsFulfilmentTemplates: [],
    allowableEmailFulfilmentTemplates: [],
    exportFileTemplateToAllow: "",
    smsTemplateToAllow: "",
    emailTemplateToAllow: "",
    exportFileTemplateValidationError: false,
    smsTemplateValidationError: false,
    emailTemplateValidationError: false,
    allowExportFileTemplateError: "",
    allowSmsTemplateError: "",
    allowEmailTemplateError: "",
  };

  componentDidMount() {
    this.getAuthorisedBackendData();
  }

  componentWillUnmount() {
    clearInterval(this.interval);
  }

  getAuthorisedBackendData = async () => {
    const authorisedActivities = await this.getAuthorisedActivities(); // Only need to do this once; don't refresh it repeatedly as it changes infrequently
    this.getSurveyName(authorisedActivities); // Only need to do this once; don't refresh it repeatedly as it changes infrequently
    this.refreshDataFromBackend(authorisedActivities);

    this.interval = setInterval(
      () => this.refreshDataFromBackend(authorisedActivities),
      1000
    );
  };

  getAuthorisedActivities = async () => {
    const response = await fetch(`/api/auth?surveyId=${this.props.surveyId}`);

    // TODO: We need more elegant error handling throughout the whole application, but this will at least protect temporarily
    if (!response.ok) {
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

  refreshDataFromBackend = async (authorisedActivities) => {

    // TODO: The security of this is a nightmare, because the method assumes the user has a ton of permissions. Will work for now, but refactor in future.

    const allExportFileFulfilmentTemplates = await getAllExportFileTemplates(
      authorisedActivities
    );

    const allSmsFulfilmentTemplates = await getAllSmsTemplates(
      authorisedActivities
    );

    const allEmailFulfilmentTemplates = await getAllEmailTemplates(
      authorisedActivities
    );

    const actionRuleExportFileTemplates =
      await getActionRuleExportFileTemplates(
        authorisedActivities,
        this.props.surveyId
      );

    const actionRuleSmsTemplates = await getActionRuleSmsTemplates(
      authorisedActivities,
      this.props.surveyId
    );

    const actionRuleEmailTemplates = await getActionRuleEmailTemplates(
      authorisedActivities,
      this.props.surveyId
    );

    const fulfilmentExportFileTemplates =
      await getFulfilmentExportFileTemplates(
        authorisedActivities,
        this.props.surveyId
      );
    const smsFulfilmentTemplates = await getSmsFulfilmentTemplates(
      authorisedActivities,
      this.props.surveyId
    );
    const emailFulfilmentTemplates = await getEmailFulfilmentTemplates(
      authorisedActivities,
      this.props.surveyId
    );

    let allowableActionRuleExportFileTemplates = [];
    let allowableActionRuleSmsTemplates = [];
    let allowableActionRuleEmailTemplates = [];
    let allowableFulfilmentExportFileTemplates = [];
    let allowableSmsFulfilmentTemplates = [];
    let allowableEmailFulfilmentTemplates = [];

    allExportFileFulfilmentTemplates.forEach((packCode) => {
      if (!actionRuleExportFileTemplates.includes(packCode)) {
        allowableActionRuleExportFileTemplates.push(packCode);
      }

      if (!fulfilmentExportFileTemplates.includes(packCode)) {
        allowableFulfilmentExportFileTemplates.push(packCode);
      }
    });

    allSmsFulfilmentTemplates.forEach((packCode) => {
      if (!actionRuleSmsTemplates.includes(packCode)) {
        allowableActionRuleSmsTemplates.push(packCode);
      }

      if (!smsFulfilmentTemplates.includes(packCode)) {
        allowableSmsFulfilmentTemplates.push(packCode);
      }
    });

    allEmailFulfilmentTemplates.forEach((packCode) => {
      if (!actionRuleEmailTemplates.includes(packCode)) {
        allowableActionRuleEmailTemplates.push(packCode);
      }

      if (!emailFulfilmentTemplates.includes(packCode)) {
        allowableEmailFulfilmentTemplates.push(packCode);
      }
    });

    this.setState({
      actionRuleExportFileTemplates: actionRuleExportFileTemplates,
      actionRuleSmsTemplates: actionRuleSmsTemplates,
      actionRuleEmailTemplates: actionRuleEmailTemplates,
      fulfilmentExportFileTemplates: fulfilmentExportFileTemplates,
      smsFulfilmentTemplates: smsFulfilmentTemplates,
      emailFulfilmentTemplates: emailFulfilmentTemplates,
      allowableActionRuleExportFileTemplates:
        allowableActionRuleExportFileTemplates,
      allowableActionRuleSmsTemplates: allowableActionRuleSmsTemplates,
      allowableActionRuleEmailTemplates: allowableActionRuleEmailTemplates,
      allowableFulfilmentExportFileTemplates:
        allowableFulfilmentExportFileTemplates,
      allowableSmsFulfilmentTemplates: allowableSmsFulfilmentTemplates,
      allowableEmailFulfilmentTemplates: allowableEmailFulfilmentTemplates,
    });
  };


  getTimeNowForDateTimePicker = () => {
    var dateNow = new Date();
    dateNow.setMinutes(dateNow.getMinutes() - dateNow.getTimezoneOffset());
    return dateNow.toJSON().slice(0, 16);
  };

  openActionRuleExportFileTemplateDialog = () => {
    this.allowActionRuleExportFileTemplateInProgress = false;

    this.setState({
      allowActionRuleExportFileTemplateDialogDisplayed: true,
      exportFileTemplateToAllow: "",
      exportFileTemplateValidationError: false,
      allowExportFileTemplateError: "",
    });
  };

  openActionRuleSmsTemplateDialog = () => {
    this.allowActionRuleSmsTemplateInProgress = false;

    this.setState({
      allowActionRuleSmsTemplateDialogDisplayed: true,
      smsTemplateToAllow: "",
      smsTemplateValidationError: false,
      allowSmsTemplateError: "",
    });
  };

  openActionRuleEmailTemplateDialog = () => {
    this.allowActionRuleEmailTemplateInProgress = false;

    this.setState({
      allowActionRuleEmailTemplateDialogDisplayed: true,
      emailTemplateToAllow: "",
      emailTemplateValidationError: false,
      allowEmailTemplateError: "",
    });
  };

  openFulfilmentExportFileTemplateDialog = () => {
    this.allowFulfilmentExportFileTemplateInProgress = false;

    this.setState({
      allowFulfilmentExportFileTemplateDialogDisplayed: true,
      exportFileTemplateToAllow: "",
      exportFileTemplateValidationError: false,
      allowExportFileTemplateError: "",
    });
  };

  openSmsFulfilmentTemplateDialog = () => {
    this.allowSmsFulfilmentTemplateInProgress = false;

    this.setState({
      allowSmsFulfilmentTemplateDialogDisplayed: true,
      smsTemplateToAllow: "",
      exportFileTemplateValidationError: false,
      allowExportFileTemplateError: "",
    });
  };

  openEmailFulfilmentTemplateDialog = () => {
    this.allowEmailFulfilmentTemplateInProgress = false;

    this.setState({
      allowEmailFulfilmentTemplateDialogDisplayed: true,
      emailTemplateToAllow: "",
      exportFileTemplateValidationError: false,
      allowExportFileTemplateError: "",
    });
  };

  onAllowActionRuleExportFileTemplate = async () => {
    if (this.allowActionRuleExportFileTemplateInProgress) {
      return;
    }

    this.allowActionRuleExportFileTemplateInProgress = true;

    if (!this.state.exportFileTemplateToAllow) {
      this.setState({
        exportFileTemplateValidationError: true,
      });

      this.allowActionRuleExportFileTemplateInProgress = false;
      return;
    }

    const newAllowExportFileTemplate = {
      surveyId: this.props.surveyId,
      packCode: this.state.exportFileTemplateToAllow,
    };

    const response = await fetch("/api/actionRuleSurveyExportFileTemplates", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(newAllowExportFileTemplate),
    });

    if (response.ok) {
      this.setState({
        allowActionRuleExportFileTemplateDialogDisplayed: false,
      });
    } else {
      const errorMessage = await response.text();
      this.setState({
        allowExportFileTemplateError: errorMessage,
      });
      this.allowActionRuleExportFileTemplateInProgress = false;
    }
  };

  onAllowActionRuleSmsTemplate = async () => {
    if (this.allowActionRuleSmsTemplateInProgress) {
      return;
    }

    this.allowActionRuleSmsTemplateInProgress = true;

    if (!this.state.smsTemplateToAllow) {
      this.setState({
        smsTemplateValidationError: true,
      });

      this.allowActionRuleSmsTemplateInProgress = false;
      return;
    }

    const newAllowSmsTemplate = {
      surveyId: this.props.surveyId,
      packCode: this.state.smsTemplateToAllow,
    };

    const response = await fetch("/api/actionRuleSurveySmsTemplates", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(newAllowSmsTemplate),
    });

    if (response.ok) {
      this.setState({
        allowActionRuleSmsTemplateDialogDisplayed: false,
      });
    } else {
      const errorMessage = await response.text();
      this.setState({
        allowSmsTemplateError: errorMessage,
      });
      this.allowActionRuleSmsTemplateInProgress = false;
    }
  };

  onAllowActionRuleEmailTemplate = async () => {
    if (this.allowActionRuleEmailTemplateInProgress) {
      return;
    }

    this.allowActionRuleEmailTemplateInProgress = true;

    if (!this.state.emailTemplateToAllow) {
      this.setState({
        emailTemplateValidationError: true,
      });

      this.allowActionRuleEmailTemplateInProgress = false;
      return;
    }

    const newAllowEmailTemplate = {
      surveyId: this.props.surveyId,
      packCode: this.state.emailTemplateToAllow,
    };

    const response = await fetch("/api/actionRuleSurveyEmailTemplates", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(newAllowEmailTemplate),
    });

    if (response.ok) {
      this.setState({ allowActionRuleEmailTemplateDialogDisplayed: false });
    } else {
      const errorMessage = await response.text();
      this.setState({
        allowEmailTemplateError: errorMessage,
      });
      this.allowActionRuleEmailTemplateInProgress = false;
    }
  };

  openSmsFulfilmentTemplateDialog = () => {
    this.allowSmsFulfilmentTemplateInProgress = false;

    this.setState({
      allowSmsFulfilmentTemplateDialogDisplayed: true,
      smsTemplateToAllow: "",
      exportFileTemplateValidationError: false,
      allowExportFileTemplateError: "",
    });
  };

  openEmailFulfilmentTemplateDialog = () => {
    this.allowEmailFulfilmentTemplateInProgress = false;

    this.setState({
      allowEmailFulfilmentTemplateDialogDisplayed: true,
      emailTemplateToAllow: "",
      exportFileTemplateValidationError: false,
      allowExportFileTemplateError: "",
    });
  };

  onAllowFulfilmentExportFileTemplate = async () => {
    if (this.allowFulfilmentExportFileTemplateInProgress) {
      return;
    }

    this.allowFulfilmentExportFileTemplateInProgress = true;

    if (!this.state.exportFileTemplateToAllow) {
      this.setState({
        exportFileTemplateValidationError: true,
      });

      this.allowFulfilmentExportFileTemplateInProgress = false;
      return;
    }

    const newAllowExportFileTemplate = {
      surveyId: this.props.surveyId,
      packCode: this.state.exportFileTemplateToAllow,
    };

    const response = await fetch("/api/fulfilmentSurveyExportFileTemplates", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(newAllowExportFileTemplate),
    });

    if (response.ok) {
      this.setState({
        allowFulfilmentExportFileTemplateDialogDisplayed: false,
      });
    } else {
      const errorMessage = await response.text();
      this.setState({
        allowExportFileTemplateError: errorMessage,
      });
      this.allowFulfilmentExportFileTemplateInProgress = false;
    }
  };

  onAllowSmsFulfilmentTemplate = async () => {
    if (this.allowSmsFulfilmentTemplateInProgress) {
      return;
    }

    this.allowSmsFulfilmentTemplateInProgress = true;

    if (!this.state.smsTemplateToAllow) {
      this.setState({
        exportFileTemplateValidationError: true,
      });

      this.allowSmsFulfilmentTemplateInProgress = false;
      return;
    }

    const newAllowSmsTemplate = {
      surveyId: this.props.surveyId,
      packCode: this.state.smsTemplateToAllow,
    };

    const response = await fetch("/api/fulfilmentSurveySmsTemplates", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(newAllowSmsTemplate),
    });

    if (response.ok) {
      this.setState({ allowSmsFulfilmentTemplateDialogDisplayed: false });
    } else {
      const errorMessage = await response.text();
      this.setState({
        allowExportFileTemplateError: errorMessage,
      });
      this.allowSmsFulfilmentTemplateInProgress = false;
    }
  };

  onAllowEmailFulfilmentTemplate = async () => {
    if (this.allowEmailFulfilmentTemplateInProgress) {
      return;
    }

    this.allowEmailFulfilmentTemplateInProgress = true;

    if (!this.state.emailTemplateToAllow) {
      this.setState({
        exportFileTemplateValidationError: true,
      });

      this.allowEmailFulfilmentTemplateInProgress = false;
      return;
    }

    const newAllowEmailTemplate = {
      surveyId: this.props.surveyId,
      packCode: this.state.emailTemplateToAllow,
    };

    const response = await fetch("/api/fulfilmentSurveyEmailTemplates", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(newAllowEmailTemplate),
    });

    if (response.ok) {
      this.setState({ allowEmailFulfilmentTemplateDialogDisplayed: false });
    } else {
      const errorMessage = await response.text();
      this.setState({
        allowExportFileTemplateError: errorMessage,
      });
      this.allowEmailFulfilmentTemplateInProgress = false;
    }
  };

  closeAllowActionRuleExportFileTemplateDialog = () => {
    this.setState({ allowActionRuleExportFileTemplateDialogDisplayed: false });
  };

  closeAllowActionRuleSmsTemplateDialog = () => {
    this.setState({ allowActionRuleSmsTemplateDialogDisplayed: false });
  };

  closeAllowActionRuleEmailTemplateDialog = () => {
    this.setState({ allowActionRuleEmailTemplateDialogDisplayed: false });
  };

  closeAllowFulfilmentExportFileTemplateDialog = () => {
    this.setState({ allowFulfilmentExportFileTemplateDialogDisplayed: false });
  };

  closeAllowSmsFulfilmentTemplateDialog = () => {
    this.setState({ allowSmsFulfilmentTemplateDialogDisplayed: false });
  };

  closeAllowEmailFulfilmentTemplateDialog = () => {
    this.setState({ allowEmailFulfilmentTemplateDialogDisplayed: false });
  };

  onExportFileTemplateChange = (event) => {
    this.setState({ exportFileTemplateToAllow: event.target.value });
  };

  onSmsTemplateChange = (event) => {
    this.setState({ smsTemplateToAllow: event.target.value });
  };

  onEmailTemplateChange = (event) => {
    this.setState({ emailTemplateToAllow: event.target.value });
  };

  render() {


    const actionRuleExportFileTemplateTableRows =
      this.state.actionRuleExportFileTemplates.map((exportFileTemplate) => (
        <TableRow key={exportFileTemplate}>
          <TableCell component="th" scope="row">
            {exportFileTemplate}
          </TableCell>
        </TableRow>
      ));

    const actionRuleSmsTemplateTableRows =
      this.state.actionRuleSmsTemplates.map((smsTemplate) => (
        <TableRow key={smsTemplate}>
          <TableCell component="th" scope="row">
            {smsTemplate}
          </TableCell>
        </TableRow>
      ));

    const actionRuleEmailTemplateTableRows =
      this.state.actionRuleEmailTemplates.map((emailTemplate) => (
        <TableRow key={emailTemplate}>
          <TableCell component="th" scope="row">
            {emailTemplate}
          </TableCell>
        </TableRow>
      ));

    const fulfilmentExportFileTemplateTableRows =
      this.state.fulfilmentExportFileTemplates.map((exportFileTemplate) => (
        <TableRow key={exportFileTemplate}>
          <TableCell component="th" scope="row">
            {exportFileTemplate}
          </TableCell>
        </TableRow>
      ));

    const smsFulfilmentTemplateTableRows =
      this.state.smsFulfilmentTemplates.map((smsTemplate) => (
        <TableRow key={smsTemplate}>
          <TableCell component="th" scope="row">
            {smsTemplate}
          </TableCell>
        </TableRow>
      ));

    const emailFulfilmentTemplateTableRows =
      this.state.emailFulfilmentTemplates.map((emailTemplate) => (
        <TableRow key={emailTemplate}>
          <TableCell component="th" scope="row">
            {emailTemplate}
          </TableCell>
        </TableRow>
      ));

    const actionRuleExportFileTemplateMenuItems =
      this.state.allowableActionRuleExportFileTemplates.map((packCode) => (
        <MenuItem key={packCode} value={packCode}>
          {packCode}
        </MenuItem>
      ));

    const actionRuleSmsTemplateMenuItems =
      this.state.allowableActionRuleSmsTemplates.map((packCode) => (
        <MenuItem key={packCode} value={packCode}>
          {packCode}
        </MenuItem>
      ));

    const actionRuleEmailTemplateMenuItems =
      this.state.allowableActionRuleEmailTemplates.map((packCode) => (
        <MenuItem key={packCode} value={packCode}>
          {packCode}
        </MenuItem>
      ));

    const fulfilmentExportFileTemplateMenuItems =
      this.state.allowableFulfilmentExportFileTemplates.map((packCode) => (
        <MenuItem key={packCode} value={packCode}>
          {packCode}
        </MenuItem>
      ));

    const smsFulfilmentTemplateMenuItems =
      this.state.allowableSmsFulfilmentTemplates.map((packCode) => (
        <MenuItem key={packCode} value={packCode}>
          {packCode}
        </MenuItem>
      ));

    const emailFulfilmentTemplateMenuItems =
      this.state.allowableEmailFulfilmentTemplates.map((packCode) => (
        <MenuItem key={packCode} value={packCode}>
          {packCode}
        </MenuItem>
      ));

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
        <CollectionExerciseList surveyId={this.props.surveyId}></CollectionExerciseList>

        {this.state.authorisedActivities.includes(
          "LIST_ALLOWED_EXPORT_FILE_TEMPLATES_ON_ACTION_RULES"
        ) && (
            <>
              <Typography variant="h6" color="inherit" style={{ marginTop: 20 }}>
                Export File Templates Allowed on Action Rules
              </Typography>
              <TableContainer component={Paper}>
                <Table>
                  <TableHead>
                    <TableRow>
                      <TableCell>Pack Code</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>{actionRuleExportFileTemplateTableRows}</TableBody>
                </Table>
              </TableContainer>
            </>
          )}
        {this.state.authorisedActivities.includes(
          "ALLOW_EXPORT_FILE_TEMPLATE_ON_ACTION_RULE"
        ) && (
            <Button
              variant="contained"
              onClick={this.openActionRuleExportFileTemplateDialog}
              style={{ marginTop: 10 }}
            >
              Allow Export File Template on Action Rule
            </Button>
          )}

        {this.state.authorisedActivities.includes(
          "LIST_ALLOWED_SMS_TEMPLATES_ON_ACTION_RULES"
        ) && (
            <>
              <Typography variant="h6" color="inherit" style={{ marginTop: 20 }}>
                SMS Templates Allowed on Action Rules
              </Typography>
              <TableContainer component={Paper}>
                <Table>
                  <TableHead>
                    <TableRow>
                      <TableCell>Pack Code</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>{actionRuleSmsTemplateTableRows}</TableBody>
                </Table>
              </TableContainer>
            </>
          )}

        {this.state.authorisedActivities.includes(
          "ALLOW_SMS_TEMPLATE_ON_ACTION_RULE"
        ) && (
            <Button
              variant="contained"
              onClick={this.openActionRuleSmsTemplateDialog}
              style={{ marginTop: 10 }}
            >
              Allow SMS Template on Action Rule
            </Button>
          )}

        {this.state.authorisedActivities.includes(
          "LIST_ALLOWED_EMAIL_TEMPLATES_ON_ACTION_RULES"
        ) && (
            <>
              <Typography variant="h6" color="inherit" style={{ marginTop: 20 }}>
                Email Templates Allowed on Action Rules
              </Typography>
              <TableContainer component={Paper}>
                <Table>
                  <TableHead>
                    <TableRow>
                      <TableCell>Pack Code</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>{actionRuleEmailTemplateTableRows}</TableBody>
                </Table>
              </TableContainer>
            </>
          )}

        {this.state.authorisedActivities.includes(
          "ALLOW_EMAIL_TEMPLATE_ON_ACTION_RULE"
        ) && (
            <Button
              variant="contained"
              onClick={this.openActionRuleEmailTemplateDialog}
              style={{ marginTop: 10 }}
            >
              Allow Email Template on Action Rule
            </Button>
          )}

        {this.state.authorisedActivities.includes(
          "LIST_ALLOWED_EXPORT_FILE_TEMPLATES_ON_FULFILMENTS"
        ) && (
            <>
              <Typography variant="h6" color="inherit" style={{ marginTop: 20 }}>
                Export File Templates Allowed on Fulfilments
              </Typography>
              <TableContainer component={Paper}>
                <Table>
                  <TableHead>
                    <TableRow>
                      <TableCell>Pack Code</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>{fulfilmentExportFileTemplateTableRows}</TableBody>
                </Table>
              </TableContainer>
            </>
          )}
        {this.state.authorisedActivities.includes(
          "ALLOW_EXPORT_FILE_TEMPLATE_ON_FULFILMENT"
        ) && (
            <Button
              variant="contained"
              onClick={this.openFulfilmentExportFileTemplateDialog}
              style={{ marginTop: 10 }}
            >
              Allow Export File Template on Fulfilment
            </Button>
          )}

        {this.state.authorisedActivities.includes(
          "LIST_ALLOWED_SMS_TEMPLATES_ON_FULFILMENTS"
        ) && (
            <>
              <Typography variant="h6" color="inherit" style={{ marginTop: 20 }}>
                SMS Templates Allowed on Fulfilments
              </Typography>
              <TableContainer component={Paper}>
                <Table>
                  <TableHead>
                    <TableRow>
                      <TableCell>Pack Code</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>{smsFulfilmentTemplateTableRows}</TableBody>
                </Table>
              </TableContainer>
            </>
          )}

        {this.state.authorisedActivities.includes(
          "ALLOW_SMS_TEMPLATE_ON_FULFILMENT"
        ) && (
            <Button
              variant="contained"
              onClick={this.openSmsFulfilmentTemplateDialog}
              style={{ marginTop: 10 }}
            >
              Allow SMS Template on Fulfilment
            </Button>
          )}

        {this.state.authorisedActivities.includes(
          "LIST_ALLOWED_EMAIL_TEMPLATES_ON_FULFILMENTS"
        ) && (
            <>
              <Typography variant="h6" color="inherit" style={{ marginTop: 20 }}>
                Email Templates Allowed on Fulfilments
              </Typography>
              <TableContainer component={Paper}>
                <Table>
                  <TableHead>
                    <TableRow>
                      <TableCell>Pack Code</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>{emailFulfilmentTemplateTableRows}</TableBody>
                </Table>
              </TableContainer>
            </>
          )}

        {this.state.authorisedActivities.includes(
          "ALLOW_EMAIL_TEMPLATE_ON_FULFILMENT"
        ) && (
            <Button
              variant="contained"
              onClick={this.openEmailFulfilmentTemplateDialog}
              style={{ marginTop: 10 }}
            >
              Allow Email Template on Fulfilment
            </Button>
          )}

        <Dialog
          open={this.state.allowActionRuleExportFileTemplateDialogDisplayed}
        >
          <DialogContent style={{ padding: 30 }}>
            <div>
              <div>
                <FormControl required fullWidth={true}>
                  <InputLabel>Export File Template</InputLabel>
                  <Select
                    onChange={this.onExportFileTemplateChange}
                    value={this.state.exportFileTemplateToAllow}
                    error={this.state.exportFileTemplateValidationError}
                  >
                    {actionRuleExportFileTemplateMenuItems}
                  </Select>
                </FormControl>
              </div>
              {this.state.allowExportFileTemplateError && (
                <div>
                  <p style={{ color: "red" }}>
                    {this.state.allowExportFileTemplateError}
                  </p>
                </div>
              )}
              <div style={{ marginTop: 10 }}>
                <Button
                  onClick={this.onAllowActionRuleExportFileTemplate}
                  variant="contained"
                  style={{ margin: 10 }}
                >
                  Allow
                </Button>
                <Button
                  onClick={this.closeAllowActionRuleExportFileTemplateDialog}
                  variant="contained"
                  style={{ margin: 10 }}
                >
                  Cancel
                </Button>
              </div>
            </div>
          </DialogContent>
        </Dialog>
        <Dialog open={this.state.allowActionRuleSmsTemplateDialogDisplayed}>
          <DialogContent style={{ padding: 30 }}>
            <div>
              <div>
                <FormControl required fullWidth={true}>
                  <InputLabel>SMS Template</InputLabel>
                  <Select
                    onChange={this.onSmsTemplateChange}
                    value={this.state.smsTemplateToAllow}
                    error={this.state.smsTemplateValidationError}
                  >
                    {actionRuleSmsTemplateMenuItems}
                  </Select>
                </FormControl>
              </div>
              {this.state.allowSmsTemplateError && (
                <div>
                  <p style={{ color: "red" }}>
                    {this.state.allowSmsTemplateError}
                  </p>
                </div>
              )}
              <div style={{ marginTop: 10 }}>
                <Button
                  onClick={this.onAllowActionRuleSmsTemplate}
                  variant="contained"
                  style={{ margin: 10 }}
                >
                  Allow
                </Button>
                <Button
                  onClick={this.closeAllowActionRuleSmsTemplateDialog}
                  variant="contained"
                  style={{ margin: 10 }}
                >
                  Cancel
                </Button>
              </div>
            </div>
          </DialogContent>
        </Dialog>
        <Dialog open={this.state.allowActionRuleEmailTemplateDialogDisplayed}>
          <DialogContent style={{ padding: 30 }}>
            <div>
              <div>
                <FormControl required fullWidth={true}>
                  <InputLabel>Email Template</InputLabel>
                  <Select
                    onChange={this.onEmailTemplateChange}
                    value={this.state.emailTemplateToAllow}
                    error={this.state.emailTemplateValidationError}
                  >
                    {actionRuleEmailTemplateMenuItems}
                  </Select>
                </FormControl>
              </div>
              {this.state.allowEmailTemplateError && (
                <div>
                  <p style={{ color: "red" }}>
                    {this.state.allowEmailTemplateError}
                  </p>
                </div>
              )}
              <div style={{ marginTop: 10 }}>
                <Button
                  onClick={this.onAllowActionRuleEmailTemplate}
                  variant="contained"
                  style={{ margin: 10 }}
                >
                  Allow
                </Button>
                <Button
                  onClick={this.closeAllowActionRuleEmailTemplateDialog}
                  variant="contained"
                  style={{ margin: 10 }}
                >
                  Cancel
                </Button>
              </div>
            </div>
          </DialogContent>
        </Dialog>
        <Dialog
          open={this.state.allowFulfilmentExportFileTemplateDialogDisplayed}
        >
          <DialogContent style={{ padding: 30 }}>
            <div>
              <div>
                <FormControl required fullWidth={true}>
                  <InputLabel>Export File Template</InputLabel>
                  <Select
                    onChange={this.onExportFileTemplateChange}
                    value={this.state.exportFileTemplateToAllow}
                    error={this.state.exportFileTemplateValidationError}
                  >
                    {fulfilmentExportFileTemplateMenuItems}
                  </Select>
                </FormControl>
              </div>
              {this.state.allowExportFileTemplateError && (
                <div>
                  <p style={{ color: "red" }}>
                    {this.state.allowExportFileTemplateError}
                  </p>
                </div>
              )}
              <div style={{ marginTop: 10 }}>
                <Button
                  onClick={this.onAllowFulfilmentExportFileTemplate}
                  variant="contained"
                  style={{ margin: 10 }}
                >
                  Allow
                </Button>
                <Button
                  onClick={this.closeAllowFulfilmentExportFileTemplateDialog}
                  variant="contained"
                  style={{ margin: 10 }}
                >
                  Cancel
                </Button>
              </div>
            </div>
          </DialogContent>
        </Dialog>
        <Dialog open={this.state.allowSmsFulfilmentTemplateDialogDisplayed}>
          <DialogContent style={{ padding: 30 }}>
            <div>
              <div>
                <FormControl required fullWidth={true}>
                  <InputLabel>SMS Template</InputLabel>
                  <Select
                    onChange={this.onSmsTemplateChange}
                    value={this.state.smsTemplateToAllow}
                    error={this.state.exportFileTemplateValidationError}
                  >
                    {smsFulfilmentTemplateMenuItems}
                  </Select>
                </FormControl>
              </div>
              {this.state.allowExportFileTemplateError && (
                <p style={{ color: "red" }}>
                  {this.state.allowExportFileTemplateError}
                </p>
              )}
              <div style={{ marginTop: 10 }}>
                <Button
                  onClick={this.onAllowSmsFulfilmentTemplate}
                  variant="contained"
                  style={{ margin: 10 }}
                >
                  Allow
                </Button>
                <Button
                  onClick={this.closeAllowSmsFulfilmentTemplateDialog}
                  variant="contained"
                  style={{ margin: 10 }}
                >
                  Cancel
                </Button>
              </div>
            </div>
          </DialogContent>
        </Dialog>
        <Dialog open={this.state.allowEmailFulfilmentTemplateDialogDisplayed}>
          <DialogContent style={{ padding: 30 }}>
            <div>
              <div>
                <FormControl required fullWidth={true}>
                  <InputLabel>Email Template</InputLabel>
                  <Select
                    onChange={this.onEmailTemplateChange}
                    value={this.state.emailTemplateToAllow}
                    error={this.state.exportFileTemplateValidationError}
                  >
                    {emailFulfilmentTemplateMenuItems}
                  </Select>
                </FormControl>
              </div>
              {this.state.allowExportFileTemplateError && (
                <p style={{ color: "red" }}>
                  {this.state.allowExportFileTemplateError}
                </p>
              )}
              <div style={{ marginTop: 10 }}>
                <Button
                  onClick={this.onAllowEmailFulfilmentTemplate}
                  variant="contained"
                  style={{ margin: 10 }}
                >
                  Allow
                </Button>
                <Button
                  onClick={this.closeAllowEmailFulfilmentTemplateDialog}
                  variant="contained"
                  style={{ margin: 10 }}
                >
                  Cancel
                </Button>
              </div>
            </div>
          </DialogContent>
        </Dialog>
      </div>
    );
  }
}

export default SurveyDetails;
