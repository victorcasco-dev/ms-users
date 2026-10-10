package com.reservas.users.dto.request;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/** Filtros de {@code POST /professors/search}. */
@Getter
@Setter
public class ProfessorSearchRequest {

	@Min(0)
	private Integer page;

	@Min(1)
	private Integer size;

	private String name;
	private UUID researchLineId;
	private Boolean active;
}
