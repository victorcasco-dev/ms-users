package com.reservas.users.service;

import com.reservas.commons.entity.User;
import com.reservas.commons.exception.UnauthorizedException;
import com.reservas.security.AuthenticatedUser;
import com.reservas.security.JwtTokenProvider;
import com.reservas.security.Role;
import com.reservas.users.dto.CurrentUserResponse;
import com.reservas.users.dto.TokenResponse;
import com.reservas.users.dto.request.LoginRequest;
import com.reservas.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/** Login y perfil del token. Las contraseñas se comparan con BCrypt y no se registran. */
@Service
@RequiredArgsConstructor
public class AuthService {

	private static final Logger log = LoggerFactory.getLogger(AuthService.class);
	private static final String INVALID_CREDENTIALS = "Invalid email or password";

	private final UserRepository userRepository;
	private final RoleService roleService;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider tokenProvider;

	@Transactional
	public TokenResponse login(LoginRequest request) {
		String email = request.getEmail().trim();
		log.info("Login attempt email={}", email);
		User user = userRepository.findByEmailIgnoreCase(email).orElse(null);
		if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
			throw new UnauthorizedException(INVALID_CREDENTIALS);
		}
		if (!user.isActive()) {
			throw new UnauthorizedException("User account is disabled");
		}
		Set<Role> roles = roleService.loadOrSeed(user.getId(), Role.valueOf(user.getType().name()));
		String token = tokenProvider.generate(user.getId(), user.getEmail(), roles);
		TokenResponse response = new TokenResponse();
		response.setAccessToken(token);
		response.setTokenType(tokenProvider.tokenType());
		response.setExpiresIn(tokenProvider.expiresInSeconds());
		response.setUserId(user.getId());
		response.setEmail(user.getEmail());
		response.setRoles(roles.stream().map(Role::name).sorted().toList());
		return response;
	}

	public CurrentUserResponse currentUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser principal)) {
			throw new UnauthorizedException("Authentication is required");
		}
		CurrentUserResponse response = new CurrentUserResponse();
		response.setUserId(principal.id());
		response.setEmail(principal.email());
		response.setRoles(List.copyOf(principal.roles()));
		return response;
	}
}
