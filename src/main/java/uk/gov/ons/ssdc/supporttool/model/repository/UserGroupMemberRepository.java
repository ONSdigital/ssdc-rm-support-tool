package uk.gov.ons.ssdc.supporttool.model.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import uk.gov.ons.ssdc.supporttool.model.entity.UserGroup;
import uk.gov.ons.ssdc.supporttool.model.entity.UserGroupMember;

@RepositoryRestResource
public interface UserGroupMemberRepository extends JpaRepository<UserGroupMember, UUID> {
}
