import React, { Component } from "react";
import "@fontsource/roboto";
import { Button, TextField } from "@material-ui/core";

class SurveySimpleSearchInput extends Component {
  state = {
    searchTerm: "",
    failedValidation: false,
  };

  componentDidMount() {}

  onChange = (event) => {
    this.setState({
      searchTerm: event.target.value,
    });
  };

  onSearch = async () => {
    if (!this.props.searchTermValidator(this.state.searchTerm)) {
      this.setState({ failedValidation: true });
      return;
    }
    this.setState({ failedValidation: false });
    this.props.onSearchExecuteAndPopulateList(
      `/api/surveyCases/${this.props.surveyId}/${this.props.urlpathName}/${this.state.searchTerm}`,
      this.state.searchTerm,
      this.props.searchDesc,
    );
  };

  render() {
    return (
      <div style={{ margin: 10 }}>
        <TextField
          required
          style={{ minWidth: 200 }}
          error={this.state.failedValidation}
          label={this.props.displayText}
          onChange={this.onChange}
          value={this.state.searchTerm}
        />

        <Button
          onClick={this.onSearch}
          variant="contained"
          style={{ margin: 10, minWidth: 200 }}
        >
          {this.props.displayText}
        </Button>
      </div>
    );
  }
}

export default SurveySimpleSearchInput;
