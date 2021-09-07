package uk.gov.ons.ssdc.supporttool.model.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.ons.ssdc.common.model.entity.Job;
import uk.gov.ons.ssdc.common.model.entity.JobRow;
import uk.gov.ons.ssdc.common.model.entity.JobRowStatus;

public interface JobRowRepository extends JpaRepository<JobRow, UUID> {
  int countByJobAndAndJobRowStatus(Job job, JobRowStatus jobRowStatus);

  boolean existsByJobAndAndJobRowStatus(Job job, JobRowStatus jobRowStatus);

  List<JobRow> findByJobAndAndJobRowStatusOrderByOriginalRowLineNumber(
      Job job, JobRowStatus jobRowStatus);

  List<JobRow> findTop500ByJobAndAndJobRowStatus(Job job, JobRowStatus jobRowStatus);
}
