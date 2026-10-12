package com.reservas.users.service;

import com.reservas.commons.dto.ProfessorResearchLineDto;
import com.reservas.commons.entity.Professor;
import com.reservas.commons.entity.ProfessorResearchLine;
import com.reservas.commons.enums.ExperienceLevel;
import com.reservas.commons.exception.ResourceNotFoundException;
import com.reservas.commons.pagination.PageRequestFactory;
import com.reservas.users.dto.ProfessorResearchLinePageDto;
import com.reservas.users.dto.request.ProfessorResearchLineSearchRequest;
import com.reservas.users.dto.request.ProfessorResearchLineUpdateRequest;
import com.reservas.users.mapper.PageMapper;
import com.reservas.users.mapper.ProfessorResearchLineMapper;
import com.reservas.users.repository.ProfessorRepository;
import com.reservas.users.repository.ProfessorResearchLineRepository;
import com.reservas.users.specification.ProfessorResearchLineSpecifications;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Lógica de negocio de la entidad M:N {@link ProfessorResearchLine}.
 *
 * <p>La tabla puente de Entrega #1 no incluye columna {@code active}; el DELETE REST
 * elimina la asociación (desvincula profesor y línea), equivalente al borrado lógico
 * de una fila de detalle.</p>
 */
@Service
@RequiredArgsConstructor
public class ProfessorResearchLineService {

	private static final Logger log = LoggerFactory.getLogger(ProfessorResearchLineService.class);

	private final ProfessorResearchLineRepository professorResearchLineRepository;
	private final ProfessorRepository professorRepository;
	private final ProfessorResearchLineMapper professorResearchLineMapper;
	private final PageMapper pageMapper;
	private final PageRequestFactory pageRequestFactory;

	/**
	 * Obtiene una fila de detalle por identificador.
	 *
	 * @param id identificador de {@code professor_research_line}
	 * @return DTO de la asociación
	 */
	@Transactional(readOnly = true)
	public ProfessorResearchLineDto getById(UUID id) {
		log.info("Getting professor research line id={}", id);
		return professorResearchLineMapper.toDto(findOrThrow(id));
	}

	/**
	 * Búsqueda paginada de asociaciones profesor ↔ línea.
	 *
	 * @param request filtros y paginación
	 * @return página de resultados
	 */
	@Transactional(readOnly = true)
	public ProfessorResearchLinePageDto search(ProfessorResearchLineSearchRequest request) {
		log.info("Searching professor research lines professorId={} researchLineId={}",
				request.getProfessorId(), request.getResearchLineId());
		Page<ProfessorResearchLine> page = professorResearchLineRepository.search(
				ProfessorResearchLineSpecifications.from(request),
				pageRequestFactory.from(request.getPage(), request.getSize(), Sort.by("id").ascending())
		);
		return pageMapper.toProfessorResearchLinePage(page);
	}

	/**
	 * Actualiza el nivel de experiencia en una fila de detalle.
	 *
	 * @param id      identificador de la fila puente
	 * @param request nuevo nivel
	 * @return DTO actualizado
	 */
	@Transactional
	public ProfessorResearchLineDto update(UUID id, ProfessorResearchLineUpdateRequest request) {
		log.info("Updating professor research line id={}", id);
		ProfessorResearchLine entity = findOrThrow(id);
		ExperienceLevel level = ExperienceLevel.valueOf(request.getExperienceLevel().name());
		entity.setExperienceLevel(level);
		return professorResearchLineMapper.toDto(professorResearchLineRepository.save(entity));
	}

	/**
	 * Desvincula validando que la fila pertenezca al profesor indicado.
	 *
	 * @param professorId identificador del profesor cabecera
	 * @param detailId    identificador de la fila puente
	 */
	@Transactional
	public void dissociate(UUID professorId, UUID detailId) {
		log.info("Dissociating professor research line detailId={} professorId={}", detailId, professorId);
		ProfessorResearchLine detail = professorResearchLineRepository.findByIdAndProfessorId(detailId, professorId)
				.orElseThrow(() -> new ResourceNotFoundException("Professor research line not found: " + detailId));
		dissociateInternal(detail);
	}

	@Transactional
	public void dissociate(UUID id) {
		log.info("Dissociating professor research line id={}", id);
		dissociateInternal(findOrThrow(id));
	}

	private void dissociateInternal(ProfessorResearchLine detail) {
		Professor professor = professorRepository.findDetailedById(detail.getProfessor().getId())
				.orElseThrow(() -> new ResourceNotFoundException("Professor not found: " + detail.getProfessor().getId()));
		professor.removeResearchLine(detail);
		professorRepository.save(professor);
	}

	private ProfessorResearchLine findOrThrow(UUID id) {
		return professorResearchLineRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Professor research line not found: " + id));
	}
}
