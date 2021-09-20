package uk.gov.ons.ssdc.supporttool.model.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.gov.ons.ssdc.common.model.entity.Job;
import uk.gov.ons.ssdc.common.model.entity.JobRow;
import uk.gov.ons.ssdc.common.model.entity.JobRowStatus;

public interface JobRowRepository extends JpaRepository<JobRow, UUID> {
  int countByJobAndAndJobRowStatus(Job job, JobRowStatus jobRowStatus);

  boolean existsByJobAndAndJobRowStatus(Job job, JobRowStatus jobRowStatus);

  List<JobRow> findByJobAndAndJobRowStatusOrderByOriginalRowLineNumber(
      Job job, JobRowStatus jobRowStatus);

  // This is required because otherwise Hibernate will attempt to read ALL the JobRows, which
  // could number in the millions, causing an out of memory crash
  @Modifying
  @Query("delete from JobRow r where r.job.id = :jobId and r.jobRowStatus = :rowStatus")
  void deleteByJobIdAndAndJobRowStatus(
      @Param("jobId") UUID jobId, @Param("rowStatus") JobRowStatus rowStatus);

  // This is required because otherwise Hibernate will attempt to read ALL the JobRows, which
  // could number in the millions, causing an out of memory crash
  @Modifying
  @Query("delete from JobRow r where r.job.id = :jobId")
  void deleteByJobId(@Param("jobId") UUID jobId);

  List<JobRow> findTop500ByJobAndAndJobRowStatus(Job job, JobRowStatus jobRowStatus);
}
