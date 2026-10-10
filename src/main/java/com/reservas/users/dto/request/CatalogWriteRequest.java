package com.reservas.users.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CatalogWriteRequest {

	@NotBlank
	@Size(max = 150)
	private String name;

	@Size(max = 500)
	private String description;
}
