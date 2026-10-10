package com.reservas.users.repository;

import com.reservas.commons.entity.Professor;
import com.reservas.users.repository.jpa.ProfessorJpaRepository;
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
public class ProfessorRepository {

	private static final Logger log = LoggerFactory.getLogger(ProfessorRepository.class);

	private final ProfessorJpaRepository jpa;

	public Optional<Professor> findById(UUID id) {
		log.debug("Finding professor by id={}", id);
		return jpa.findById(id);
	}

	public Optional<Professor> findDetailedById(UUID id) {
		log.debug("Finding professor with research lines id={}", id);
		return jpa.findDetailedById(id);
	}

	public Page<Professor> search(Specification<Professor> specification, Pageable pageable) {
		log.debug("Searching professors page={} size={}", pageable.getPageNumber(), pageable.getPageSize());
		return jpa.findAll(specification, pageable);
	}

	public boolean existsByEmailAndIdNot(String email, UUID id) {
		log.debug("Checking professor email uniqueness email={} excluding id={}", email, id);
		return jpa.existsByEmailIgnoreCaseAndIdNot(email, id);
	}

	public Professor save(Professor professor) {
		log.debug("Saving professor id={} email={}", professor.getId(), professor.getEmail());
		return jpa.save(professor);
	}
}
