package com.reservas.users.dto;

import com.reservas.commons.dto.PageMetadataDto;
import com.reservas.commons.dto.ProfessorResearchLineDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Respuesta paginada de filas de la tabla puente profesor–línea de investigación.
 */
@Getter
@Setter
public class ProfessorResearchLinePageDto {

	private List<ProfessorResearchLineDto> content;
	private PageMetadataDto page;
}
