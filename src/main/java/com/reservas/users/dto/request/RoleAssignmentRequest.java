package com.reservas.users.dto.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/** Reemplaza el conjunto de roles de un usuario. */
public class RoleAssignmentRequest {

	@NotEmpty
	private List<String> roles;

	public List<String> getRoles() {
		return roles;
	}

	public void setRoles(List<String> roles) {
		this.roles = roles;
	}
}
