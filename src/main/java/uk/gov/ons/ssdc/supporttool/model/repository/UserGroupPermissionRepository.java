package uk.gov.ons.ssdc.supporttool.model.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.ons.ssdc.common.model.entity.UserGroup;
import uk.gov.ons.ssdc.common.model.entity.UserGroupPermission;

public interface UserGroupPermissionRepository extends JpaRepository<UserGroupPermission, UUID> {
  List<UserGroupPermission> findByGroup(UserGroup group);
}
