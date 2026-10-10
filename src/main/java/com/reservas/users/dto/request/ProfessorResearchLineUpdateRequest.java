package com.reservas.users.dto.request;

import com.reservas.commons.dto.ExperienceLevelDto;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Actualización de una fila de detalle M:N (nivel de experiencia en la línea).
 */
@Getter
@Setter
public class ProfessorResearchLineUpdateRequest {

	@NotNull
	private ExperienceLevelDto experienceLevel;
}
