import React, { Component } from "react";
import { Link } from "react-router-dom";
import Table from "@material-ui/core/Table";
import TableBody from "@material-ui/core/TableBody";
import TableCell from "@material-ui/core/TableCell";
import TableRow from "@material-ui/core/TableRow";
import TableContainer from "@material-ui/core/TableContainer";
import TableHead from "@material-ui/core/TableHead";
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
import { errorAlert, getAuthorisedActivities } from "./Utils";

class SurveysList extends Component {
  state = {
    thisUserAdminGroups: [],
    isLoading: true,
    surveys: [],
    createSurveyDialogDisplayed: false,
    validationError: false,
    newSurveyName: "",
    surveyMetadataError: false,
    newSurveyValidationRules: "",
    newSurveySampleDefinitionUrl: "",
    newSurveyMetadata: "",
    newSurveyHeaderRow: true,
    newSurveySampleSeparator: ",",
    validationRulesValidationError: false,
    sampleDefinitionUrlError: false,
    authorisedActivities: [],
  };

  componentDidMount() {
    this.getBackEndData();
  }

  getBackEndData = async () => {
    const authorisedActivities = await getAuthorisedActivities();
    this.setState({ authorisedActivities: authorisedActivities });
    this.refreshDataFromBackend(this.state.authorisedActivities);
  };

  refreshDataFromBackend = async (authorisedActivities) => {
    if (!authorisedActivities.includes("LIST_SURVEYS")) return;

    const response = await fetch("/api/surveys");
    const surveyJson = await response.json();

    this.setState({ surveys: surveyJson });
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
      newSurveySampleDefinitionUrl: "",
      newSurveyMetadata: "",
    });
  };

  closeDialog = () => {
    // No. Do not. Do not put anything extra in here. This method ONLY deals with closing the dialog.
    this.setState({ createSurveyDialogDisplayed: false });
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
      newSurveySampleDefinitionUrl: event.target.value,
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

    if (!this.state.newSurveySampleDefinitionUrl.trim()) {
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
      sampleDefinitionUrl: this.state.newSurveySampleDefinitionUrl,
      metadata: metadataJson,
    };

    const response = await fetch("/api/surveys", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(newSurvey),
    });

    if (response.ok) {
      this.setState({ createSurveyDialogDisplayed: false });
      this.refreshDataFromBackend(this.state.authorisedActivities);
    } else {
      this.createSurveyInProgress = false;
      const responseJson = await response.json();
      errorAlert(responseJson);
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

    return (
      <>
        {this.state.authorisedActivities.includes("LIST_SURVEYS") && (
          <>
            <Typography variant="h6" color="inherit">
              Surveys
            </Typography>
            <TableContainer component={Paper}>
              <Table id="surveyListTable">
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
            id="createSurveyBtn"
            variant="contained"
            onClick={this.openDialog}
            style={{ marginTop: 10 }}
          >
            Create Survey
          </Button>
        )}
        <Dialog open={this.state.createSurveyDialogDisplayed} fullWidth={true}>
          <DialogContent style={{ padding: 30 }}>
            <div>
              <div>
                <TextField
                  id="surveyNameTextField"
                  required
                  error={this.state.validationError}
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
                    id="headerRowSelect"
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
                    id="sampleFileSeparator"
                    onChange={this.onNewSurveySampleSeparatorChange}
                    value={this.state.newSurveySampleSeparator}
                  >
                    <MenuItem value={","}>Comma</MenuItem>
                    <MenuItem value={":"}>Colon</MenuItem>
                    <MenuItem value={"|"}>Pipe</MenuItem>
                  </Select>
                </FormControl>
                <TextField
                  id="validationRulesTextField"
                  style={{ marginTop: 10 }}
                  required
                  multiline
                  fullWidth={true}
                  error={this.state.validationRulesValidationError}
                  label="Validation rules"
                  onChange={this.onNewSurveyValidationRulesChange}
                  value={this.state.newSurveyValidationRules}
                />
                <TextField
                  id="surveyDefinitionURLTextField"
                  style={{ marginTop: 10 }}
                  required
                  multiline
                  fullWidth={true}
                  error={this.state.sampleDefinitionUrlError}
                  label="Survey Definition URL"
                  onChange={this.onNewSurveySampleDefinitionUrlChange}
                  value={this.state.newSurveySampleDefinitionUrl}
                />
                <TextField
                  id="metadataTextField"
                  style={{ marginTop: 10 }}
                  multiline
                  fullWidth={true}
                  error={this.state.surveyMetadataError}
                  label="Metadata"
                  onChange={this.onNewSurveyMetadataChange}
                  value={this.state.newSurveyMetadata}
                />
              </div>
              <div style={{ marginTop: 10 }}>
                <Button
                  id="postCreateSurveyBtn"
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
      </>
    );
  }
}

export default SurveysList;
