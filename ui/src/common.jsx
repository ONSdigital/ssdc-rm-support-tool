export const convertStatusText = (status) => {
  if (status === "FILE_UPLOADED") {
    return "File Uploaded";
  } else if (status === "STAGING_IN_PROGRESS") {
    return "Staging in Progress";
  } else if (status === "VALIDATION_IN_PROGRESS") {
    return "Validation in Progress";
  } else if (status === "VALIDATED_OK") {
    return "Validated OK";
  } else if (status === "VALIDATED_WITH_ERRORS") {
    return "Validated With Errors";
  } else if (status === "VALIDATED_TOTAL_FAILURE") {
    return "File Not Valid";
  } else if (status === "PROCESSING_IN_PROGRESS") {
    return "Processing in Progress";
  } else if (status === "PROCESSED") {
    return "Processed";
  } else if (status === "CANCELLED") {
    return "Cancelled";
  }

  return "Unknown Status";
};
