package com.reservas.users.repository.jpa;

import com.reservas.security.Role;
import com.reservas.users.security.UserRoleAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface UserRoleJpaRepository extends JpaRepository<UserRoleAssignment, UUID> {

	List<UserRoleAssignment> findByUserId(UUID userId);

	boolean existsByUserIdAndRole(UUID userId, Role role);

	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("delete from UserRoleAssignment assignment where assignment.userId = :userId")
	void deleteByUserId(@Param("userId") UUID userId);
}
