import React, { Component } from "react";
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

class ExportFileTemplateList extends Component {
  state = {
    exportFileTemplates: [],
    createExportFileTemplatePackCodeError: "",
    createExportFileTemplateDialogDisplayed: false,
    exportFileDestinations: [],
    exportFileDestination: "",
    exportFileDestinationValidationError: false,
    authorisedActivities: [],
    description: "",
    packCode: "",
    template: "",
    descriptionValidationError: false,
    packCodeValidationError: false,
    templateValidationErrorMessage: "",
    templateValidationError: false,
    newTemplateMetadataValidationError: false,
  };

  componentDidMount() {
    this.getBackEndData();
  }

  getBackEndData = async () => {
    const authorisedActivities = await getAuthorisedActivities();
    this.setState({ authorisedActivities: authorisedActivities });

    this.refreshDataFromBackend(authorisedActivities);
  };

  refreshDataFromBackend = async (authorisedActivities) => {
    this.getExportFileTemplates(authorisedActivities);
    this.getExportFileDestinations(authorisedActivities);
  };

  getExportFileTemplates = async (authorisedActivities) => {
    if (!authorisedActivities.includes("LIST_EXPORT_FILE_TEMPLATES")) return;

    const response = await fetch("/api/exportFileTemplates");
    const templateJson = await response.json();

    this.setState({ exportFileTemplates: templateJson });
  };

  getExportFileDestinations = async (authorisedActivities) => {
    if (!authorisedActivities.includes("LIST_EXPORT_FILE_DESTINATIONS")) return;

    const supplierResponse = await fetch("/api/exportFileDestinations");
    const supplierJson = await supplierResponse.json();

    this.setState({
      exportFileDestinations: supplierJson,
    });
  };

  openExportFileTemplateDialog = () => {
    this.createExportFileTemplateInProgress = false;

    // Yes. Yes here. Here is the one and ONLY place where you should be preparing the dialog
    this.setState({
      exportFileDestination: "",
      description: "",
      packCode: "",
      template: "",
      newTemplateMetadata: "",
      exportFileDestinationValidationError: false,
      descriptionValidationError: false,
      packCodeValidationError: false,
      templateValidationErrorMessage: "",
      templateValidationError: false,
      newTemplateMetadataValidationError: false,
      createExportFileTemplatePackCodeError: "",
      createExportFileTemplateDialogDisplayed: true,
    });
  };

  closeExportFileTemplateDialog = () => {
    // No. Do not. Do not put anything extra in here. This method ONLY deals with closing the dialog.
    this.setState({
      createExportFileTemplateDialogDisplayed: false,
    });

    this.refresh;
  };

  onExportFileDestinationChange = (event) => {
    this.setState({
      exportFileDestination: event.target.value,
      exportFileDestinationValidationError: false,
    });
  };

  onCreateExportFileTemplate = async () => {
    if (this.createExportFileTemplateInProgress) {
      return;
    }

    this.createExportFileTemplateInProgress = true;

    this.setState({
      createExportFileTemplatePackCodeError: "",
      packCodeValidationError: false,
    });

    var failedValidation = false;

    if (!this.state.exportFileDestination.trim()) {
      this.setState({ exportFileDestinationValidationError: true });
      failedValidation = true;
    }

    if (!this.state.description.trim()) {
      this.setState({ descriptionValidationError: true });
      failedValidation = true;
    }

    if (!this.state.packCode.trim()) {
      this.setState({ packCodeValidationError: true });
      failedValidation = true;
    }

    if (
      this.state.exportFileTemplates.some(
        (exportFileTemplate) =>
          exportFileTemplate.packCode.toUpperCase() ===
          this.state.packCode.toUpperCase(),
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

        const hasDuplicateTemplateColumns =
          new Set(parsedJson).size !== parsedJson.length;
        if (hasDuplicateTemplateColumns) {
          this.setState({
            templateValidationError: true,
            templateValidationErrorMessage:
              "Template cannot have duplicate columns",
          });
          failedValidation = true;
        }
      } catch (err) {
        this.setState({ templateValidationError: true });
        failedValidation = true;
      }
    }

    let metadata = null;

    if (this.state.newTemplateMetadata) {
      try {
        metadata = JSON.parse(this.state.newTemplateMetadata);
        if (Object.keys(metadata).length === 0) {
          this.setState({ newTemplateMetadataValidationError: true });
          failedValidation = true;
        }
      } catch (err) {
        this.setState({ newTemplateMetadataValidationError: true });
        failedValidation = true;
      }
    }

    if (failedValidation) {
      this.createExportFileTemplateInProgress = false;
      return;
    }

    const newExportFileTemplate = {
      description: this.state.description,
      packCode: this.state.packCode,
      exportFileDestination: this.state.exportFileDestination,
      template: JSON.parse(this.state.template),
      metadata: metadata,
    };

    const response = await fetch("/api/exportFileTemplates", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(newExportFileTemplate),
    });

    // TODO: We need more elegant error handling throughout the whole application, but this will at least protect temporarily
    if (!response.ok) {
      this.createExportFileTemplateInProgress = false;
      const responseJson = await response.json();
      errorAlert(responseJson);
    } else {
      this.setState({ createExportFileTemplateDialogDisplayed: false });
    }

    this.getExportFileTemplates(this.state.authorisedActivities);
  };

  onDescriptionChange = (event) => {
    const resetValidation = !event.target.value.trim();

    this.setState({
      description: event.target.value,
      descriptionValidationError: resetValidation,
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
      templateValidationErrorMessage: "",
    });
  };

  onNewTemplateMetadataChange = (event) => {
    this.setState({
      newTemplateMetadata: event.target.value,
      newTemplateMetadataValidationError: false,
    });
  };

  render() {
    const exportFileTemplateRows = this.state.exportFileTemplates.map(
      (exportFileTemplate) => (
        <TableRow key={exportFileTemplate.packCode}>
          <TableCell component="th" scope="row" id="exportFileTemplatePackcode">
            {exportFileTemplate.packCode}
          </TableCell>
          <TableCell component="th" scope="row">
            {exportFileTemplate.description}
          </TableCell>
          <TableCell component="th" scope="row">
            {exportFileTemplate.exportFileDestination}
          </TableCell>
          <TableCell component="th" scope="row">
            {JSON.stringify(exportFileTemplate.template)}
          </TableCell>
          <TableCell component="th" scope="row">
            {JSON.stringify(exportFileTemplate.metadata)}
          </TableCell>
        </TableRow>
      ),
    );

    const exportFileDestinationMenuItems =
      this.state.exportFileDestinations.map((supplier) => (
        <MenuItem key={supplier} value={supplier} id={supplier}>
          {supplier}
        </MenuItem>
      ));

    return (
      <>
        {this.state.authorisedActivities.includes(
          "LIST_EXPORT_FILE_TEMPLATES",
        ) && (
          <>
            <Typography variant="h6" color="inherit" style={{ marginTop: 10 }}>
              Export File Templates
            </Typography>
            <TableContainer component={Paper}>
              <Table id="exportFileTemplateTable">
                <TableHead>
                  <TableRow>
                    <TableCell>Pack Code</TableCell>
                    <TableCell>Description</TableCell>
                    <TableCell>Export File Destination</TableCell>
                    <TableCell>Template</TableCell>
                    <TableCell>Metadata</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>{exportFileTemplateRows}</TableBody>
              </Table>
            </TableContainer>
          </>
        )}
        {this.state.authorisedActivities.includes(
          "CREATE_EXPORT_FILE_TEMPLATE",
        ) && (
          <Button
            variant="contained"
            onClick={this.openExportFileTemplateDialog}
            style={{ marginTop: 10 }}
            id="createExportFileTemplateBtn"
          >
            Create Export File Template
          </Button>
        )}

        <Dialog
          open={this.state.createExportFileTemplateDialogDisplayed}
          fullWidth={true}
        >
          <DialogContent style={{ padding: 30 }}>
            <div>
              <div>
                <TextField
                  required
                  fullWidth={true}
                  error={this.state.packCodeValidationError}
                  label="Pack Code"
                  onChange={this.onPackCodeChange}
                  value={this.state.packCode}
                  helperText={this.state.createExportFileTemplatePackCodeError}
                  id="packCodeTextField"
                />
                <TextField
                  required
                  fullWidth={true}
                  style={{ marginTop: 10 }}
                  error={this.state.descriptionValidationError}
                  label="Description"
                  onChange={this.onDescriptionChange}
                  value={this.state.description}
                  id="descriptionTextField"
                />
                <FormControl
                  required
                  fullWidth={true}
                  style={{ marginTop: 10 }}
                  id="form"
                >
                  <InputLabel>Export File Destination</InputLabel>
                  <Select
                    onChange={this.onExportFileDestinationChange}
                    value={this.state.exportFileDestination}
                    error={this.state.exportFileDestinationValidationError}
                    id="exportFileDestinationSelectField"
                  >
                    {exportFileDestinationMenuItems}
                  </Select>
                </FormControl>
                <TextField
                  required
                  fullWidth={true}
                  style={{ marginTop: 10 }}
                  error={this.state.templateValidationError}
                  label="Template"
                  onChange={this.onTemplateChange}
                  value={this.state.template}
                  helperText={this.state.templateValidationErrorMessage}
                  id="templateTextField"
                />
                <TextField
                  fullWidth={true}
                  style={{ marginTop: 10 }}
                  error={this.state.newTemplateMetadataValidationError}
                  label="Metadata"
                  onChange={this.onNewTemplateMetadataChange}
                  value={this.state.newTemplateMetadata}
                  id="metadataTextField"
                />
              </div>
              <div style={{ marginTop: 10 }}>
                <Button
                  onClick={this.onCreateExportFileTemplate}
                  variant="contained"
                  style={{ margin: 10 }}
                  id="createExportFileTemplateInnerBtn"
                >
                  Create export file template
                </Button>
                <Button
                  onClick={this.closeExportFileTemplateDialog}
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

export default ExportFileTemplateList;
