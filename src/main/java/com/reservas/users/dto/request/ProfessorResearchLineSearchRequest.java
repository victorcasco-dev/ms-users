package com.reservas.users.dto.request;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Filtros paginados para consultar asociaciones profesor ↔ línea de investigación.
 */
@Getter
@Setter
public class ProfessorResearchLineSearchRequest {

	@Min(0)
	private Integer page;

	@Min(1)
	private Integer size;

	private UUID professorId;
	private UUID researchLineId;
}
