package com.reservas.users.controller;

import com.reservas.commons.dto.UserCreateDto;
import com.reservas.commons.dto.UserDto;
import com.reservas.commons.dto.UserPageDto;
import com.reservas.commons.web.BaseController;
import com.reservas.users.dto.request.UserSearchRequest;
import com.reservas.users.dto.request.UserUpdateRequest;
import com.reservas.users.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST de usuarios ({@link com.reservas.commons.entity.User} y subtipos).
 * DELETE marca {@code active = false} (borrado lógico Parte #1).
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Users")
public class UserController extends BaseController {

	private final UserService userService;

	@PostMapping
	@Operation(summary = "Register a user (student, professor or administrator)")
	public ResponseEntity<UserDto> create(@Valid @RequestBody UserCreateDto dto) {
		log.info("Incoming POST /users type={}", dto.getType());
		UserDto created = userService.create(dto);
		return created("/users/" + created.getId(), created);
	}

	@GetMapping
	@Operation(summary = "List users with pagination (OpenAPI / gateway contract)")
	public UserPageDto list(
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer size,
			@RequestParam(required = false) String q
	) {
		log.info("Incoming GET /users page={} size={}", page, size);
		UserSearchRequest request = new UserSearchRequest();
		request.setPage(page);
		request.setSize(size);
		request.setQ(q);
		return userService.search(request);
	}

	@PostMapping("/search")
	@Operation(summary = "Search users with pagination and filters in the request body")
	public UserPageDto search(@Valid @RequestBody UserSearchRequest request) {
		log.info("Incoming POST /users/search");
		return userService.search(request);
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get user by id")
	public UserDto getById(@PathVariable UUID id) {
		log.info("Incoming GET /users/{}", id);
		return userService.getById(id);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update user")
	public UserDto update(@PathVariable UUID id, @Valid @RequestBody UserUpdateRequest request) {
		log.info("Incoming PUT /users/{}", id);
		return userService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Logical delete user (active = false)")
	public ResponseEntity<Void> delete(@PathVariable UUID id) {
		log.info("Incoming DELETE /users/{}", id);
		userService.deleteLogically(id);
		return noContent();
	}
}
