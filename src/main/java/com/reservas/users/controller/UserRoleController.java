package com.reservas.users.controller;

import com.reservas.commons.web.BaseController;
import com.reservas.users.dto.request.RoleAssignmentRequest;
import com.reservas.users.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users/{userId}/roles")
@RequiredArgsConstructor
@Tag(name = "User roles")
public class UserRoleController extends BaseController {

	private final RoleService roleService;

	@GetMapping
	@PreAuthorize("hasAuthority('ADMINISTRATOR')")
	@Operation(summary = "List roles assigned to a user")
	public List<String> get(@PathVariable UUID userId) {
		log.info("Incoming GET /users/{}/roles", userId);
		return roleService.getRoleNames(userId);
	}

	@PutMapping
	@PreAuthorize("hasAuthority('ADMINISTRATOR')")
	@Operation(summary = "Replace the roles assigned to a user")
	public List<String> replace(@PathVariable UUID userId, @Valid @RequestBody RoleAssignmentRequest request) {
		log.info("Incoming PUT /users/{}/roles", userId);
		return roleService.replace(userId, request.getRoles());
	}
}
