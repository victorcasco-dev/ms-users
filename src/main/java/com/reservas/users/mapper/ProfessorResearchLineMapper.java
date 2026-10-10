package com.reservas.users.mapper;

import com.reservas.commons.dto.ProfessorResearchLineDto;
import com.reservas.commons.entity.ProfessorResearchLine;
import com.reservas.users.mapper.ResearchLineMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/** MapStruct: filas M:N {@link ProfessorResearchLine} ↔ DTO. */
@Mapper(
		componentModel = "spring",
		uses = ResearchLineMapper.class,
		unmappedTargetPolicy = ReportingPolicy.IGNORE,
		unmappedSourcePolicy = ReportingPolicy.IGNORE
)
public interface ProfessorResearchLineMapper {

	@Mapping(target = "professorId", source = "professor.id")
	ProfessorResearchLineDto toDto(ProfessorResearchLine entity);
}
