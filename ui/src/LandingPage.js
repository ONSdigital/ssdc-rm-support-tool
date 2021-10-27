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
  Typography,
} from "@material-ui/core";
import Table from "@material-ui/core/Table";
import TableBody from "@material-ui/core/TableBody";
import TableCell from "@material-ui/core/TableCell";
import TableContainer from "@material-ui/core/TableContainer";
import TableHead from "@material-ui/core/TableHead";
import TableRow from "@material-ui/core/TableRow";
import { Link } from "react-router-dom";

class LandingPage extends Component {
  state = {
    authorisedActivities: [],
    thisUserAdminGroups: [],
    surveys: [],
    createSurveyDialogDisplayed: false,
    validationError: false,
    newSurveyName: "",
    validationRulesValidationError: false,
    sampleDefinitionUrlError: false,
    surveyMetadataError: false,
    newSurveyValidationRules: "",
    newSurveySampleDefintionUrl: "",
    newSurveyMetadata: "",
    printTemplates: [],
    smsTemplates: [],
    createPrintTemplateDialogDisplayed: false,
    createPrintTemplatePackCodeError: "",
    createSmsTemplateDialogDisplayed: false,
    createSmsTemplateError: "",
    createSMSTemplatePackCodeError: "",
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
    nextFulfilmentTriggerDateTime: "1970-01-01T00:00:00.000Z",
    configureNextTriggerDisplayed: false,
  };

  componentDidMount() {
    this.getAuthorisedBackendData();
  }

  componentWillUnmount() {
    clearInterval(this.interval);
  }

  getAuthorisedBackendData = async () => {
    this.getThisUserAdminGroups(); // Only need to do this once; don't refresh it repeatedly as it changes infrequently
    const authorisedActivities = await this.getAuthorisedActivities(); // Only need to do this once; don't refresh it repeatedly as it changes infrequently
    this.getPrintSuppliers(authorisedActivities); // Only need to do this once; don't refresh it repeatedly as it changes infrequently

    this.refreshDataFromBackend(authorisedActivities);

    this.interval = setInterval(
      () => this.refreshDataFromBackend(authorisedActivities),
      1000
    );
  };

  getAuthorisedActivities = async () => {
    const authResponse = await fetch("/api/auth");

    // TODO: We need more elegant error handling throughout the whole application, but this will at least protect temporarily
    if (!authResponse.ok) {
      return;
    }

    const authorisedActivities = await authResponse.json();

    this.setState({ authorisedActivities: authorisedActivities });

    return authorisedActivities;
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

  refreshDataFromBackend = (authorisedActivities) => {
    this.getSurveys(authorisedActivities);
    this.getPrintTemplates(authorisedActivities);
    this.getSmsTemplates(authorisedActivities);
  };

  getPrintSuppliers = async (authorisedActivities) => {
    if (!authorisedActivities.includes("LIST_PRINT_SUPPLIERS")) return;

    const supplierResponse = await fetch("/api/printsuppliers");
    const supplierJson = await supplierResponse.json();

    this.setState({
      printSuppliers: supplierJson,
    });
  };

  getSurveys = async (authorisedActivities) => {
    if (!authorisedActivities.includes("LIST_SURVEYS")) return;

    const response = await fetch("/api/surveys");
    const surveyJson = await response.json();

    this.setState({ surveys: surveyJson });
  };

  getDateTimeForDateTimePicker = (date) => {
    date.setMinutes(date.getMinutes() - date.getTimezoneOffset());
    return date.toJSON().slice(0, 16);
  };

  getFulfilmentTrigger = async () => {
    if (
      !this.state.authorisedActivities.includes("CONFIGURE_FULFILMENT_TRIGGER")
    )
      return;

    const response = await fetch(`/api/fulfilmentNextTriggers`);

    if (!response.ok) {
      this.setState({
        nextFulfilmentTriggerDateTime: this.getDateTimeForDateTimePicker(
          new Date()
        ),
      });
    } else {
      const fulfilmentNextTriggerJson = await response.json();
      var dateOfTrigger = new Date(fulfilmentNextTriggerJson);
      this.setState({
        nextFulfilmentTriggerDateTime:
          this.getDateTimeForDateTimePicker(dateOfTrigger),
      });
    }
  };

  getPrintTemplates = async (authorisedActivities) => {
    if (!authorisedActivities.includes("LIST_PRINT_TEMPLATES")) return;

    const response = await fetch("/api/printTemplates");
    const templateJson = await response.json();

    this.setState({ printTemplates: templateJson });
  };

  getSmsTemplates = async (authorisedActivities) => {
    if (!authorisedActivities.includes("LIST_SMS_TEMPLATES")) return;

    const response = await fetch("/api/smsTemplates");
    const templateJson = await response.json();

    this.setState({ smsTemplates: templateJson });
  };

  openDialog = () => {
    this.createSurveyInProgress = false;

    this.setState({
      newSurveyName: "",
      validationError: false,
      validationRulesValidationError: false,
      newSurveyValidationRules: "",
      createSurveyDialogDisplayed: true,
      newSurveyHeaderRow: true,
      newSurveySampleSeparator: ",",
      sampleDefinitionUrlError: false,
      surveyMetadataError: false,
      newSurveySampleDefintionUrl: "",
      newSurveyMetadata: "",
    });
  };

  openFulfilmentTriggerDialog = () => {
    this.updateFulfilmentTriggerDateTimeInProgress = false;

    this.getFulfilmentTrigger();
    this.setState({
      configureNextTriggerDisplayed: true,
    });
  };

  openPrintTemplateDialog = () => {
    this.createPrintTemplateInProgress = false;

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
    this.createSmsTemplateInProgress = false;

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
    this.setState({
      createPrintTemplateDialogDisplayed: false,
      createPrintTemplatePackCodeError: "",
    });
  };

  closeSmsTemplateDialog = () => {
    this.setState({
      createSmsTemplateDialogDisplayed: false,
      createSMSTemplatePackCodeError: "",
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

  onNewSurveySampleDefinitionUrlChange = (event) => {
    const resetValidation = !event.target.value.trim();
    this.setState({
      sampleDefinitionUrlError: resetValidation,
      newSurveySampleDefintionUrl: event.target.value,
    });
  };

  onNewSurveyMetadataChange = (event) => {
    const resetValidation = !event.target.value.trim();
    this.setState({
      surveyMetadataError: resetValidation,
      newSurveyMetadata: event.target.value,
    });
  };

  onNewSurveyHeaderRowChange = (event) => {
    this.setState({ newSurveyHeaderRow: event.target.value });
  };

  onNewSurveySampleSeparatorChange = (event) => {
    this.setState({ newSurveySampleSeparator: event.target.value });
  };

  onCreateSurvey = async () => {
    if (this.createSurveyInProgress) {
      return;
    }

    this.createSurveyInProgress = true;

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

    if (!this.state.newSurveySampleDefintionUrl.trim()) {
      this.setState({ sampleDefinitionUrlError: true });
      validationFailed = true;
    }

    let metadataJson = null;
    if (this.state.newSurveyMetadata.length > 0) {
      try {
        const parsedJson = JSON.parse(this.state.newSurveyMetadata);
        if (Object.keys(parsedJson).length === 0) {
          this.setState({ surveyMetadataError: true });
          validationFailed = true;
        } else {
          metadataJson = JSON.parse(this.state.newSurveyMetadata);
        }
      } catch (err) {
        this.setState({ surveyMetadataError: true });
        validationFailed = true;
      }
    }

    if (validationFailed) {
      this.createSurveyInProgress = false;
      return;
    }

    const newSurvey = {
      name: this.state.newSurveyName,
      sampleValidationRules: JSON.parse(this.state.newSurveyValidationRules),
      sampleWithHeaderRow: this.state.newSurveyHeaderRow,
      sampleSeparator: this.state.newSurveySampleSeparator,
      sampleDefinitionUrl: this.state.newSurveySampleDefintionUrl,
      metadata: metadataJson,
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
    if (this.updateFulfilmentTriggerDateTimeInProgress) {
      return;
    }

    this.updateFulfilmentTriggerDateTimeInProgress = true;

    const triggerDateTime = new Date(
      this.state.nextFulfilmentTriggerDateTime
    ).toISOString();

    const response = await fetch(
      `/api/fulfilmentNextTriggers/?triggerDateTime=${triggerDateTime}`,
      {
        method: "POST",
      }
    );

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
    if (this.createPrintTemplateInProgress) {
      return;
    }

    this.createPrintTemplateInProgress = true;

    this.setState({
      createPrintTemplatePackCodeError: "",
      packCodeValidationError: false,
    });

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

    if (
      this.state.printTemplates.some(
        (printTemplate) =>
          printTemplate.packCode.toUpperCase() ===
          this.state.packCode.toUpperCase()
      )
    ) {
      this.setState({
        packCodeValidationError: true,
        createPrintTemplatePackCodeError: "Pack code already in use",
      });

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
      this.createPrintTemplateInProgress = false;
      return;
    }

    const newPrintTemplate = {
      packCode: this.state.packCode,
      printSupplier: this.state.printSupplier,
      template: JSON.parse(this.state.template),
    };

    const response = await fetch("/api/printTemplates", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(newPrintTemplate),
    });

    if (!response.ok) {
      this.createPrintTemplateInProgress = false;
    } else {
      this.setState({ createPrintTemplateDialogDisplayed: false });
    }
  };

  onCreateSmsTemplate = async () => {
    if (this.createSmsTemplateInProgress) {
      return;
    }

    this.createSmsTemplateInProgress = true;

    this.setState({
      createSMSTemplatePackCodeError: "",
      packCodeValidationError: false,
    });

    var failedValidation = false;

    if (!this.state.packCode.trim()) {
      this.setState({ packCodeValidationError: true });
      failedValidation = true;
    }

    if (
      this.state.smsTemplates.some(
        (smsTemplate) =>
          smsTemplate.packCode.toUpperCase() ===
          this.state.packCode.toUpperCase()
      )
    ) {
      this.setState({
        createSMSTemplatePackCodeError: "PackCode already in use",
        packCodeValidationError: true,
      });
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
      this.createSmsTemplateInProgress = false;
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
      this.setState({
        createSmsTemplateError: "Error Creating SMSTemplate",
      });
      this.createSmsTemplateInProgress = false;
    } else {
      this.setState({ createSmsTemplateDialogDisplayed: false });
    }
  };

  render() {
    const surveyTableRows = this.state.surveys.map((survey) => (
      <TableRow key={survey.name}>
        <TableCell component="th" scope="row">
          <Link to={`/survey?surveyId=${survey.id}`}>{survey.name}</Link>
        </TableCell>
        <TableCell component="th" scope="row">
          <a href={survey.sampleDefinitionUrl} target="_blank" rel="noreferrer">
            {survey.sampleDefinitionUrl}
          </a>
        </TableCell>
        <TableCell component="th" scope="row">
          {JSON.stringify(survey.metadata)}
        </TableCell>
      </TableRow>
    ));

    const printTemplateRows = this.state.printTemplates.map((printTemplate) => (
      <TableRow key={printTemplate.packCode}>
        <TableCell component="th" scope="row">
          {printTemplate.packCode}
        </TableCell>
        <TableCell component="th" scope="row">
          {printTemplate.printSupplier}
        </TableCell>
        <TableCell component="th" scope="row">
          {JSON.stringify(printTemplate.template)}
        </TableCell>
      </TableRow>
    ));
    const smsTemplateRows = this.state.smsTemplates.map((smsTemplate) => (
      <TableRow key={smsTemplate.packCode}>
        <TableCell component="th" scope="row">
          {smsTemplate.packCode}
        </TableCell>
        <TableCell component="th" scope="row">
          {JSON.stringify(smsTemplate.template)}
        </TableCell>
        <TableCell component="th" scope="row">
          {smsTemplate.notifyTemplateId}
        </TableCell>
      </TableRow>
    ));

    const printSupplierMenuItems = this.state.printSuppliers.map((supplier) => (
      <MenuItem key={supplier} value={supplier}>
        {supplier}
      </MenuItem>
    ));

    return (
      <div style={{ padding: 20 }}>
        {this.state.authorisedActivities.includes("LIST_SURVEYS") && (
          <>
            <Typography variant="h6" color="inherit">
              Surveys
            </Typography>
            <TableContainer component={Paper}>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>Name</TableCell>
                    <TableCell>Sample Definition URL</TableCell>
                    <TableCell>Metadata</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>{surveyTableRows}</TableBody>
              </Table>
            </TableContainer>
          </>
        )}
        {this.state.authorisedActivities.includes("CREATE_SURVEY") && (
          <Button
            variant="contained"
            onClick={this.openDialog}
            style={{ marginTop: 10 }}
          >
            Create Survey
          </Button>
        )}

        {this.state.authorisedActivities.includes("LIST_PRINT_TEMPLATES") && (
          <>
            <Typography variant="h6" color="inherit" style={{ marginTop: 10 }}>
              Print Templates
            </Typography>
            <TableContainer component={Paper}>
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
          </>
        )}
        {this.state.authorisedActivities.includes("CREATE_PRINT_TEMPLATE") && (
          <Button
            variant="contained"
            onClick={this.openPrintTemplateDialog}
            style={{ marginTop: 10 }}
          >
            Create Print Template
          </Button>
        )}

        {this.state.authorisedActivities.includes("LIST_SMS_TEMPLATES") && (
          <>
            <Typography variant="h6" color="inherit" style={{ marginTop: 10 }}>
              SMS Templates
            </Typography>
            <TableContainer component={Paper}>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>Pack Code</TableCell>
                    <TableCell>Template</TableCell>
                    <TableCell>Gov Notify Template ID</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>{smsTemplateRows}</TableBody>
              </Table>
            </TableContainer>
          </>
        )}
        {this.state.authorisedActivities.includes("CREATE_SMS_TEMPLATE") && (
          <Button
            variant="contained"
            onClick={this.openSmsTemplateDialog}
            style={{ marginTop: 10 }}
          >
            Create sms Template
          </Button>
        )}

        {this.state.authorisedActivities.includes(
          "CONFIGURE_FULFILMENT_TRIGGER"
        ) && (
          <div>
            <Button
              variant="contained"
              onClick={this.openFulfilmentTriggerDialog}
              style={{ marginTop: 20 }}
            >
              Configure fulfilment trigger
            </Button>
          </div>
        )}
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
                    <MenuItem value={"|"}>Pipe</MenuItem>
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
                <TextField
                  style={{ marginTop: 10 }}
                  required
                  multiline
                  fullWidth={true}
                  error={this.state.sampleDefinitionUrlError}
                  id="standard-required"
                  label="Survey Definition URL"
                  onChange={this.onNewSurveySampleDefinitionUrlChange}
                  value={this.state.newSurveySampleDefintionUrl}
                />
                <TextField
                  style={{ marginTop: 10 }}
                  multiline
                  fullWidth={true}
                  error={this.state.surveyMetadataError}
                  id="standard-required"
                  label="Metadata"
                  onChange={this.onNewSurveyMetadataChange}
                  value={this.state.newSurveyMetadata}
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
                  helperText={this.state.createPrintTemplatePackCodeError}
                />
                <TextField
                  required
                  fullWidth={true}
                  style={{ marginTop: 20 }}
                  error={this.state.templateValidationError}
                  label="Template"
                  onChange={this.onTemplateChange}
                  value={this.state.template}
                  helperText={this.state.notifyTemplateIdErrorMessage}
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
                  helperText={this.state.createSMSTemplatePackCodeError}
                />
                <TextField
                  required
                  fullWidth={true}
                  style={{ marginTop: 20 }}
                  error={this.state.notifyTemplateIdValidationError}
                  label="Notify Template ID (UUID)"
                  onChange={this.onNotifyTemplateIdChange}
                  value={this.state.notifyTemplateId}
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
