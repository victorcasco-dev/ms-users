package com.reservas.users.dto;

import com.reservas.commons.dto.CatalogDto;
import com.reservas.commons.dto.PageMetadataDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/** Respuesta paginada de catálogo de líneas. */
@Getter
@Setter
public class CatalogPageDto {

	private List<CatalogDto> content;
	private PageMetadataDto page;
}
