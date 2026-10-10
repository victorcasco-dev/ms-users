package com.reservas.users.dto.request;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResearchLineSearchRequest {

	@Min(0)
	private Integer page;

	@Min(1)
	private Integer size;

	private String name;
	private Boolean active;
}
