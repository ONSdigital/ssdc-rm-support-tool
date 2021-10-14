package uk.gov.ons.ssdc.supporttool.model.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.ons.ssdc.common.model.entity.User;
import uk.gov.ons.ssdc.common.model.entity.UserGroup;
import uk.gov.ons.ssdc.common.model.entity.UserGroupMember;

public interface UserGroupMemberRepository extends JpaRepository<UserGroupMember, UUID> {
  List<UserGroupMember> findByUser(User user);

  List<UserGroupMember> findByGroup(UserGroup group);
}
