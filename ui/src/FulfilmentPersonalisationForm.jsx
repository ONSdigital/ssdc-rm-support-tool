import React, { Component } from "react";
import { FormControl, TextField } from "@material-ui/core";

class FulfilmentPersonalisationForm extends Component {
  getTemplateRequestPersonalisationKeys = (templateKeys) => {
    return templateKeys
      .filter((templateKey) => templateKey.startsWith("__request__."))
      .map((templateKey) => templateKey.replace("__request__.", ""));
  };

  render() {
    let selectedTemplate = this.props.template;
    let requestTemplateKeys = [];
    if (selectedTemplate !== "") {
      requestTemplateKeys = this.getTemplateRequestPersonalisationKeys(
        this.props.template.template,
      );
    }

    let personalisationFormItems = requestTemplateKeys.map(
      (personalisationKey) => (
        <FormControl fullWidth={true} key={personalisationKey}>
          <TextField
            label={personalisationKey}
            id={"personalisationKey-" + personalisationKey}
            name={personalisationKey}
            onChange={this.props.onPersonalisationValueChange}
          />
        </FormControl>
      ),
    );

    return (
      <>
        {requestTemplateKeys.length > 0 && personalisationFormItems && (
          <fieldset>
            <label>Optional Request Personalisation</label>
            {personalisationFormItems}
          </fieldset>
        )}
      </>
    );
  }
}

export default FulfilmentPersonalisationForm;
