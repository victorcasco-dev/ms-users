package com.reservas.users.controller;

import com.reservas.commons.web.BaseController;
import com.reservas.users.dto.CurrentUserResponse;
import com.reservas.users.dto.TokenResponse;
import com.reservas.users.dto.request.LoginRequest;
import com.reservas.users.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication")
public class AuthController extends BaseController {

	private final AuthService authService;

	@PostMapping("/login")
	@SecurityRequirements
	@Operation(summary = "Authenticate with email and password and receive a JWT")
	public TokenResponse login(@Valid @RequestBody LoginRequest request) {
		log.info("Incoming POST /auth/login");
		return authService.login(request);
	}

	@GetMapping("/me")
	@PreAuthorize("isAuthenticated()")
	@Operation(summary = "Current user from the access token")
	public CurrentUserResponse me() {
		log.info("Incoming GET /auth/me");
		return authService.currentUser();
	}
}
