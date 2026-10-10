package com.reservas.users.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/** Alta/actualización de línea vía catálogo. */
@Getter
@Setter
public class CatalogWriteRequest {

	@NotBlank
	@Size(max = 150)
	private String name;

	@Size(max = 500)
	private String description;
}
