import React, { Component } from 'react';
import '@fontsource/roboto';
import { Button, TextField } from '@material-ui/core';


class SurveySimpleSearchInput extends Component {
    state = {
        filterValue: '',
        failedValidation: false
    }

    componentDidMount() {
    }

    onChange = (event) => {
        this.setState({
            filterValue: event.target.value
        })
    }

    onSearch = async () => {
        if (!this.props.isNumeric(this.state.filterValue)) {
            this.setState({ failedValidation: true })
            return
        }
        this.setState({ failedValidation: false })
        this.props.onSearchExecuteAndPopulateList('searchInSurvey/' + this.props.surveyId + '/' + this.props.urlpathName + '/' + this.state.filterValue)
    }

    render() {
        return (
            <div style={{ margin: 10 }}>
                <TextField
                    required
                    style={{ minWidth: 200 }}
                    error={this.state.failedValidation}
                    label={this.props.displayText}
                    onChange={this.onChange}
                    value={this.state.filterValue} />

                <Button onClick={this.onSearch} variant="contained"
                    style={{ margin: 10, minWidth: 200 }}>
                    {this.props.displayText}
                </Button>
            </div>
        )
    }
}

export default SurveySimpleSearchInput