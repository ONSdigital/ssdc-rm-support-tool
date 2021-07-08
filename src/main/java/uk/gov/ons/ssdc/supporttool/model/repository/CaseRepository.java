package uk.gov.ons.ssdc.supporttool.model.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import uk.gov.ons.ssdc.supporttool.model.entity.Case;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RepositoryRestResource(exported = false)
public interface CaseRepository extends PagingAndSortingRepository<Case, UUID> {
  Optional<Case> findByCaseRef(@Param("caseRef") Long caseRef);

  //
  @Query(
      value =
          "SELECT ca.*" +
              " FROM casev3.collection_exercise ce, casev3.cases ca" +
              " WHERE ce.id = ca.collection_exercise_id" +
              " AND (:surveyId IS NULL OR ce.survey_id = :surveyId)" +
              " AND EXISTS " +
              " (SELECT * FROM jsonb_each_text(ca.sample) AS x(ky, val) WHERE lower(x.val) LIKE lower(:searchTerm)) " +
              " AND (:collexId IS NULL OR ce.id = :collexId)" +
              " AND (:addressInvalid IS NULL OR ca.address_invalid = :addressInvalid)" +
              " AND (:receiptReceived IS NULL OR ca.receipt_received = :receiptReceived)" +
              " AND (:surveyLaunched IS NULL OR ca.survey_launched = :surveyLaunched)" +
              " AND (:filterRefusalReceived = FALSE OR ca.refusal_received = :refusalReceived)",
      nativeQuery = true)
  List<Case> findBySampleContains(@Param("searchTerm") String searchTerm,
                                  @Param("surveyId") UUID surveyId,
                                  @Param("collexId") UUID collexId,
                                  @Param("addressInvalid") Boolean addressInvalid,
                                  @Param("receiptReceived") Boolean receiptReceived,
                                  @Param("surveyLaunched") Boolean surveyLaunched,
                                  @Param("refusalReceived") String refusalReceived,
                                  @Param("filterRefusalReceived") boolean filterRefusalReceived
  );
}
