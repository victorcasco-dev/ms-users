package com.reservas.users.service;

import com.reservas.commons.dto.ProfessorResearchLineCreateDto;
import com.reservas.commons.dto.ProfessorResearchLineDto;
import com.reservas.commons.dto.UserDto;
import com.reservas.commons.dto.UserPageDto;
import com.reservas.commons.entity.Professor;
import com.reservas.commons.entity.ProfessorResearchLine;
import com.reservas.commons.entity.ResearchLine;
import com.reservas.commons.enums.ExperienceLevel;
import com.reservas.commons.exception.BadRequestException;
import com.reservas.commons.exception.ConflictException;
import com.reservas.commons.exception.InvalidOperationException;
import com.reservas.commons.exception.ResourceNotFoundException;
import com.reservas.commons.pagination.PageRequestFactory;
import com.reservas.users.dto.request.ProfessorSearchRequest;
import com.reservas.users.dto.request.ProfessorUpdateRequest;
import com.reservas.users.mapper.PageMapper;
import com.reservas.users.mapper.ProfessorResearchLineMapper;
import com.reservas.users.mapper.UserMapper;
import com.reservas.users.repository.ProfessorRepository;
import com.reservas.users.repository.ProfessorResearchLineRepository;
import com.reservas.users.repository.ResearchLineRepository;
import com.reservas.users.specification.ProfessorSpecifications;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Consulta y actualización de {@link Professor}, vínculo M:N vía {@link ProfessorResearchLineService}
 * y endpoints anidados bajo {@code /professors}.
 */
@Service
@RequiredArgsConstructor
public class ProfessorService {

	private static final Logger log = LoggerFactory.getLogger(ProfessorService.class);

	private final ProfessorRepository professorRepository;
	private final ProfessorResearchLineRepository professorResearchLineRepository;
	private final ResearchLineRepository researchLineRepository;
	private final UserMapper userMapper;
	private final ProfessorResearchLineMapper professorResearchLineMapper;
	private final PageMapper pageMapper;
	private final PageRequestFactory pageRequestFactory;
	private final ProfessorResearchLineService professorResearchLineService;

	@Transactional(readOnly = true)
	public UserDto getById(UUID id) {
		log.info("Getting professor id={}", id);
		return userMapper.toDto(findOrThrow(id));
	}

	@Transactional(readOnly = true)
	public UserPageDto search(ProfessorSearchRequest request) {
		log.info("Searching professors name={} researchLineId={}", request.getName(), request.getResearchLineId());
		Page<Professor> page = professorRepository.search(
				ProfessorSpecifications.from(request),
				pageRequestFactory.from(
						request.getPage(),
						request.getSize(),
						Sort.by("lastName").ascending().and(Sort.by("firstName").ascending())
				)
		);
		return pageMapper.toUserPage(page);
	}

	@Transactional
	public UserDto update(UUID id, ProfessorUpdateRequest request) {
		log.info("Updating professor id={}", id);
		Professor professor = findOrThrow(id);
		if (professorRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
			throw new ConflictException("Email already registered: " + request.getEmail());
		}
		professor.setEmail(request.getEmail());
		professor.setFirstName(request.getFirstName());
		professor.setLastName(request.getLastName());
		professor.setAcademicTitle(request.getAcademicTitle());
		return userMapper.toDto(professorRepository.save(professor));
	}

	@Transactional(readOnly = true)
	public List<ProfessorResearchLineDto> listResearchLines(UUID professorId) {
		log.info("Listing research lines for professor id={}", professorId);
		findOrThrow(professorId);
		return professorResearchLineRepository.findByProfessorId(professorId).stream()
				.map(professorResearchLineMapper::toDto)
				.toList();
	}

	@Transactional
	public ProfessorResearchLineDto addResearchLine(UUID professorId, ProfessorResearchLineCreateDto request) {
		log.info("Adding research line {} to professor {}", request.getResearchLineId(), professorId);
		if (request.getResearchLineId() == null) {
			throw new BadRequestException("researchLineId is required");
		}
		Professor professor = professorRepository.findDetailedById(professorId)
				.orElseThrow(() -> new ResourceNotFoundException("Professor not found: " + professorId));
		ResearchLine researchLine = researchLineRepository.findById(request.getResearchLineId())
				.orElseThrow(() -> new ResourceNotFoundException("Research line not found: " + request.getResearchLineId()));
		if (!researchLine.isActive()) {
			throw new InvalidOperationException("Research line is inactive: " + researchLine.getName());
		}
		if (professorResearchLineRepository.existsByProfessorAndResearchLine(professorId, researchLine.getId())) {
			throw new ConflictException("Professor already has this research line");
		}

		ExperienceLevel level = request.getExperienceLevel() == null
				? ExperienceLevel.BASIC
				: ExperienceLevel.valueOf(request.getExperienceLevel().name());
		professor.addResearchLine(researchLine, level);
		Professor saved = professorRepository.save(professor);
		ProfessorResearchLine created = saved.getResearchLines().stream()
				.filter(detail -> researchLine.getId().equals(detail.getResearchLine().getId()))
				.findFirst()
				.orElseThrow(() -> new IllegalStateException("Research line was not attached to professor"));
		return professorResearchLineMapper.toDto(created);
	}

	@Transactional
	public void removeResearchLine(UUID professorId, UUID detailId) {
		log.info("Removing research line detail {} from professor {}", detailId, professorId);
		professorResearchLineService.dissociate(professorId, detailId);
	}

	private Professor findOrThrow(UUID id) {
		return professorRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Professor not found: " + id));
	}
}
