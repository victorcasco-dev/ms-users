package com.reservas.users.repository;

import com.reservas.commons.entity.ResearchLine;
import com.reservas.users.repository.jpa.ResearchLineJpaRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ResearchLineRepository {

	private static final Logger log = LoggerFactory.getLogger(ResearchLineRepository.class);

	private final ResearchLineJpaRepository jpa;

	public Optional<ResearchLine> findById(UUID id) {
		log.debug("Finding research line by id={}", id);
		return jpa.findById(id);
	}

	public Page<ResearchLine> search(Specification<ResearchLine> specification, Pageable pageable) {
		log.debug("Searching research lines page={} size={}", pageable.getPageNumber(), pageable.getPageSize());
		return jpa.findAll(specification, pageable);
	}

	public boolean existsByName(String name) {
		log.debug("Checking research line name uniqueness name={}", name);
		return jpa.existsByNameIgnoreCase(name);
	}

	public boolean existsByNameAndIdNot(String name, UUID id) {
		log.debug("Checking research line name uniqueness name={} excluding id={}", name, id);
		return jpa.existsByNameIgnoreCaseAndIdNot(name, id);
	}

	public ResearchLine save(ResearchLine researchLine) {
		log.debug("Saving research line name={}", researchLine.getName());
		return jpa.save(researchLine);
	}

	public List<ResearchLine> findAllActive() {
		log.debug("Listing all active research lines");
		return jpa.findAllByActiveTrueOrderByNameAsc();
	}
}
