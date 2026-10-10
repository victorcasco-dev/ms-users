package com.reservas.users.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/** Cuerpo de {@code PUT /professors/{id}}. */
@Getter
@Setter
public class ProfessorUpdateRequest {

	@Email
	@NotBlank
	private String email;

	@NotBlank
	private String firstName;

	@NotBlank
	private String lastName;

	private String academicTitle;
}
