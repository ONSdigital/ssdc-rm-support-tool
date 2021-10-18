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
  getActionRulePrintTemplates,
  getActionRuleSmsTemplates,
  getSensitiveSampleColumns,
} from "./Utils";
import { Link } from "react-router-dom";

class CollectionExerciseDetails extends Component {
  state = {
    authorisedActivities: [],
    actionRules: [],
    printPackCodes: [],
    sensitiveSampleColumns: [],
    smsPackCodes: [],
    createActionRulesDialogDisplayed: false,
    printPackCodeValidationError: false,
    smsPackCodeValidationError: false,
    smsPhoneNumberColumnValidationError: false,
    actionRuleTypeValidationError: false,
    uacQidMetadataValidationError: false,
    collectionExerciseName: "",
    newActionRulePrintPackCode: "",
    newActionRuleSmsPackCode: "",
    newActionRuleSmsPhoneNumberColumn: "",
    newActionRuleClassifiers: "",
    newActionRuleType: "",
    newUacQidMetadata: "",
  };

  componentDidMount() {
    this.getAuthorisedBackendData();
  }

  componentWillUnmount() {
    clearInterval(this.interval);
  }

  getAuthorisedBackendData = async () => {
    const authorisedActivities = await this.getAuthorisedActivities(); // Only need to do this once; don't refresh it repeatedly as it changes infrequently
    this.getCollectionExerciseName(authorisedActivities);
    this.getActionRules(authorisedActivities);
    this.getPrintTemplates(authorisedActivities);
    this.getSmsTemplates(authorisedActivities);
    this.getSensitiveSampleColumns(authorisedActivities);

    this.interval = setInterval(
      () => this.getActionRules(authorisedActivities),
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

  getSensitiveSampleColumns = async (authorisedActivities) => {
    const sensitiveSampleColumns = await getSensitiveSampleColumns(
      authorisedActivities,
      this.props.surveyId
    );
    this.setState({ sensitiveSampleColumns: sensitiveSampleColumns });
  };

  getCollectionExerciseName = async (authorisedActivities) => {
    if (!authorisedActivities.includes("VIEW_COLLECTION_EXERCISE")) return;

    const response = await fetch(
      `api/collectionExercises/${this.props.collectionExerciseId}`
    );

    // TODO: We need more elegant error handling throughout the whole application, but this will at least protect temporarily
    if (!response.ok) {
      return;
    }

    const collectionExerciseJson = await response.json();

    this.setState({ collectionExerciseName: collectionExerciseJson.name });
  };

  getActionRules = async (authorisedActivities) => {
    if (!authorisedActivities.includes("LIST_ACTION_RULES")) return;

    const response = await fetch(
      `/api/actionRules/?collectionExercise=${this.props.collectionExerciseId}`
    );
    const actionRuleJson = await response.json();

    this.setState({
      actionRules: actionRuleJson,
    });
  };

  getPrintTemplates = async (authorisedActivities) => {
    if (
      !authorisedActivities.includes(
        "LIST_ALLOWED_PRINT_TEMPLATES_ON_ACTION_RULES"
      )
    )
      return;

    const packCodes = await getActionRulePrintTemplates(
      authorisedActivities,
      this.props.surveyId
    );
    this.setState({ printPackCodes: packCodes });
  };

  getSmsTemplates = async (authorisedActivities) => {
    if (
      !authorisedActivities.includes(
        "LIST_ALLOWED_SMS_TEMPLATES_ON_ACTION_RULES"
      )
    )
      return;

    const packCodes = await getActionRuleSmsTemplates(
      authorisedActivities,
      this.props.surveyId
    );
    this.setState({ smsPackCodes: packCodes });
  };

  openDialog = () => {
    this.setState({
      newActionRuleType: "",
      actionRuleTypeValidationError: false,
      newActionRulePrintPackCode: "",
      newActionRuleSmsPackCode: "",
      newActionRuleSmsPhoneNumberColumn: "",
      packCodeValidationError: false,
      smsPhoneNumberColumnValidationError: false,
      uacQidMetadataValidationError: false,
      newActionRuleClassifiers: "",
      newUacQidMetadata: "",
      createActionRulesDialogDisplayed: true,
      newActionRuleTriggerDate: this.getTimeNowForDateTimePicker(),
    });
  };

  closeDialog = () => {
    this.setState({ createActionRulesDialogDisplayed: false });
  };

  onNewActionRulePrintPackCodeChange = (event) => {
    this.setState({
      printPackCodeValidationError: false,
      newActionRulePrintPackCode: event.target.value,
    });
  };

  onNewActionRuleSmsPackCodeChange = (event) => {
    this.setState({
      smsPackCodeValidationError: false,
      newActionRuleSmsPackCode: event.target.value,
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

  onNewActionRuleUacQidMetadataChange = (event) => {
    this.setState({
      newUacQidMetadata: event.target.value,
      uacQidMetadataValidationError: false,
    });
  };

  onCreateActionRule = async () => {
    var failedValidation = false;

    if (!this.state.newActionRuleType) {
      this.setState({ actionRuleTypeValidationError: true });
      failedValidation = true;
    }

    if (
      !this.state.newActionRulePrintPackCode &&
      this.state.newActionRuleType === "PRINT"
    ) {
      this.setState({ printPackCodeValidationError: true });
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
      !this.state.newActionRuleSmsPhoneNumberColumn &&
      this.state.newActionRuleType === "SMS"
    ) {
      this.setState({ smsPhoneNumberColumnValidationError: true });
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
      return;
    }

    let newActionRulePackCode = "";
    let newActionRuleSmsPhoneNumberColumn = null;

    if (this.state.newActionRuleType === "PRINT") {
      newActionRulePackCode = this.state.newActionRulePrintPackCode;
    }

    if (this.state.newActionRuleType === "SMS") {
      newActionRulePackCode = this.state.newActionRuleSmsPackCode;
      newActionRuleSmsPhoneNumberColumn =
        this.state.newActionRuleSmsPhoneNumberColumn;
    }

    const newActionRule = {
      type: this.state.newActionRuleType,
      triggerDateTime: new Date(
        this.state.newActionRuleTriggerDate
      ).toISOString(),
      classifiers: this.state.newActionRuleClassifiers,
      packCode: newActionRulePackCode,
      collectionExerciseId: this.props.collectionExerciseId,
      phoneNumberColumn: newActionRuleSmsPhoneNumberColumn,
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
  };

  getTimeNowForDateTimePicker = () => {
    var dateNow = new Date();
    dateNow.setMinutes(dateNow.getMinutes() - dateNow.getTimezoneOffset());
    return dateNow.toJSON().slice(0, 16);
  };

  render() {
    const sortedActionRules = this.state.actionRules.sort((first, second) =>
      first.triggerDateTime.localeCompare(second.triggerDateTime)
    );

    const actionRuleTableRows = sortedActionRules.map(
      (actionRule, index) => {
        return (
          <TableRow key={index}>
            <TableCell component="th" scope="row">
              {actionRule.type}
            </TableCell>
            <TableCell component="th" scope="row">
              {actionRule.triggerDateTime}
            </TableCell>
            <TableCell component="th" scope="row">
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
          </TableRow>
        );
      }
    );

    const printPackCodeMenuItems = this.state.printPackCodes.map((packCode) => (
      <MenuItem key={packCode} value={packCode}>
        {packCode}
      </MenuItem>
    ));

    const smsPackCodeMenuItems = this.state.smsPackCodes.map((packCode) => (
      <MenuItem key={packCode} value={packCode}>
        {packCode}
      </MenuItem>
    ));

    const sensitiveSampleColumnsMenuItems =
      this.state.sensitiveSampleColumns.map((column) => (
        <MenuItem key={column} value={column}>
          {column}
        </MenuItem>
      ));

    let allowedActionRuleTypeMenuItems = [];
    if (this.state.authorisedActivities.includes("CREATE_PRINT_ACTION_RULE")) {
      allowedActionRuleTypeMenuItems.push(
        <MenuItem value={"PRINT"}>Print</MenuItem>
      );
    }
    if (this.state.authorisedActivities.includes("CREATE_SMS_ACTION_RULE")) {
      allowedActionRuleTypeMenuItems.push(
        <MenuItem value={"SMS"}>SMS</MenuItem>
      );
    }
    if (
      this.state.authorisedActivities.includes(
        "CREATE_FACE_TO_FACE_ACTION_RULE"
      )
    ) {
      allowedActionRuleTypeMenuItems.push(
        <MenuItem value={"FACE_TO_FACE"}>Face to face</MenuItem>
      );
    }
    if (
      this.state.authorisedActivities.includes(
        "CREATE_OUTBOUND_PHONE_ACTION_RULE"
      )
    ) {
      allowedActionRuleTypeMenuItems.push(
        <MenuItem value={"OUTBOUND_TELEPHONE"}>Outbound phone</MenuItem>
      );
    }
    if (
      this.state.authorisedActivities.includes(
        "CREATE_DEACTIVATE_UAC_ACTION_RULE"
      )
    ) {
      allowedActionRuleTypeMenuItems.push(
        <MenuItem value={"DEACTIVATE_UAC"}>Deactivate UAC</MenuItem>
      );
    }

    return (
      <div style={{ padding: 20 }}>
        <Link to={`/survey?surveyId=${this.props.surveyId}`}>
          ← Back to survey
        </Link>
        <Typography variant="h4" color="inherit" style={{ marginBottom: 20 }}>
          Collection Exercise Details - {this.state.collectionExerciseName}
        </Typography>
        {this.state.authorisedActivities.includes("LIST_ACTION_RULES") && (
          <>
            <Typography variant="h6" color="inherit" style={{ marginTop: 20 }}>
              Action Rules
            </Typography>
            <TableContainer component={Paper}>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>Type</TableCell>
                    <TableCell>Trigger date</TableCell>
                    <TableCell>Has triggered?</TableCell>
                    <TableCell>UAC Metadata</TableCell>
                    <TableCell>Classifiers</TableCell>
                    <TableCell>Pack Code</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>{actionRuleTableRows}</TableBody>
              </Table>
            </TableContainer>
          </>
        )}
        {allowedActionRuleTypeMenuItems.length > 0 && (
          <div style={{ marginTop: 10 }}>
            <Button variant="contained" onClick={this.openDialog}>
              Create Action Rule
            </Button>
          </div>
        )}
        {(this.state.authorisedActivities.includes("LOAD_SAMPLE") ||
          this.state.authorisedActivities.includes(
            "VIEW_SAMPLE_LOAD_PROGRESS"
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
                  >
                    {allowedActionRuleTypeMenuItems}
                  </Select>
                </FormControl>
                {this.state.newActionRuleType === "PRINT" && (
                  <>
                    <FormControl required fullWidth={true}>
                      <InputLabel>Pack Code</InputLabel>
                      <Select
                        onChange={this.onNewActionRulePrintPackCodeChange}
                        value={this.state.newActionRulePrintPackCode}
                        error={this.state.printPackCodeValidationError}
                      >
                        {printPackCodeMenuItems}
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
      </div>
    );
  }
}

export default CollectionExerciseDetails;
