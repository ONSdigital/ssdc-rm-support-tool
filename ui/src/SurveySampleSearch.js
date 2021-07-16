import React, { Component } from "react";
import "@fontsource/roboto";
import {
  Button,
  FormControl,
  Select,
  InputLabel,
  TextField,
  MenuItem,
  Typography,
  Box,
} from "@material-ui/core";

const SEARCH_FIELD_WIDTH = 200;
const SEARCH_BUTTON_WIDTH = 200;
const BOOL_FILTER_STYLE = { minWidth: 100, marginLeft: 20, marginRight: 20 };

class SurveySampleSearch extends Component {
  state = {
    selectedCollectionExercise: "",
    refusalTypes: [],
    selectedRefusalFilter: "",
    selectedReceiptedFilter: "",
    selectedInvalidFilter: "",
    selectedLaunchedFilter: "",
    searchTerm: "",
  };

  componentDidMount() {
    this.getRefusalTypes();
  }

    getRefusalTypes = async () => {
      const response = await fetch('/refusals/types')
        let refusalJson = await response.json()

    this.setState({ refusalTypes: refusalJson });
  };

  onSearchChange = (event) => {
    this.setState({
      searchTerm: event.target.value,
    });
  };

  onSearch = async () => {
    if (!this.props.searchTermValidator(this.state.searchTerm)) {
      this.setState({ searchTermFailedValidation: true });
      return;
    }
    this.setState({ searchTermFailedValidation: false });
    let searchUrl = `searchInSurvey/${this.props.surveyId}?searchTerm=${this.state.searchTerm}`;

    if (this.state.selectedCollectionExercise) {
      searchUrl += "&collexId=" + this.state.selectedCollectionExercise;
    }

    if (this.state.selectedRefusalFilter) {
      searchUrl += "&refusal=" + this.state.selectedRefusalFilter;
    }

    if (this.state.selectedReceiptedFilter) {
      searchUrl += "&receipted=" + this.state.selectedReceiptedFilter;
    }

    if (this.state.selectedInvalidFilter) {
      searchUrl += "&invalid=" + this.state.selectedInvalidFilter;
    }

    if (this.state.selectedLaunchedFilter) {
      searchUrl += "&launched=" + this.state.selectedLaunchedFilter;
    }

    this.props.onSearchExecuteAndPopulateList(
      searchUrl,
      this.state.searchTerm,
      "sample data containing"
    );
  };

  onFilterCollectionExercise = (event) => {
    this.setState({
      selectedCollectionExercise: event.target.value,
    });
  };

  onFilterRefusal = (event) => {
    this.setState({
      selectedRefusalFilter: event.target.value,
    });
  };
  onFilterReceipted = (event) => {
    this.setState({
      selectedReceiptedFilter: event.target.value,
    });
  };

  onFilterInvalid = (event) => {
    this.setState({
      selectedInvalidFilter: event.target.value,
    });
  };

  onFilterLaunched = (event) => {
    this.setState({
      selectedLaunchedFilter: event.target.value,
    });
  };

  render() {
    const noFilterMenuItem = (
      <MenuItem key={"NO FILTER"} value={""}>
        {"NO FILTER"}
      </MenuItem>
    );
    let collectionExerciseMenuItems = [];
    collectionExerciseMenuItems.push(noFilterMenuItem);
    collectionExerciseMenuItems.push(
      this.props.collectionExercises.map((collex) => (
        <MenuItem
          key={collex.name}
          value={collex._links.self.href.split("/")[4]}
        >
          {collex.name}
        </MenuItem>
      ))
    );

    const refusalMenuItems = [];
    refusalMenuItems.push(noFilterMenuItem);
    refusalMenuItems.push(
      this.state.refusalTypes.map((refusalType) => (
        <MenuItem key={refusalType} value={refusalType}>
          {refusalType}
        </MenuItem>
      ))
    );

    const trueOrFalseFilterMenuItems = [];
    trueOrFalseFilterMenuItems.push(noFilterMenuItem);
    trueOrFalseFilterMenuItems.push([
      <MenuItem key={"true"} value={"true"}>
        {"TRUE"}
      </MenuItem>,
      <MenuItem key={"false"} value={"false"}>
        {"FALSE"}
      </MenuItem>,
    ]);

    return (
      <div id="sampleSearchDiv">
        <div id="searchTxtDiv" style={{ margin: 10 }}>
          <TextField
            required
            style={{ minWidth: SEARCH_FIELD_WIDTH }}
            error={this.state.searchTermFailedValidation}
            label="Search All Sample Data"
            onChange={this.onSearchChange}
            value={this.state.searchTerm}
          />
          <Button
            onClick={this.onSearch}
            variant="contained"
            style={{ margin: 10, minWidth: SEARCH_BUTTON_WIDTH }}
          >
            Search Sample Data
          </Button>
        </div>
        <div id="searchSampleFiltersDiv">
          <Box marginTop={2} marginLeft={1}>
            <Typography display={"inline"} variant="subtitle2">
              Optional search filters:
            </Typography>
          </Box>
          <FormControl
            style={{
              minWidth: 200,
              marginLeft: 10,
              marginRight: 20,
              padding: 0,
            }}
          >
            <InputLabel>Collection Exercise</InputLabel>
            <Select
              onChange={this.onFilterCollectionExercise}
              value={this.state.selectedCollectionExercise}
            >
              value={this.state.selectedCollectionExercise}>
              {collectionExerciseMenuItems}
            </Select>
          </FormControl>

          <FormControl
            style={{ minWidth: 200, marginLeft: 20, marginRight: 20 }}
          >
            <InputLabel>Refused</InputLabel>
            <Select
              onChange={this.onFilterRefusal}
              value={this.state.selectedRefusalFilter}
            >
              {refusalMenuItems}
            </Select>
          </FormControl>

          <FormControl style={BOOL_FILTER_STYLE}>
            <InputLabel>Receipted</InputLabel>
            <Select
              onChange={this.onFilterReceipted}
              value={this.state.selectedReceiptedFilter}
            >
              {trueOrFalseFilterMenuItems}
            </Select>
          </FormControl>

          <FormControl style={BOOL_FILTER_STYLE}>
            <InputLabel>Invalid</InputLabel>
            <Select
              onChange={this.onFilterInvalid}
              value={this.state.selectedInvalidFilter}
            >
              {trueOrFalseFilterMenuItems}
            </Select>
          </FormControl>

          <FormControl style={BOOL_FILTER_STYLE}>
            <InputLabel>Launched</InputLabel>
            <Select
              onChange={this.onFilterLaunched}
              value={this.state.selectedLaunchedFilter}
            >
              {trueOrFalseFilterMenuItems}
            </Select>
          </FormControl>
        </div>
      </div>
    );
  }
}

export default SurveySampleSearch;
