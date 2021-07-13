package uk.gov.ons.ssdc.supporttool.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.ons.ssdc.supporttool.model.entity.Case;
import uk.gov.ons.ssdc.supporttool.model.entity.Event;

@ActiveProfiles("test")
public interface EventRepository extends JpaRepository<Event, Case> {}
