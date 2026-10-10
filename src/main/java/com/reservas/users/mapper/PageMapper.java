package com.reservas.users.mapper;

import com.reservas.commons.dto.UserPageDto;
import com.reservas.commons.dto.PageMetadataDto;
import com.reservas.commons.entity.ProfessorResearchLine;
import com.reservas.commons.entity.ResearchLine;
import com.reservas.commons.entity.User;
import com.reservas.users.dto.CatalogPageDto;
import com.reservas.users.dto.ProfessorResearchLinePageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

/**
 * Convierte páginas Spring Data en DTOs paginados del API.
 */
@Component
@RequiredArgsConstructor
public class PageMapper {

	private final UserMapper userMapper;
	private final ResearchLineMapper researchLineMapper;
	private final ProfessorResearchLineMapper professorResearchLineMapper;

	public PageMetadataDto toMetadata(Page<?> page) {
		PageMetadataDto metadata = new PageMetadataDto();
		metadata.setPage(page.getNumber());
		metadata.setSize(page.getSize());
		metadata.setTotalElements(page.getTotalElements());
		metadata.setTotalPages(page.getTotalPages());
		return metadata;
	}

	public UserPageDto toUserPage(Page<? extends User> page) {
		UserPageDto dto = new UserPageDto();
		dto.setContent(page.getContent().stream().map(userMapper::toDto).toList());
		dto.setPage(toMetadata(page));
		return dto;
	}

	public CatalogPageDto toCatalogPage(Page<ResearchLine> page) {
		CatalogPageDto dto = new CatalogPageDto();
		dto.setContent(page.getContent().stream().map(researchLineMapper::toDto).toList());
		dto.setPage(toMetadata(page));
		return dto;
	}

	public ProfessorResearchLinePageDto toProfessorResearchLinePage(Page<ProfessorResearchLine> page) {
		ProfessorResearchLinePageDto dto = new ProfessorResearchLinePageDto();
		dto.setContent(page.getContent().stream().map(professorResearchLineMapper::toDto).toList());
		dto.setPage(toMetadata(page));
		return dto;
	}
}
