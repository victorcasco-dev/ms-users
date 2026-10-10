package com.reservas.users.repository;

import com.reservas.commons.entity.User;
import com.reservas.users.repository.jpa.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UserRepository {

	private static final Logger log = LoggerFactory.getLogger(UserRepository.class);

	private final UserJpaRepository jpa;

	public Optional<User> findById(UUID id) {
		log.debug("Finding user by id={}", id);
		return jpa.findById(id);
	}

	public Page<User> search(Specification<User> specification, Pageable pageable) {
		log.debug("Searching users page={} size={}", pageable.getPageNumber(), pageable.getPageSize());
		return jpa.findAll(specification, pageable);
	}

	public boolean existsByEmail(String email) {
		log.debug("Checking email uniqueness email={}", email);
		return jpa.existsByEmailIgnoreCase(email);
	}

	public boolean existsByEmailAndIdNot(String email, UUID id) {
		log.debug("Checking email uniqueness email={} excluding id={}", email, id);
		return jpa.existsByEmailIgnoreCaseAndIdNot(email, id);
	}

	public User save(User user) {
		log.debug("Saving user email={}", user.getEmail());
		return jpa.save(user);
	}
}
