package com.reservas.users.professor;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

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
