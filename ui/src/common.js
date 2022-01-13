export const convertStatusText = (status) => {
  if (status === "FILE_UPLOADED") {
    return "File Uploaded";
  }
  if (status === "STAGING_IN_PROGRESS") {
    return "Staging in Progress";
  }
  if (status === "VALIDATION_IN_PROGRESS") {
    return "Validation in Progress";
  }
  if (status === "VALIDATED_OK") {
    return "Validated OK";
  }
  if (status === "VALIDATED_WITH_ERRORS") {
    return "Validated With Errors";
  }
  if (status === "VALIDATED_TOTAL_FAILURE") {
    return "File Not Valid";
  }
  if (status === "PROCESSING_IN_PROGRESS") {
    return "Processing in Progress";
  }
  if (status === "PROCESSED") {
    return "Processed";
  }
  if (status === "CANCELLED") {
    return "Cancelled";
  }

  return "Unknown Status";
};
