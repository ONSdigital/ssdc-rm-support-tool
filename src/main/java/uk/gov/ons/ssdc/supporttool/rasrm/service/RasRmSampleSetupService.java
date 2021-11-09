package uk.gov.ons.ssdc.supporttool.rasrm.service;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Component;
import uk.gov.ons.ssdc.common.model.entity.CollectionExercise;
import uk.gov.ons.ssdc.supporttool.model.repository.CollectionExerciseRepository;
import uk.gov.ons.ssdc.supporttool.rasrm.client.RasRmSampleServiceClient;

@Component
public class RasRmSampleSetupService {
  private static final Set<String> MANDATORY_RAS_RM_BUSINESS_COLLEX_METADATA =
      Set.of("rasRmCollectionExerciseId", "rasRmCollectionInstrumentId");

  private final RasRmSampleServiceClient rasRmSampleServiceClient;
  private final CollectionExerciseRepository collectionExerciseRepository;

  public RasRmSampleSetupService(
      RasRmSampleServiceClient rasRmSampleServiceClient,
      CollectionExerciseRepository collectionExerciseRepository) {
    this.rasRmSampleServiceClient = rasRmSampleServiceClient;
    this.collectionExerciseRepository = collectionExerciseRepository;
  }

  public void setupSampleSummary(CollectionExercise collectionExercise, int sampleCaseCount) {
    Object metadataObject = collectionExercise.getMetadata();

    if (metadataObject == null) {
      throw new RuntimeException(
          "Unexpected null metadata. Metadata is required for RAS-RM business.");
    }

    if (!(metadataObject instanceof Map)) {
      throw new RuntimeException(
          "Unexpected metadata type. Wanted Map but got "
              + metadataObject.getClass().getSimpleName());
    }

    Map metadata = (Map) metadataObject;

    if (!metadata.keySet().containsAll(MANDATORY_RAS_RM_BUSINESS_COLLEX_METADATA)) {
      throw new RuntimeException("Metadata does not contain mandatory values");
    }

    // TODO: has been hard-coded to 1 collection instrument, for now
    UUID rasRmSampleSummaryId =
        rasRmSampleServiceClient.createSampleSummary(sampleCaseCount, 1).getId();

    // Now store the rasRmSampleSummaryId into the metadata for later use
    metadata.put("rasRmSampleSummaryId", rasRmSampleSummaryId.toString());

    collectionExercise.setMetadata(metadata);
    collectionExerciseRepository.saveAndFlush(collectionExercise);
  }
}
