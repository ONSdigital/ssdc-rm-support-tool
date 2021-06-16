package uk.gov.ons.ssdc.supporttool.model.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.ons.ssdc.supporttool.model.entity.Job;
import uk.gov.ons.ssdc.supporttool.model.entity.JobStatus;

public interface JobRepository extends JpaRepository<Job, UUID> {
  List<Job> findByCollectionExerciseIdOrderByCreatedAtDesc(UUID collectionExerciseId);

  List<Job> findByJobStatus(JobStatus jobStatus);
}
