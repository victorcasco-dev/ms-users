package com.reservas.users.repository.jpa;

import com.reservas.commons.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

/** Spring Data JPA para {@link User}. */
public interface UserJpaRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

	boolean existsByEmailIgnoreCase(String email);

	boolean existsByEmailIgnoreCaseAndIdNot(String email, UUID id);
}
