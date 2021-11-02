package uk.gov.ons.ssdc.supporttool.model.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.ons.ssdc.common.model.entity.CollectionExercise;
import uk.gov.ons.ssdc.common.model.entity.Job;
import uk.gov.ons.ssdc.common.model.entity.JobStatus;
import uk.gov.ons.ssdc.common.model.entity.JobType;

public interface JobRepository extends JpaRepository<Job, UUID> {
  List<Job> findByCollectionExerciseAndJobTypeOrderByCreatedAtDesc(
      CollectionExercise collectionExercise, JobType jobType);

  List<Job> findByJobStatus(JobStatus jobStatus);
}
