package com.reservas.users.repository;

import com.reservas.commons.entity.ProfessorResearchLine;
import com.reservas.users.repository.jpa.ProfessorResearchLineJpaRepository;
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

/**
 * Acceso a datos de la entidad puente {@link ProfessorResearchLine}.
 */
@Repository
@RequiredArgsConstructor
public class ProfessorResearchLineRepository {

	private static final Logger log = LoggerFactory.getLogger(ProfessorResearchLineRepository.class);

	private final ProfessorResearchLineJpaRepository jpa;

	public List<ProfessorResearchLine> findByProfessorId(UUID professorId) {
		log.debug("Listing research lines for professorId={}", professorId);
		return jpa.findByProfessor_Id(professorId);
	}

	public boolean existsByProfessorAndResearchLine(UUID professorId, UUID researchLineId) {
		log.debug("Checking research line assignment professorId={} researchLineId={}", professorId, researchLineId);
		return jpa.existsByProfessor_IdAndResearchLine_Id(professorId, researchLineId);
	}

	public Optional<ProfessorResearchLine> findByIdAndProfessorId(UUID id, UUID professorId) {
		log.debug("Finding research line detail id={} professorId={}", id, professorId);
		return jpa.findByIdAndProfessor_Id(id, professorId);
	}

	public Optional<ProfessorResearchLine> findById(UUID id) {
		log.debug("Finding professor research line id={}", id);
		return jpa.findById(id);
	}

	public Page<ProfessorResearchLine> search(Specification<ProfessorResearchLine> specification, Pageable pageable) {
		log.debug("Searching professor research lines page={} size={}", pageable.getPageNumber(), pageable.getPageSize());
		return jpa.findAll(specification, pageable);
	}

	public ProfessorResearchLine save(ProfessorResearchLine entity) {
		log.debug("Saving professor research line id={}", entity.getId());
		return jpa.save(entity);
	}

	public void delete(ProfessorResearchLine entity) {
		log.debug("Deleting professor research line id={}", entity.getId());
		jpa.delete(entity);
	}
}
