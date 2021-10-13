package uk.gov.ons.ssdc.supporttool.model.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.ons.ssdc.common.model.entity.UserGroup;

public interface UserGroupRepository extends JpaRepository<UserGroup, UUID> {

  boolean existsByName(String name);
}
