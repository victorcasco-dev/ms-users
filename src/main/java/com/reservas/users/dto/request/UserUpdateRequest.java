package com.reservas.users.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import com.reservas.commons.enums.UserType;

import java.util.List;
import java.util.UUID;

/** Cuerpo de {@code PUT /users/{id}}. */
@Getter
@Setter
public class UserUpdateRequest {

	@Email
	@NotBlank
	private String email;

	@NotBlank
	private String firstName;

	@NotBlank
	private String lastName;

	private String password;
	private String studentCode;
	private String academicTitle;
	private String position;
	private UUID tutorProfessorId;

	/** Nuevo tipo de usuario. Nulo = se conserva el actual. */
	private UserType type;

	/** Roles de acceso. Si viene vacío o nulo se conservan los actuales. */
	private List<String> roles;
}
