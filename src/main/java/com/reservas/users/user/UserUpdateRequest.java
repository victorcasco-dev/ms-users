package com.reservas.users.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

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
}
