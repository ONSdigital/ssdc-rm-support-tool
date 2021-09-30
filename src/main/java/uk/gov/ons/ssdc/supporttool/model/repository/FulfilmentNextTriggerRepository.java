package uk.gov.ons.ssdc.supporttool.model.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.ons.ssdc.common.model.entity.FulfilmentNextTrigger;

public interface FulfilmentNextTriggerRepository
    extends JpaRepository<FulfilmentNextTrigger, UUID> {}
