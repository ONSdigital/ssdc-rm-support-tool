import React, { Component } from "react";
import "@fontsource/roboto";
import {
  Button,
  Dialog,
  DialogContent,
  TextField,
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
  getActionRulePrintTemplates,
  getActionRuleSmsTemplates,
  getAllPrintTemplates,
  getFulfilmentPrintTemplates,
  getSmsFulfilmentTemplates,
  getAllSmsTemplates,
} from "./Utils";
import { Link } from "react-router-dom";

class SurveyDetails extends Component {
  state = {
    authorisedActivities: [],
    surveyName: "",
    collectionExercises: [],
    createCollectionExerciseDialogDisplayed: false,
    validationError: false,
    newCollectionExerciseName: "",
    allowActionRulePrintTemplateDialogDisplayed: false,
    allowActionRuleSmsTemplateDialogDisplayed: false,
    allowFulfilmentPrintTemplateDialogDisplayed: false,
    allowSmsFulfilmentTemplateDialogDisplayed: false,
    actionRulePrintTemplates: [],
    actionRuleSmsTemplates: [],
    fulfilmentPrintTemplates: [],
    smsFulfilmentTemplates: [],
    allowableActionRulePrintTemplates: [],
    allowableActionRuleSmsTemplates: [],
    allowableFulfilmentPrintTemplates: [],
    allowableSmsFulfilmentTemplates: [],
    printTemplateToAllow: "",
    smsTemplateToAllow: "",
    printTemplateValidationError: false,
    smsTemplateValidationError: false,
    allowPrintTemplateError: "",
    allowSmsTemplateError: "",
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
    this.getCollectionExercises(authorisedActivities);

    // TODO: The security of this is a nightmare, because the method assumes the user has a ton of permissions. Will work for now, but refactor in future.

    const allPrintFulfilmentTemplates = await getAllPrintTemplates(
      authorisedActivities
    );
    const allSmsFulfilmentTemplates = await getAllSmsTemplates(
      authorisedActivities
    );

    const actionRulePrintTemplates = await getActionRulePrintTemplates(
      authorisedActivities,
      this.props.surveyId
    );

    const actionRuleSmsTemplates = await getActionRuleSmsTemplates(
      authorisedActivities,
      this.props.surveyId
    );

    const fulfilmentPrintTemplates = await getFulfilmentPrintTemplates(
      authorisedActivities,
      this.props.surveyId
    );
    const smsFulfilmentTemplates = await getSmsFulfilmentTemplates(
      authorisedActivities,
      this.props.surveyId
    );

    let allowableActionRulePrintTemplates = [];
    let allowableActionRuleSmsTemplates = [];
    let allowableFulfilmentPrintTemplates = [];
    let allowableSmsFulfilmentTemplates = [];
    allPrintFulfilmentTemplates.forEach((packCode) => {
      if (!actionRulePrintTemplates.includes(packCode)) {
        allowableActionRulePrintTemplates.push(packCode);
      }

      if (!fulfilmentPrintTemplates.includes(packCode)) {
        allowableFulfilmentPrintTemplates.push(packCode);
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

    this.setState({
      actionRulePrintTemplates: actionRulePrintTemplates,
      actionRuleSmsTemplates: actionRuleSmsTemplates,
      fulfilmentPrintTemplates: fulfilmentPrintTemplates,
      smsFulfilmentTemplates: smsFulfilmentTemplates,
      allowableActionRulePrintTemplates: allowableActionRulePrintTemplates,
      allowableActionRuleSmsTemplates: allowableActionRuleSmsTemplates,
      allowableFulfilmentPrintTemplates: allowableFulfilmentPrintTemplates,
      allowableSmsFulfilmentTemplates: allowableSmsFulfilmentTemplates,
    });
  };

  getCollectionExercises = async (authorisedActivities) => {
    if (!authorisedActivities.includes("LIST_COLLECTION_EXERCISES")) return;

    const response = await fetch(
      `/api/collectionExercises/?surveyId=${this.props.surveyId}`
    );
    const collexJson = await response.json();

    this.setState({
      collectionExercises: collexJson,
    });
  };

  openDialog = () => {
    this.setState({
      newCollectionExerciseName: "",
      validationError: false,
      createCollectionExerciseDialogDisplayed: true,
    });
  };

  closeDialog = () => {
    this.setState({ createCollectionExerciseDialogDisplayed: false });
  };

  onNewCollectionExerciseNameChange = (event) => {
    const resetValidation = !event.target.value.trim();
    this.setState({
      validationError: resetValidation,
      newCollectionExerciseName: event.target.value,
    });
  };

  onCreateCollectionExercise = async () => {
    if (!this.state.newCollectionExerciseName.trim()) {
      this.setState({ validationError: true });
      return;
    }

    const newCollectionExercise = {
      name: this.state.newCollectionExerciseName,
      surveyId: this.props.surveyId,
    };

    const response = await fetch("/api/collectionExercises", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(newCollectionExercise),
    });

    if (response.ok) {
      this.setState({ createCollectionExerciseDialogDisplayed: false });
    }
  };

  openActionRulePrintTemplateDialog = () => {
    this.setState({
      allowActionRulePrintTemplateDialogDisplayed: true,
      printTemplateToAllow: "",
      printTemplateValidationError: false,
      allowPrintTemplateError: "",
    });
  };

  openActionRuleSmsTemplateDialog = () => {
    this.setState({
      allowActionRuleSmsTemplateDialogDisplayed: true,
      smsTemplateToAllow: "",
      smsTemplateValidationError: false,
      allowSmsTemplateError: "",
    });
  };

  openFulfilmentPrintTemplateDialog = () => {
    this.setState({
      allowFulfilmentPrintTemplateDialogDisplayed: true,
      printTemplateToAllow: "",
      printTemplateValidationError: false,
      allowPrintTemplateError: "",
    });
  };

  openSmsFulfilmentTemplateDialog = () => {
    this.setState({
      allowSmsFulfilmentTemplateDialogDisplayed: true,
      smsTemplateToAllow: "",
      printTemplateValidationError: false,
      allowPrintTemplateError: "",
    });
  };

  onAllowActionRulePrintTemplate = async () => {
    if (!this.state.printTemplateToAllow) {
      this.setState({
        printTemplateValidationError: true,
      });

      return;
    }

    const newAllowPrintTemplate = {
      surveyId: this.props.surveyId,
      packCode: this.state.printTemplateToAllow,
    };

    const response = await fetch("/api/actionRuleSurveyPrintTemplates", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(newAllowPrintTemplate),
    });

    if (response.ok) {
      this.setState({ allowActionRulePrintTemplateDialogDisplayed: false });
    } else {
      const errorMessage = await response.text();
      this.setState({
        allowPrintTemplateError: errorMessage,
      });
    }
  };

  onAllowActionRuleSmsTemplate = async () => {
    if (!this.state.smsTemplateToAllow) {
      this.setState({
        smsTemplateValidationError: true,
      });

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
      this.setState({ allowActionRuleSmsTemplateDialogDisplayed: false });
    } else {
      const errorMessage = await response.text();
      this.setState({
        allowSmsTemplateError: errorMessage,
      });
    }
  };

  onAllowFulfilmentPrintTemplate = async () => {
    if (!this.state.printTemplateToAllow) {
      this.setState({
        printTemplateValidationError: true,
      });

      return;
    }

    const newAllowPrintTemplate = {
      surveyId: this.props.surveyId,
      packCode: this.state.printTemplateToAllow,
    };

    const response = await fetch("/api/fulfilmentSurveyPrintTemplates", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(newAllowPrintTemplate),
    });

    if (response.ok) {
      this.setState({ allowFulfilmentPrintTemplateDialogDisplayed: false });
    } else {
      const errorMessage = await response.text();
      this.setState({
        allowPrintTemplateError: errorMessage,
      });
    }
  };

  onAllowSmsFulfilmentTemplate = async () => {
    if (!this.state.smsTemplateToAllow) {
      this.setState({
        printTemplateValidationError: true,
      });

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
        allowPrintTemplateError: errorMessage,
      });
    }
  };

  closeAllowActionRulePrintTemplateDialog = () => {
    this.setState({ allowActionRulePrintTemplateDialogDisplayed: false });
  };

  closeAllowActionRuleSmsTemplateDialog = () => {
    this.setState({ allowActionRuleSmsTemplateDialogDisplayed: false });
  };

  closeAllowFulfilmentPrintTemplateDialog = () => {
    this.setState({ allowFulfilmentPrintTemplateDialogDisplayed: false });
  };

  closeAllowSmsFulfilmentTemplateDialog = () => {
    this.setState({ allowSmsFulfilmentTemplateDialogDisplayed: false });
  };

  onPrintTemplateChange = (event) => {
    this.setState({ printTemplateToAllow: event.target.value });
  };

  onSmsTemplateChange = (event) => {
    this.setState({ smsTemplateToAllow: event.target.value });
  };

  render() {
    const collectionExerciseTableRows = this.state.collectionExercises.map(
      (collex) => (
        <TableRow key={collex.name}>
          <TableCell component="th" scope="row">
            <Link
              to={`/collex?surveyId=${this.props.surveyId}&collexId=${collex.id}`}
            >
              {collex.name}
            </Link>
          </TableCell>
        </TableRow>
      )
    );

    const actionRulePrintTemplateTableRows =
      this.state.actionRulePrintTemplates.map((printTemplate) => (
        <TableRow key={printTemplate}>
          <TableCell component="th" scope="row">
            {printTemplate}
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

    const fulfilmentPrintTemplateTableRows =
      this.state.fulfilmentPrintTemplates.map((printTemplate) => (
        <TableRow key={printTemplate}>
          <TableCell component="th" scope="row">
            {printTemplate}
          </TableCell>
        </TableRow>
      ));

    const smsFulfilmentTemplateTableRows =
      this.state.smsFulfilmentTemplates.map((printTemplate) => (
        <TableRow key={printTemplate}>
          <TableCell component="th" scope="row">
            {printTemplate}
          </TableCell>
        </TableRow>
      ));

    const actionRulePrintTemplateMenuItems =
      this.state.allowableActionRulePrintTemplates.map((packCode) => (
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

    const fulfilmentPrintTemplateMenuItems =
      this.state.allowableFulfilmentPrintTemplates.map((packCode) => (
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
        {this.state.authorisedActivities.includes(
          "LIST_COLLECTION_EXERCISES"
        ) && (
          <>
            <Typography variant="h6" color="inherit" style={{ marginTop: 10 }}>
              Collection Exercises
            </Typography>
            <TableContainer component={Paper}>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>Collection Exercise Name</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>{collectionExerciseTableRows}</TableBody>
              </Table>
            </TableContainer>
          </>
        )}
        {this.state.authorisedActivities.includes(
          "CREATE_COLLECTION_EXERCISE"
        ) && (
          <Button
            variant="contained"
            onClick={this.openDialog}
            style={{ marginTop: 10 }}
          >
            Create Collection Exercise
          </Button>
        )}

        {this.state.authorisedActivities.includes(
          "LIST_ALLOWED_PRINT_TEMPLATES_ON_ACTION_RULES"
        ) && (
          <>
            <Typography variant="h6" color="inherit" style={{ marginTop: 20 }}>
              Print Templates Allowed on Action Rules
            </Typography>
            <TableContainer component={Paper}>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>Pack Code</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>{actionRulePrintTemplateTableRows}</TableBody>
              </Table>
            </TableContainer>
          </>
        )}
        {this.state.authorisedActivities.includes(
          "ALLOW_PRINT_TEMPLATE_ON_ACTION_RULE"
        ) && (
          <Button
            variant="contained"
            onClick={this.openActionRulePrintTemplateDialog}
            style={{ marginTop: 10 }}
          >
            Allow Print Template on Action Rule
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
          "LIST_ALLOWED_PRINT_TEMPLATES_ON_FULFILMENTS"
        ) && (
          <>
            <Typography variant="h6" color="inherit" style={{ marginTop: 20 }}>
              Print Templates Allowed on Fulfilments
            </Typography>
            <TableContainer component={Paper}>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>Pack Code</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>{fulfilmentPrintTemplateTableRows}</TableBody>
              </Table>
            </TableContainer>
          </>
        )}
        {this.state.authorisedActivities.includes(
          "ALLOW_PRINT_TEMPLATE_ON_FULFILMENT"
        ) && (
          <Button
            variant="contained"
            onClick={this.openFulfilmentPrintTemplateDialog}
            style={{ marginTop: 10 }}
          >
            Allow Print Template on Fulfilment
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

        <Dialog open={this.state.createCollectionExerciseDialogDisplayed}>
          <DialogContent style={{ padding: 30 }}>
            <div>
              <div>
                <TextField
                  required
                  fullWidth={true}
                  error={this.state.validationError}
                  id="standard-required"
                  label="Collection exercise name"
                  onChange={this.onNewCollectionExerciseNameChange}
                  value={this.state.newCollectionExerciseName}
                />
              </div>
              <div style={{ marginTop: 10 }}>
                <Button
                  onClick={this.onCreateCollectionExercise}
                  variant="contained"
                  style={{ margin: 10 }}
                >
                  Create collection exercise
                </Button>
                <Button
                  onClick={this.closeDialog}
                  variant="contained"
                  style={{ margin: 10 }}
                >
                  Cancel
                </Button>
              </div>
            </div>
          </DialogContent>
        </Dialog>
        <Dialog open={this.state.allowActionRulePrintTemplateDialogDisplayed}>
          <DialogContent style={{ padding: 30 }}>
            <div>
              <div>
                <FormControl required fullWidth={true}>
                  <InputLabel>Print Template</InputLabel>
                  <Select
                    onChange={this.onPrintTemplateChange}
                    value={this.state.printTemplateToAllow}
                    error={this.state.printTemplateValidationError}
                  >
                    {actionRulePrintTemplateMenuItems}
                  </Select>
                </FormControl>
              </div>
              {this.state.allowPrintTemplateError && (
                <div>
                  <p style={{ color: "red" }}>
                    {this.state.allowPrintTemplateError}
                  </p>
                </div>
              )}
              <div style={{ marginTop: 10 }}>
                <Button
                  onClick={this.onAllowActionRulePrintTemplate}
                  variant="contained"
                  style={{ margin: 10 }}
                >
                  Allow
                </Button>
                <Button
                  onClick={this.closeAllowActionRulePrintTemplateDialog}
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
        <Dialog open={this.state.allowFulfilmentPrintTemplateDialogDisplayed}>
          <DialogContent style={{ padding: 30 }}>
            <div>
              <div>
                <FormControl required fullWidth={true}>
                  <InputLabel>Print Template</InputLabel>
                  <Select
                    onChange={this.onPrintTemplateChange}
                    value={this.state.printTemplateToAllow}
                    error={this.state.printTemplateValidationError}
                  >
                    {fulfilmentPrintTemplateMenuItems}
                  </Select>
                </FormControl>
              </div>
              {this.state.allowPrintTemplateError && (
                <div>
                  <p style={{ color: "red" }}>
                    {this.state.allowPrintTemplateError}
                  </p>
                </div>
              )}
              <div style={{ marginTop: 10 }}>
                <Button
                  onClick={this.onAllowFulfilmentPrintTemplate}
                  variant="contained"
                  style={{ margin: 10 }}
                >
                  Allow
                </Button>
                <Button
                  onClick={this.closeAllowFulfilmentPrintTemplateDialog}
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
                    error={this.state.printTemplateValidationError}
                  >
                    {smsFulfilmentTemplateMenuItems}
                  </Select>
                </FormControl>
              </div>
              {this.state.allowPrintTemplateError && (
                <p style={{ color: "red" }}>
                  {this.state.allowPrintTemplateError}
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
      </div>
    );
  }
}

export default SurveyDetails;
