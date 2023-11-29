import React, { Component } from "react";
import "@fontsource/roboto";
import {
  Button,
  Dialog,
  DialogContent,
  FormControl,
  InputLabel,
  MenuItem,
  Paper,
  Select,
  TextField,
  Typography,
} from "@material-ui/core";
import Table from "@material-ui/core/Table";
import TableBody from "@material-ui/core/TableBody";
import TableCell from "@material-ui/core/TableCell";
import TableContainer from "@material-ui/core/TableContainer";
import TableHead from "@material-ui/core/TableHead";
import TableRow from "@material-ui/core/TableRow";
import SampleUpload from "./SampleUpload";
import {
  getActionRuleExportFilePackCodesForSurvey,
  getActionRuleSmsPackCodesForSurvey,
  getActionRuleEmailPackCodesForSurvey,
  getSensitiveSampleColumns,
  errorAlert,
} from "./Utils";
import { Link } from "react-router-dom";
import JSONPretty from "react-json-pretty";

class CollectionExerciseDetails extends Component {
  state = {
    authorisedActivities: [],
    actionRules: [],
    exportFilePackCodes: [],
    sensitiveSampleColumns: [],
    smsPackCodes: [],
    emailPackCodes: [],
    collectionInstrumentRulesDisplayed: false,
    createActionRulesDialogDisplayed: false,
    rescheduleActionRulesDialogDisplayed: false,
    exportFilePackCodeValidationError: false,
    smsPackCodeValidationError: false,
    smsPhoneNumberColumnValidationError: false,
    emailPackCodeValidationError: false,
    emailColumnValidationError: false,
    actionRuleTypeValidationError: false,
    uacQidMetadataValidationError: false,
    collectionExerciseDetails: {},
    newActionRuleExportFilePackCode: "",
    newActionRuleSmsPackCode: "",
    newActionRuleSmsPhoneNumberColumn: "",
    newActionRuleEmailPackCode: "",
    newActionRuleEmailColumn: "",
    newActionRuleClassifiers: "",
    newActionRuleType: "",
    newUacQidMetadata: "",
    rescheduleActionRuleDisabled: false,
    actionRuleToBeUpdated: {},
    updatedTriggerDateTime: "",
  };

  componentDidMount() {
    this.getAuthorisedBackendData();
  }

  getAuthorisedBackendData = async () => {
    const authorisedActivities = await this.getAuthorisedActivities(); // Only need to do this once; don't refresh it repeatedly as it changes infrequently
    this.getCollectionExerciseDetails(authorisedActivities);
    this.getActionRules(authorisedActivities);
    this.getExportFileTemplates(authorisedActivities);
    this.getSmsTemplates(authorisedActivities);
    this.getEmailTemplates(authorisedActivities);
    this.getSensitiveSampleColumns(authorisedActivities);
  };

  getAuthorisedActivities = async () => {
    const response = await fetch(`/api/auth?surveyId=${this.props.surveyId}`);

    // TODO: We need more elegant error handling throughout the whole application, but this will at least protect temporarily
    const responseJson = await response.json();
    if (!response.ok) {
      errorAlert(responseJson);
      return;
    }

    this.setState({ authorisedActivities: responseJson });

    return responseJson;
  };

  getSensitiveSampleColumns = async (authorisedActivities) => {
    const sensitiveSampleColumns = await getSensitiveSampleColumns(
      authorisedActivities,
      this.props.surveyId,
    );
    this.setState({ sensitiveSampleColumns: sensitiveSampleColumns });
  };

  getCollectionExerciseDetails = async (authorisedActivities) => {
    if (!authorisedActivities.includes("VIEW_COLLECTION_EXERCISE")) return;

    const response = await fetch(
      `api/collectionExercises/${this.props.collectionExerciseId}`,
    );

    // TODO: We need more elegant error handling throughout the whole application, but this will at least protect temporarily
    const responseJson = await response.json();
    if (!response.ok) {
      errorAlert(responseJson);
      return;
    }

    this.setState({ collectionExerciseDetails: responseJson });
  };

  getActionRules = async (authorisedActivities) => {
    if (!authorisedActivities.includes("LIST_ACTION_RULES")) return;

    const response = await fetch(
      `/api/actionRules/?collectionExercise=${this.props.collectionExerciseId}`,
    );
    const actionRuleJson = await response.json();

    this.setState({
      actionRules: actionRuleJson,
    });
  };

  getExportFileTemplates = async (authorisedActivities) => {
    if (
      !authorisedActivities.includes(
        "LIST_ALLOWED_EXPORT_FILE_TEMPLATES_ON_ACTION_RULES",
      )
    )
      return;

    const packCodes = await getActionRuleExportFilePackCodesForSurvey(
      authorisedActivities,
      this.props.surveyId,
    );
    this.setState({ exportFilePackCodes: packCodes });
  };

  getSmsTemplates = async (authorisedActivities) => {
    if (
      !authorisedActivities.includes(
        "LIST_ALLOWED_SMS_TEMPLATES_ON_ACTION_RULES",
      )
    )
      return;

    const packCodes = await getActionRuleSmsPackCodesForSurvey(
      authorisedActivities,
      this.props.surveyId,
    );
    this.setState({ smsPackCodes: packCodes });
  };

  getEmailTemplates = async (authorisedActivities) => {
    if (
      !authorisedActivities.includes(
        "LIST_ALLOWED_EMAIL_TEMPLATES_ON_ACTION_RULES",
      )
    )
      return;

    const packCodes = await getActionRuleEmailPackCodesForSurvey(
      authorisedActivities,
      this.props.surveyId,
    );
    this.setState({ emailPackCodes: packCodes });
  };

  openCollectionInstrumentRulesDialog = () => {
    this.setState({ collectionInstrumentRulesDisplayed: true });
  };

  closeCollectionInstrumentRulesDialog = () => {
    this.setState({ collectionInstrumentRulesDisplayed: false });
  };

  openCreateActionDialog = () => {
    this.createActionRuleDisabled = false;

    this.setState({
      newActionRuleType: "",
      actionRuleTypeValidationError: false,
      newActionRuleExportFilePackCode: "",
      newActionRuleSmsPackCode: "",
      newActionRuleSmsPhoneNumberColumn: "",
      newActionRuleEmailPackCode: "",
      newActionRuleEmailColumn: "",
      packCodeValidationError: false,
      smsPhoneNumberColumnValidationError: false,
      emailColumnValidationError: false,
      uacQidMetadataValidationError: false,
      newActionRuleClassifiers: "",
      newUacQidMetadata: "",
      createActionRulesDialogDisplayed: true,
      newActionRuleTriggerDate: this.getTimeNowForDateTimePicker(),
    });
  };

  openRescheduleDialog = (actionRule) => {
    this.rescheduleActionRuleDisabled = false;

    this.setState({
      actionRuleToBeUpdated: actionRule,
      rescheduleActionRulesDialogDisplayed: true,
      updatedTriggerDateTime: this.getTimeNowForDateTimePicker(),
      newActionRuleTriggerDate: this.getTimeNowForDateTimePicker(),
    });
  };

  closeDialog = () => {
    this.setState({ createActionRulesDialogDisplayed: false });
  };

  closeRescheduleDialog = () => {
    this.setState({
      actionRuleToBeUpdated: {},
      rescheduleActionRulesDialogDisplayed: false,
    });
  };

  onNewActionRuleExportFilePackCodeChange = (event) => {
    this.setState({
      exportFilePackCodeValidationError: false,
      newActionRuleExportFilePackCode: event.target.value,
    });
  };

  onNewActionRuleSmsPackCodeChange = (event) => {
    this.setState({
      smsPackCodeValidationError: false,
      newActionRuleSmsPackCode: event.target.value,
    });
  };

  onNewActionRuleEmailPackCodeChange = (event) => {
    this.setState({
      emailPackCodeValidationError: false,
      newActionRuleEmailPackCode: event.target.value,
    });
  };

  onNewActionRuleClassifiersChange = (event) => {
    this.setState({
      newActionRuleClassifiers: event.target.value,
    });
  };

  onNewActionRuleTriggerDateChange = (event) => {
    this.setState({ newActionRuleTriggerDate: event.target.value });
  };

  onNewActionRuleTypeChange = (event) => {
    this.setState({
      newActionRuleType: event.target.value,
      actionRuleTypeValidationError: false,
    });
  };

  onNewActionRuleSmsPhoneNumberChange = (event) => {
    this.setState({
      newActionRuleSmsPhoneNumberColumn: event.target.value,
      smsPhoneNumberColumnValidationError: false,
    });
  };

  onNewActionRuleEmailChange = (event) => {
    this.setState({
      newActionRuleEmailColumn: event.target.value,
      emailColumnValidationError: false,
    });
  };

  onNewActionRuleUacQidMetadataChange = (event) => {
    this.setState({
      newUacQidMetadata: event.target.value,
      uacQidMetadataValidationError: false,
    });
  };

  onCreateActionRule = async () => {
    if (this.createActionRuleDisabled) {
      return;
    }

    this.createActionRuleDisabled = true;

    var failedValidation = false;

    if (!this.state.newActionRuleType) {
      this.setState({ actionRuleTypeValidationError: true });
      failedValidation = true;
    }

    if (
      !this.state.newActionRuleExportFilePackCode &&
      this.state.newActionRuleType === "EXPORT_FILE"
    ) {
      this.setState({ exportFilePackCodeValidationError: true });
      failedValidation = true;
    }

    if (
      !this.state.newActionRuleSmsPackCode &&
      this.state.newActionRuleType === "SMS"
    ) {
      this.setState({ smsPackCodeValidationError: true });
      failedValidation = true;
    }

    if (
      !this.state.newActionRuleEmailPackCode &&
      this.state.newActionRuleType === "EMAIL"
    ) {
      this.setState({ emailPackCodeValidationError: true });
      failedValidation = true;
    }

    if (
      !this.state.newActionRuleSmsPhoneNumberColumn &&
      this.state.newActionRuleType === "SMS"
    ) {
      this.setState({ smsPhoneNumberColumnValidationError: true });
      failedValidation = true;
    }

    if (
      !this.state.newActionRuleEmailColumn &&
      this.state.newActionRuleType === "EMAIL"
    ) {
      this.setState({ emailColumnValidationError: true });
      failedValidation = true;
    }

    var uacMetadataJson = null;

    if (this.state.newUacQidMetadata.length > 0) {
      try {
        const parsedJson = JSON.parse(this.state.newUacQidMetadata);
        if (Object.keys(parsedJson).length === 0) {
          this.setState({ uacQidMetadataValidationError: true });
          failedValidation = true;
        }
      } catch (err) {
        this.setState({ uacQidMetadataValidationError: true });
        failedValidation = true;
      }
      uacMetadataJson = JSON.parse(this.state.newUacQidMetadata);
    }

    if (failedValidation) {
      this.createActionRuleDisabled = false;
      return;
    }

    let newActionRulePackCode = "";
    let newActionRuleSmsPhoneNumberColumn = null;
    let newActionRuleEmailColumn = null;

    if (this.state.newActionRuleType === "EXPORT_FILE") {
      newActionRulePackCode = this.state.newActionRuleExportFilePackCode;
    }

    if (this.state.newActionRuleType === "SMS") {
      newActionRulePackCode = this.state.newActionRuleSmsPackCode;
      newActionRuleSmsPhoneNumberColumn =
        this.state.newActionRuleSmsPhoneNumberColumn;
    }

    if (this.state.newActionRuleType === "EMAIL") {
      newActionRulePackCode = this.state.newActionRuleEmailPackCode;
      newActionRuleEmailColumn = this.state.newActionRuleEmailColumn;
    }

    const newActionRule = {
      type: this.state.newActionRuleType,
      triggerDateTime: new Date(
        this.state.newActionRuleTriggerDate,
      ).toISOString(),
      classifiers: this.state.newActionRuleClassifiers,
      packCode: newActionRulePackCode,
      collectionExerciseId: this.props.collectionExerciseId,
      phoneNumberColumn: newActionRuleSmsPhoneNumberColumn,
      emailColumn: newActionRuleEmailColumn,
      uacMetadata: uacMetadataJson,
    };

    const response = await fetch("/api/actionRules", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(newActionRule),
    });

    if (response.ok) {
      this.setState({ createActionRulesDialogDisplayed: false });
    }
    this.getActionRules(this.state.authorisedActivities);
  };

  onRescheduleActionRule = async () => {
    if (this.rescheduleActionRuleDisabled) {
      return;
    }

    this.rescheduleActionRuleDisabled = true;

    // Error checking in case action rule doesn't exist anymore
    if (!this.state.actionRules.includes(this.state.actionRuleToBeUpdated)) {
      this.rescheduleActionRuleDisabled = false;
      return;
    }

    const actionRule = this.state.actionRuleToBeUpdated;
    const dateISOString = new Date(
      this.state.newActionRuleTriggerDate,
    ).toISOString();

    if (
      !confirm(`Are you sure you wish to change the date for
${actionRule.type} from ${actionRule.triggerDateTime} to ${dateISOString}?`)
    ) {
      this.setState({
        actionRuleToBeUpdated: {},
        rescheduleActionRulesDialogDisplayed: false,
      });
      return;
    }

    actionRule.triggerDateTime = dateISOString;

    const response = await fetch("/api/actionRules", {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(actionRule),
    });

    if (response.ok) {
      this.setState({
        createActionRulesDialogDisplayed: false,
        actionRuleToBeUpdated: {},
      });
    } else {
      alert("Something went wrong\n" + response.statusText);
    }

    this.getActionRules(this.state.authorisedActivities);
  };

  getTimeNowForDateTimePicker = () => {
    var dateNow = new Date();
    dateNow.setMinutes(dateNow.getMinutes() - dateNow.getTimezoneOffset());
    return dateNow.toJSON().slice(0, 16);
  };

  render() {
    const collectionExerciseDetails = (
      <>
        <TableRow key={this.state.collectionExerciseDetails.id}>
          <TableCell component="th" scope="row">
            {this.state.collectionExerciseDetails.id}
          </TableCell>
          <TableCell component="th" scope="row">
            {this.state.collectionExerciseDetails.reference}
          </TableCell>
          <TableCell component="th" scope="row">
            {this.state.collectionExerciseDetails.startDate}
          </TableCell>
          <TableCell component="th" scope="row">
            {this.state.collectionExerciseDetails.endDate}
          </TableCell>
          <TableCell component="th" scope="row">
            {JSON.stringify(this.state.collectionExerciseDetails.metadata)}
          </TableCell>
          <TableCell component="th" scope="row">
            <Button
              variant="contained"
              onClick={() => this.openCollectionInstrumentRulesDialog()}
            >
              View Rules
            </Button>
          </TableCell>
        </TableRow>
      </>
    );

    const sortedActionRules = this.state.actionRules.sort((first, second) =>
      first.triggerDateTime.localeCompare(second.triggerDateTime),
    );

    const actionRuleTableRows = sortedActionRules.map((actionRule, index) => {
      return (
        <TableRow key={index}>
          <TableCell component="th" scope="row">
            {actionRule.type}
          </TableCell>
          <TableCell component="th" scope="row">
            {actionRule.triggerDateTime}
          </TableCell>
          <TableCell component="th" scope="row" id="hasTriggered">
            {actionRule.hasTriggered ? "YES" : "NO"}
          </TableCell>
          <TableCell component="th" scope="row">
            {JSON.stringify(actionRule.uacMetadata)}
          </TableCell>
          <TableCell component="th" scope="row">
            {actionRule.classifiers}
          </TableCell>
          <TableCell component="th" scope="row">
            {actionRule.packCode}
          </TableCell>
          <TableCell component="th" scope="row">
            <Button
              variant="contained"
              onClick={() => this.openRescheduleDialog(actionRule)}
              id="createActionRuleDialogBtn"
            >
              Reschedule
            </Button>
          </TableCell>
        </TableRow>
      );
    });

    const exportFilePackCodeMenuItems = this.state.exportFilePackCodes.map(
      (packCode) => (
        <MenuItem key={packCode} value={packCode} id={packCode}>
          {packCode}
        </MenuItem>
      ),
    );

    const smsPackCodeMenuItems = this.state.smsPackCodes.map((packCode) => (
      <MenuItem key={packCode} value={packCode} id={packCode}>
        {packCode}
      </MenuItem>
    ));

    const emailPackCodeMenuItems = this.state.emailPackCodes.map((packCode) => (
      <MenuItem key={packCode} value={packCode} id={packCode}>
        {packCode}
      </MenuItem>
    ));

    const sensitiveSampleColumnsMenuItems =
      this.state.sensitiveSampleColumns.map((column) => (
        <MenuItem key={column} value={column} id={column}>
          {column}
        </MenuItem>
      ));

    let allowedActionRuleTypeMenuItems = [];
    if (
      this.state.authorisedActivities.includes("CREATE_EXPORT_FILE_ACTION_RULE")
    ) {
      allowedActionRuleTypeMenuItems.push(
        <MenuItem value={"EXPORT_FILE"}>Export File</MenuItem>,
      );
    }

    if (this.state.authorisedActivities.includes("CREATE_SMS_ACTION_RULE")) {
      allowedActionRuleTypeMenuItems.push(
        <MenuItem value={"SMS"}>SMS</MenuItem>,
      );
    }

    if (this.state.authorisedActivities.includes("CREATE_EMAIL_ACTION_RULE")) {
      allowedActionRuleTypeMenuItems.push(
        <MenuItem value={"EMAIL"}>Email</MenuItem>,
      );
    }

    if (
      this.state.authorisedActivities.includes(
        "CREATE_FACE_TO_FACE_ACTION_RULE",
      )
    ) {
      allowedActionRuleTypeMenuItems.push(
        <MenuItem value={"FACE_TO_FACE"}>Face to face</MenuItem>,
      );
    }
    if (
      this.state.authorisedActivities.includes(
        "CREATE_OUTBOUND_PHONE_ACTION_RULE",
      )
    ) {
      allowedActionRuleTypeMenuItems.push(
        <MenuItem value={"OUTBOUND_TELEPHONE"}>Outbound Phone</MenuItem>,
      );
    }
    if (
      this.state.authorisedActivities.includes(
        "CREATE_DEACTIVATE_UAC_ACTION_RULE",
      )
    ) {
      allowedActionRuleTypeMenuItems.push(
        <MenuItem value={"DEACTIVATE_UAC"}>Deactivate UAC</MenuItem>,
      );
    }
    if (
      this.state.authorisedActivities.includes("CREATE_EQ_FLUSH_ACTION_RULE")
    ) {
      allowedActionRuleTypeMenuItems.push(
        <MenuItem value={"EQ_FLUSH"}>EQ Flush</MenuItem>,
      );
    }

    return (
      <div style={{ padding: 20 }}>
        <Link to={`/survey?surveyId=${this.props.surveyId}`}>
          ← Back to survey
        </Link>
        <Typography variant="h4" color="inherit" style={{ marginBottom: 20 }}>
          Collection Exercise: {this.state.collectionExerciseDetails.name}
        </Typography>
        {this.state.authorisedActivities.includes(
          "VIEW_COLLECTION_EXERCISE",
        ) && (
          <div>
            <Typography variant="h6" color="inherit" style={{ marginTop: 10 }}>
              Collection Exercise Details
            </Typography>
            <TableContainer component={Paper}>
              <Table id="collectionExerciseTableList">
                <TableHead>
                  <TableRow>
                    <TableCell>ID</TableCell>
                    <TableCell>Reference</TableCell>
                    <TableCell>Start Date</TableCell>
                    <TableCell>End Date</TableCell>
                    <TableCell>Metadata</TableCell>
                    <TableCell>Collection Instrument Rules</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>{collectionExerciseDetails}</TableBody>
              </Table>
            </TableContainer>
            {this.state.collectionInstrumentRulesDisplayed && (
              <Dialog open={true}>
                <DialogContent style={{ padding: 30 }}>
                  <JSONPretty
                    id="json-pretty"
                    data={
                      this.state.collectionExerciseDetails
                        .collectionInstrumentSelectionRules
                    }
                    style={{
                      overflowY: "scroll",
                      margin: 10,
                      maxHeight: 500,
                    }}
                  />
                  <div>
                    <Button
                      onClick={this.closeCollectionInstrumentRulesDialog}
                      variant="contained"
                      style={{ margin: 10, padding: 10 }}
                    >
                      Close
                    </Button>
                  </div>
                </DialogContent>
              </Dialog>
            )}
          </div>
        )}
        {this.state.authorisedActivities.includes("LIST_ACTION_RULES") && (
          <>
            <Typography variant="h6" color="inherit" style={{ marginTop: 20 }}>
              Action Rules
            </Typography>
            <TableContainer component={Paper}>
              <Table id="actionRuleTable">
                <TableHead>
                  <TableRow>
                    <TableCell>Type</TableCell>
                    <TableCell>Trigger date</TableCell>
                    <TableCell>Has triggered?</TableCell>
                    <TableCell>UAC Metadata</TableCell>
                    <TableCell>Classifiers</TableCell>
                    <TableCell>Pack Code</TableCell>
                    <TableCell></TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>{actionRuleTableRows}</TableBody>
              </Table>
            </TableContainer>
          </>
        )}
        {allowedActionRuleTypeMenuItems.length > 0 && (
          <div style={{ marginTop: 10 }}>
            <Button
              variant="contained"
              onClick={this.openCreateActionDialog}
              id="createActionRuleDialogBtn"
            >
              Create Action Rule
            </Button>
          </div>
        )}
        {(this.state.authorisedActivities.includes("LOAD_SAMPLE") ||
          this.state.authorisedActivities.includes(
            "VIEW_SAMPLE_LOAD_PROGRESS",
          )) && (
          <SampleUpload
            authorisedActivities={this.state.authorisedActivities}
            collectionExerciseId={this.props.collectionExerciseId}
          />
        )}
        <Dialog open={this.state.createActionRulesDialogDisplayed}>
          <DialogContent style={{ padding: 30 }}>
            <div>
              <div>
                <FormControl required fullWidth={true}>
                  <InputLabel>Type</InputLabel>
                  <Select
                    onChange={this.onNewActionRuleTypeChange}
                    value={this.state.newActionRuleType}
                    error={this.state.actionRuleTypeValidationError}
                    id="selectActionRuleType"
                  >
                    {allowedActionRuleTypeMenuItems}
                  </Select>
                </FormControl>
                {this.state.newActionRuleType === "EXPORT_FILE" && (
                  <>
                    <FormControl required fullWidth={true}>
                      <InputLabel>Pack Code</InputLabel>
                      <Select
                        onChange={this.onNewActionRuleExportFilePackCodeChange}
                        value={this.state.newActionRuleExportFilePackCode}
                        error={this.state.exportFilePackCodeValidationError}
                        id="selectActionRuleExportFilePackCode"
                      >
                        {exportFilePackCodeMenuItems}
                      </Select>
                    </FormControl>
                    <FormControl fullWidth={true}>
                      <TextField
                        style={{ minWidth: 200 }}
                        error={this.state.uacQidMetadataValidationError}
                        label="UAC QID Metadata"
                        onChange={this.onNewActionRuleUacQidMetadataChange}
                        value={this.state.newUacQidMetadata}
                      />
                    </FormControl>
                  </>
                )}
                {this.state.newActionRuleType === "SMS" && (
                  <>
                    <FormControl required fullWidth={true}>
                      <InputLabel>Pack Code</InputLabel>
                      <Select
                        onChange={this.onNewActionRuleSmsPackCodeChange}
                        value={this.state.newActionRuleSmsPackCode}
                        error={this.state.smsPackCodeValidationError}
                      >
                        {smsPackCodeMenuItems}
                      </Select>
                    </FormControl>
                    <FormControl required fullWidth={true}>
                      <InputLabel>Phone Number Column</InputLabel>
                      <Select
                        onChange={this.onNewActionRuleSmsPhoneNumberChange}
                        value={this.state.newActionRuleSmsPhoneNumberColumn}
                        error={this.state.smsPhoneNumberColumnValidationError}
                      >
                        {sensitiveSampleColumnsMenuItems}
                      </Select>
                    </FormControl>
                    <FormControl fullWidth={true}>
                      <TextField
                        style={{ minWidth: 200 }}
                        error={this.state.uacQidMetadataValidationError}
                        label="UAC QID Metadata"
                        onChange={this.onNewActionRuleUacQidMetadataChange}
                        value={this.state.newUacQidMetadata}
                      />
                    </FormControl>
                  </>
                )}
                {this.state.newActionRuleType === "EMAIL" && (
                  <>
                    <FormControl required fullWidth={true}>
                      <InputLabel>Pack Code</InputLabel>
                      <Select
                        onChange={this.onNewActionRuleEmailPackCodeChange}
                        value={this.state.newActionRuleEmailPackCode}
                        error={this.state.emailPackCodeValidationError}
                        id="selectActionRuleEmailPackCode"
                      >
                        {emailPackCodeMenuItems}
                      </Select>
                    </FormControl>
                    <FormControl required fullWidth={true}>
                      <InputLabel>Email Column</InputLabel>
                      <Select
                        onChange={this.onNewActionRuleEmailChange}
                        value={this.state.newActionRuleEmailColumn}
                        error={this.state.emailColumnValidationError}
                        id="selectActionRuleEmailColumn"
                      >
                        {sensitiveSampleColumnsMenuItems}
                      </Select>
                    </FormControl>
                    <FormControl fullWidth={true}>
                      <TextField
                        style={{ minWidth: 200 }}
                        error={this.state.uacQidMetadataValidationError}
                        label="UAC QID Metadata"
                        onChange={this.onNewActionRuleUacQidMetadataChange}
                        value={this.state.newUacQidMetadata}
                      />
                    </FormControl>
                  </>
                )}
                <TextField
                  fullWidth={true}
                  style={{ marginTop: 20 }}
                  error={this.state.classifiersValidationError}
                  label="Classifiers"
                  onChange={this.onNewActionRuleClassifiersChange}
                  value={this.state.newActionRuleClassifiers}
                />
                <TextField
                  label="Trigger Date"
                  type="datetime-local"
                  value={this.state.newActionRuleTriggerDate}
                  onChange={this.onNewActionRuleTriggerDateChange}
                  style={{ marginTop: 20 }}
                  InputLabelProps={{
                    shrink: true,
                  }}
                />
              </div>
              <div style={{ marginTop: 10 }}>
                <Button
                  onClick={this.onCreateActionRule}
                  variant="contained"
                  style={{ margin: 10 }}
                  id="createActionRuleBtn"
                >
                  Create action rule
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
        <Dialog open={this.state.rescheduleActionRulesDialogDisplayed}>
          <DialogContent style={{ padding: 30 }}>
            <div>
              <div>
                <TextField
                  label="Trigger Date"
                  type="datetime-local"
                  value={this.state.newActionRuleTriggerDate}
                  onChange={this.onNewActionRuleTriggerDateChange}
                  style={{ marginTop: 20 }}
                  InputLabelProps={{
                    shrink: true,
                  }}
                />
              </div>
              <div style={{ marginTop: 10 }}>
                <Button
                  onClick={this.onRescheduleActionRule}
                  variant="contained"
                  style={{ margin: 10 }}
                  id="createActionRuleBtn"
                >
                  Reschedule
                </Button>
                <Button
                  onClick={this.closeRescheduleDialog}
                  variant="contained"
                  style={{ margin: 10 }}
                >
                  Cancel
                </Button>
              </div>
            </div>
          </DialogContent>
        </Dialog>
        {["LOAD_BULK_REFUSAL", "VIEW_BULK_REFUSAL_PROGRESS"].some((p) =>
          this.state.authorisedActivities.includes(p),
        ) && (
          <>
            <div style={{ marginTop: 20 }}>
              <Link
                to={`/bulkUploads?surveyId=${this.props.surveyId}&collexId=${this.props.collectionExerciseId}`}
              >
                Bulk Uploads
              </Link>
            </div>
          </>
        )}
      </div>
    );
  }
}

export default CollectionExerciseDetails;
