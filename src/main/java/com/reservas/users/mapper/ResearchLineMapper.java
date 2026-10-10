package com.reservas.users.mapper;

import com.reservas.commons.dto.CatalogDto;
import com.reservas.commons.entity.ResearchLine;
import com.reservas.users.dto.request.CatalogWriteRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface ResearchLineMapper {

	CatalogDto toDto(ResearchLine entity);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "active", constant = "true")
	ResearchLine toEntity(CatalogWriteRequest request);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "active", ignore = true)
	void update(CatalogWriteRequest request, @MappingTarget ResearchLine entity);
}
