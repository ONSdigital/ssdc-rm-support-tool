import React, { Component } from "react";
import "@fontsource/roboto";
import {
  Button,
  Dialog,
  DialogContent,
  TextField,
  Paper,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
} from "@material-ui/core";
import Table from "@material-ui/core/Table";
import TableBody from "@material-ui/core/TableBody";
import TableCell from "@material-ui/core/TableCell";
import TableContainer from "@material-ui/core/TableContainer";
import TableHead from "@material-ui/core/TableHead";
import TableRow from "@material-ui/core/TableRow";
import { uuidv4 } from "./common";
import { Link } from "react-router-dom";

class LandingPage extends Component {
  state = {
    authorisedActivities: [],
    surveys: [],
    createSurveyDialogDisplayed: false,
    validationError: false,
    newSurveyName: "",
    validationRulesValidationError: false,
    newSurveyValidationRules: "",
    printTemplates: [],
    smsTemplates: [],
    createPrintTemplateDialogDisplayed: false,
    createSmsTemplateDialogDisplayed: false,
    createSmsTemplateError: "",
    printSuppliers: [],
    printSupplier: "",
    packCode: "",
    template: "",
    notifyTemplateId: "",
    printSupplierValidationError: false,
    packCodeValidationError: false,
    templateValidationError: false,
    notifyTemplateIdValidationError: false,
    notifyTemplateIdErrorMessage: "",
    newSurveyHeaderRow: true,
    newSurveySampleSeparator: ",",
    triggerID: "366ce7da-f885-493d-b933-a3b74d583a06",
    nextFulfilmentTriggerDateTime: "1970-01-01T00:00:00.000Z",
    configureNextTriggerDisplayed: false,
  };

  componentDidMount() {
    this.getAuthorisedActivities(); // Only need to do this once; don't refresh it repeatedly as it changes infrequently
    this.refreshDataFromBackend();

    this.interval = setInterval(() => this.refreshDataFromBackend(), 1000);
  }

  componentWillUnmount() {
    clearInterval(this.interval);
  }

  getAuthorisedActivities = async () => {
    const authResponse = await fetch("/api/auth");

    // TODO: We need more elegant error handling throughout the whole application, but this will at least protect temporarily
    if (!authResponse.ok) {
      return;
    }

    const authorisedActivities = await authResponse.json();

    if (authorisedActivities.includes("CREATE_PRINT_TEMPLATE")) {
      const supplierResponse = await fetch("/api/printsuppliers");

      // TODO: We need more elegant error handling throughout the whole application, but this will at least protect temporarily
      if (!supplierResponse.ok) {
        return;
      }

      const supplierJson = await supplierResponse.json();

      this.setState({
        authorisedActivities: authorisedActivities,
        printSuppliers: supplierJson,
      });
    } else {
      this.setState({ authorisedActivities: authorisedActivities });
    }
  };

  refreshDataFromBackend = () => {
    this.getSurveys();
    this.getPrintTemplates();
    this.getSmsTemplates();
  };

  getSurveys = async () => {
    const response = await fetch("/api/surveys");
    const surveyJson = await response.json();

    this.setState({ surveys: surveyJson._embedded.surveys });
  };

  getDateTimeForDateTimePicker = (date) => {
    date.setMinutes(date.getMinutes() - date.getTimezoneOffset());
    return date.toJSON().slice(0, 16);
  };

  getFulfilmentTrigger = async () => {
    const response = await fetch(
      `/api/fulfilmentNextTriggers/${this.state.triggerID}`
    );

    if (!response.ok) {
      this.setState({
        nextFulfilmentTriggerDateTime: this.getDateTimeForDateTimePicker(
          new Date()
        ),
      });
    } else {
      const fulfilmentNextTriggerJson = await response.json();
      var dateOfTrigger = new Date(fulfilmentNextTriggerJson.triggerDateTime);
      this.setState({
        nextFulfilmentTriggerDateTime:
          this.getDateTimeForDateTimePicker(dateOfTrigger),
      });
    }
  };

  getPrintTemplates = async () => {
    const response = await fetch("/api/printTemplates");
    const templateJson = await response.json();

    this.setState({ printTemplates: templateJson._embedded.printTemplates });
  };
  getSmsTemplates = async () => {
    const response = await fetch("/api/smsTemplates");
    const templateJson = await response.json();

    this.setState({ smsTemplates: templateJson._embedded.smsTemplates });
  };

  openDialog = () => {
    this.setState({
      newSurveyName: "",
      validationError: false,
      validationRulesValidationError: false,
      newSurveyValidationRules: "",
      createSurveyDialogDisplayed: true,
      newSurveyHeaderRow: true,
      newSurveySampleSeparator: ",",
    });
  };

  openFulfilmentTriggerDialog = () => {
    this.getFulfilmentTrigger();
    this.setState({
      configureNextTriggerDisplayed: true,
    });
  };

  openPrintTemplateDialog = () => {
    this.setState({
      printSupplier: "",
      packCode: "",
      template: "",
      printSupplierValidationError: false,
      packCodeValidationError: false,
      templateValidationError: false,
      createPrintTemplateDialogDisplayed: true,
    });
  };

  openSmsTemplateDialog = () => {
    this.setState({
      packCode: "",
      template: "",
      notifyTemplateId: "",
      packCodeValidationError: false,
      templateValidationError: false,
      createSmsTemplateDialogDisplayed: true,
      createSmsTemplateError: "",
      notifyTemplateIdErrorMessage: "",
    });
  };

  closeDialog = () => {
    this.setState({ createSurveyDialogDisplayed: false });
  };

  closeNextTriggerDialog = () => {
    this.setState({ configureNextTriggerDisplayed: false });
  };

  closePrintTemplateDialog = () => {
    this.setState({ createPrintTemplateDialogDisplayed: false });
  };
  closeSmsTemplateDialog = () => {
    this.setState({
      createSmsTemplateDialogDisplayed: false,
    });
  };

  onNewSurveyNameChange = (event) => {
    const resetValidation = !event.target.value.trim();
    this.setState({
      validationError: resetValidation,
      newSurveyName: event.target.value,
    });
  };

  onNewSurveyValidationRulesChange = (event) => {
    const resetValidation = !event.target.value.trim();
    this.setState({
      validationRulesValidationError: resetValidation,
      newSurveyValidationRules: event.target.value,
    });
  };

  onNewSurveyHeaderRowChange = (event) => {
    this.setState({ newSurveyHeaderRow: event.target.value });
  };

  onNewSurveySampleSeparatorChange = (event) => {
    this.setState({ newSurveySampleSeparator: event.target.value });
  };

  onCreateSurvey = async () => {
    let validationFailed = false;

    if (!this.state.newSurveyName.trim()) {
      this.setState({ validationError: true });
      validationFailed = true;
    }

    if (!this.state.newSurveyValidationRules.trim()) {
      this.setState({ validationRulesValidationError: true });
      validationFailed = true;
    } else {
      try {
        const parsedJson = JSON.parse(this.state.newSurveyValidationRules);
        if (!Array.isArray(parsedJson)) {
          this.setState({ validationRulesValidationError: true });
          validationFailed = true;
        }
      } catch (err) {
        this.setState({ validationRulesValidationError: true });
        validationFailed = true;
      }
    }

    if (validationFailed) {
      return;
    }

    const newSurvey = {
      id: uuidv4(),
      name: this.state.newSurveyName,
      sampleValidationRules: JSON.parse(this.state.newSurveyValidationRules),
      sampleWithHeaderRow: this.state.newSurveyHeaderRow,
      sampleSeparator: this.state.newSurveySampleSeparator,
    };

    await fetch("/api/surveys", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(newSurvey),
    });

    this.setState({ createSurveyDialogDisplayed: false });
  };

  onFulfilmentTriggerDateChange = (event) => {
    this.setState({ nextFulfilmentTriggerDateTime: event.target.value });
  };

  onUpdateFulfilmentTriggerDateTime = async () => {
    const updatedTriggerDateTime = {
      id: this.state.triggerID,
      triggerDateTime: new Date(
        this.state.nextFulfilmentTriggerDateTime
      ).toISOString(),
    };

    const response = await fetch("/api/fulfilmentNextTriggers", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(updatedTriggerDateTime),
    });

    if (response.ok) {
      this.setState({ configureNextTriggerDisplayed: false });
    }
  };

  onPrintSupplierChange = (event) => {
    this.setState({
      printSupplier: event.target.value,
      printSupplierValidationError: false,
    });
  };

  onPackCodeChange = (event) => {
    const resetValidation = !event.target.value.trim();

    this.setState({
      packCode: event.target.value,
      packCodeValidationError: resetValidation,
    });
  };

  onTemplateChange = (event) => {
    const resetValidation = !event.target.value.trim();

    this.setState({
      template: event.target.value,
      templateValidationError: resetValidation,
    });
  };

  onNotifyTemplateIdChange = (event) => {
    const resetValidation = !event.target.value.trim();

    this.setState({
      notifyTemplateId: event.target.value,
      notifyTemplateIdValidationError: resetValidation,
    });
  };

  onCreatePrintTemplate = async () => {
    var failedValidation = false;

    if (!this.state.printSupplier.trim()) {
      this.setState({ printSupplierValidationError: true });
      failedValidation = true;
    }

    if (!this.state.printSupplier.trim()) {
      this.setState({ printSupplierValidationError: true });
      failedValidation = true;
    }

    if (!this.state.packCode.trim()) {
      this.setState({ packCodeValidationError: true });
      failedValidation = true;
    }

    if (!this.state.template.trim()) {
      this.setState({ templateValidationError: true });
      failedValidation = true;
    } else {
      try {
        const parsedJson = JSON.parse(this.state.template);
        if (!Array.isArray(parsedJson) || parsedJson.length === 0) {
          this.setState({ templateValidationError: true });
          failedValidation = true;
        }
      } catch (err) {
        this.setState({ templateValidationError: true });
        failedValidation = true;
      }
    }

    if (failedValidation) {
      return;
    }

    const newPrintTemplate = {
      packCode: this.state.packCode,
      printSupplier: this.state.printSupplier,
      template: JSON.parse(this.state.template),
    };

    await fetch("/api/printTemplates", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(newPrintTemplate),
    });

    this.setState({ createPrintTemplateDialogDisplayed: false });
  };

  onCreateSmsTemplate = async () => {
    var failedValidation = false;

    if (!this.state.packCode.trim()) {
      this.setState({ packCodeValidationError: true });
      failedValidation = true;
    }
    if (!this.state.notifyTemplateId) {
      this.setState({
        notifyTemplateIdValidationError: true,
      });
      failedValidation = true;
    } else {
      const regexExp =
        /^[0-9a-fA-F]{8}\b-[0-9a-fA-F]{4}\b-[0-9a-fA-F]{4}\b-[0-9a-fA-F]{4}\b-[0-9a-fA-F]{12}$/gi;
      if (!regexExp.test(this.state.notifyTemplateId)) {
        this.setState({
          notifyTemplateIdValidationError: true,
          notifyTemplateIdErrorMessage: "Not a valid UUID",
        });
        failedValidation = true;
      }
    }
    if (!this.state.template.trim()) {
      this.setState({ templateValidationError: true });
      failedValidation = true;
    } else {
      try {
        const parsedJson = JSON.parse(this.state.template);
        if (!Array.isArray(parsedJson)) {
          this.setState({ templateValidationError: true });
          failedValidation = true;
        }
      } catch (err) {
        this.setState({ templateValidationError: true });
        failedValidation = true;
      }
    }

    if (failedValidation) {
      return;
    }

    const newSmsTemplate = {
      notifyTemplateId: this.state.notifyTemplateId,
      packCode: this.state.packCode,
      template: JSON.parse(this.state.template),
    };

    const response = await fetch("/api/smsTemplates", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(newSmsTemplate),
    });
    if (!response.ok) {
      const errorMessage = await response.text();
      this.setState({
        createSmsTemplateError: errorMessage,
        notifyTemplateIdValidationError: true,
        templateValidationError: true,
        packCodeValidationError: true,
      });
      return;
    }

    this.setState({ createSmsTemplateDialogDisplayed: false });
  };

  render() {
    const surveyTableRows = this.state.surveys.map((survey) => {
      const surveyId = survey._links.self.href.split("/").pop();

      return (
        <TableRow key={survey.name}>
          <TableCell component="th" scope="row">
            <Link to={`/survey?surveyId=${surveyId}`}>{survey.name}</Link>
          </TableCell>
        </TableRow>
      );
    });

    const printTemplateRows = this.state.printTemplates.map((printTemplate) => {
      const packCode = printTemplate._links.self.href.split("/").pop();

      return (
        <TableRow key={packCode}>
          <TableCell component="th" scope="row">
            {packCode}
          </TableCell>
          <TableCell component="th" scope="row">
            {printTemplate.printSupplier}
          </TableCell>
          <TableCell component="th" scope="row">
            {JSON.stringify(printTemplate.template)}
          </TableCell>
        </TableRow>
      );
    });
    const smsTemplateRows = this.state.smsTemplates.map((smsTemplate) => {
      const packCode = smsTemplate._links.self.href.split("/").pop();

      return (
        <TableRow key={packCode}>
          <TableCell component="th" scope="row">
            {packCode}
          </TableCell>
          <TableCell component="th" scope="row">
            {JSON.stringify(smsTemplate.template)}
          </TableCell>
        </TableRow>
      );
    });

    const printSupplierMenuItems = this.state.printSuppliers.map((supplier) => (
      <MenuItem key={supplier} value={supplier}>
        {supplier}
      </MenuItem>
    ));

    return (
      <div style={{ padding: 20 }}>
        {this.state.authorisedActivities.includes("CREATE_SURVEY") && (
          <Button variant="contained" onClick={this.openDialog}>
            Create Survey
          </Button>
        )}
        {this.state.authorisedActivities.includes("LIST_SURVEYS") && (
          <TableContainer component={Paper} style={{ marginTop: 20 }}>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Survey Name</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>{surveyTableRows}</TableBody>
            </Table>
          </TableContainer>
        )}
        {this.state.authorisedActivities.includes("CREATE_PRINT_TEMPLATE") && (
          <div>
            <Button
              variant="contained"
              onClick={this.openPrintTemplateDialog}
              style={{ marginTop: 20 }}
            >
              Create Print Template
            </Button>
            <TableContainer component={Paper} style={{ marginTop: 20 }}>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>Pack Code</TableCell>
                    <TableCell>Print Supplier</TableCell>
                    <TableCell>Template</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>{printTemplateRows}</TableBody>
              </Table>
            </TableContainer>
          </div>
        )}

        {this.state.authorisedActivities.includes("CREATE_SMS_TEMPLATE") && (
          <div>
            <Button
              variant="contained"
              onClick={this.openSmsTemplateDialog}
              style={{ marginTop: 20 }}
            >
              Create sms Template
            </Button>
            <TableContainer component={Paper} style={{ marginTop: 20 }}>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>Pack Code</TableCell>
                    <TableCell>Template</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>{smsTemplateRows}</TableBody>
              </Table>
            </TableContainer>
          </div>
        )}
        <div>
          <Button
            variant="contained"
            onClick={this.openFulfilmentTriggerDialog}
            style={{ marginTop: 20 }}
          >
            Configure fulfilment trigger
          </Button>
        </div>
        {this.state.authorisedActivities.includes("SUPER_USER") && (
          <>
            <div style={{ marginTop: 20 }}>
              <Link to="/userAdmin">User and Groups Admin</Link>
            </div>
            <div style={{ marginTop: 20 }}>
              <Link to="/exceptionManager">Exception Manager</Link>
            </div>
          </>
        )}
        <Dialog open={this.state.createSurveyDialogDisplayed} fullWidth={true}>
          <DialogContent style={{ padding: 30 }}>
            <div>
              <div>
                <TextField
                  required
                  error={this.state.validationError}
                  id="standard-required"
                  label="Survey name"
                  onChange={this.onNewSurveyNameChange}
                  value={this.state.newSurveyName}
                />
                <FormControl
                  style={{ marginTop: 10 }}
                  required
                  fullWidth={true}
                >
                  <InputLabel>Sample Has Header Row</InputLabel>
                  <Select
                    onChange={this.onNewSurveyHeaderRowChange}
                    value={this.state.newSurveyHeaderRow}
                  >
                    <MenuItem value={true}>True</MenuItem>
                    <MenuItem value={false}>False</MenuItem>
                  </Select>
                </FormControl>
                <FormControl
                  style={{ marginTop: 10 }}
                  required
                  fullWidth={true}
                >
                  <InputLabel>Sample File Separator</InputLabel>
                  <Select
                    onChange={this.onNewSurveySampleSeparatorChange}
                    value={this.state.newSurveySampleSeparator}
                  >
                    <MenuItem value={","}>Comma</MenuItem>
                    <MenuItem value={":"}>Colon</MenuItem>
                  </Select>
                </FormControl>
                <TextField
                  style={{ marginTop: 10 }}
                  required
                  multiline
                  fullWidth={true}
                  error={this.state.validationRulesValidationError}
                  id="standard-required"
                  label="Validation rules"
                  onChange={this.onNewSurveyValidationRulesChange}
                  value={this.state.newSurveyValidationRules}
                />
              </div>
              <div style={{ marginTop: 10 }}>
                <Button
                  onClick={this.onCreateSurvey}
                  variant="contained"
                  style={{ margin: 10 }}
                >
                  Create survey
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
        <Dialog
          open={this.state.configureNextTriggerDisplayed}
          fullWidth={true}
        >
          <DialogContent style={{ padding: 30 }}>
            <div>
              <div>
                <TextField
                  label="Trigger Date"
                  type="datetime-local"
                  value={this.state.nextFulfilmentTriggerDateTime.slice(0, 16)}
                  onChange={this.onFulfilmentTriggerDateChange}
                  style={{ marginTop: 20 }}
                  InputLabelProps={{
                    shrink: true,
                  }}
                />
              </div>
              <div style={{ marginTop: 10 }}>
                <Button
                  onClick={this.onUpdateFulfilmentTriggerDateTime}
                  variant="contained"
                  style={{ margin: 10 }}
                >
                  Update fulfilment trigger
                </Button>
                <Button
                  onClick={this.closeNextTriggerDialog}
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
          open={this.state.createPrintTemplateDialogDisplayed}
          fullWidth={true}
        >
          <DialogContent style={{ padding: 30 }}>
            <div>
              <div>
                <FormControl required fullWidth={true}>
                  <InputLabel>Print Supplier</InputLabel>
                  <Select
                    onChange={this.onPrintSupplierChange}
                    value={this.state.printSupplier}
                    error={this.state.printSupplierValidationError}
                  >
                    {printSupplierMenuItems}
                  </Select>
                </FormControl>
                <TextField
                  required
                  fullWidth={true}
                  style={{ marginTop: 20 }}
                  error={this.state.packCodeValidationError}
                  label="Pack Code"
                  onChange={this.onPackCodeChange}
                  value={this.state.packCode}
                />
                <TextField
                  required
                  fullWidth={true}
                  style={{ marginTop: 20 }}
                  error={this.state.templateValidationError}
                  label="Template"
                  onChange={this.onTemplateChange}
                  value={this.state.template}
                />
              </div>
              <div style={{ marginTop: 10 }}>
                <Button
                  onClick={this.onCreatePrintTemplate}
                  variant="contained"
                  style={{ margin: 10 }}
                >
                  Create print template
                </Button>
                <Button
                  onClick={this.closePrintTemplateDialog}
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
          open={this.state.createSmsTemplateDialogDisplayed}
          fullWidth={true}
        >
          <DialogContent style={{ padding: 30 }}>
            {this.state.createSmsTemplateError && (
              <div style={{ color: "red" }}>
                {this.state.createSmsTemplateError}
              </div>
            )}
            <div>
              <div>
                <TextField
                  required
                  fullWidth={true}
                  style={{ marginTop: 20 }}
                  error={this.state.packCodeValidationError}
                  label="Pack Code"
                  onChange={this.onPackCodeChange}
                  value={this.state.packCode}
                />
                <TextField
                  required
                  fullWidth={true}
                  style={{ marginTop: 20 }}
                  error={this.state.notifyTemplateIdValidationError}
                  label="Notify Template ID (UUID)"
                  onChange={this.onNotifyTemplateIdChange}
                  value={this.state.notifyTemplateId}
                  helperText={this.state.notifyTemplateIdErrorMessage}
                />
                <TextField
                  fullWidth={true}
                  style={{ marginTop: 20 }}
                  error={this.state.templateValidationError}
                  label="Template"
                  onChange={this.onTemplateChange}
                  value={this.state.template}
                />
              </div>
              <div style={{ marginTop: 10 }}>
                <Button
                  onClick={this.onCreateSmsTemplate}
                  variant="contained"
                  style={{ margin: 10 }}
                >
                  Create SMS template
                </Button>
                <Button
                  onClick={this.closeSmsTemplateDialog}
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

export default LandingPage;
