package uk.gov.ons.ssdc.supporttool.testhelper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;
import uk.gov.ons.ssdc.common.model.entity.*;
import uk.gov.ons.ssdc.common.validation.ColumnValidator;
import uk.gov.ons.ssdc.common.validation.MandatoryRule;
import uk.gov.ons.ssdc.common.validation.Rule;

public class ScheduleHelper {
  public static Job getJob(String columnName, boolean sampleWithHeaderRow, JobStatus jobStatus) {
    CollectionExercise collectionExercise = new CollectionExercise();
    Survey survey = new Survey();
    survey.setName("test_survey-" + UUID.randomUUID());
    survey.setSampleSeparator(',');
    survey.setSampleValidationRules(
        new ColumnValidator[] {
          new ColumnValidator(columnName, false, new Rule[] {new MandatoryRule()})
        });
    survey.setSampleWithHeaderRow(sampleWithHeaderRow);
    collectionExercise.setSurvey(survey);
    Job job = new Job();
    job.setCollectionExercise(collectionExercise);
    job.setJobStatus(jobStatus);
    job.setJobType(JobType.SAMPLE);
    job.setFileRowCount(3);
    job.setId(UUID.randomUUID());
    job.setFileId(UUID.randomUUID());
    return job;
  }

  public static void createFile(Job job, String header) throws IOException {
    File file = new File("/tmp/" + job.getFileId());
    file.createNewFile();
    file.deleteOnExit();
    FileWriter fw = new FileWriter(file.getPath(), true);
    BufferedWriter bw = new BufferedWriter(fw);
    bw.write(header);
    bw.newLine();
    bw.write("Blah");
    bw.newLine();
    bw.write("Blah2");
    bw.close();
  }
}
