package com.reservas.users.service;

import com.reservas.commons.exception.BadRequestException;
import com.reservas.commons.exception.ResourceNotFoundException;
import com.reservas.security.Role;
import com.reservas.users.repository.jpa.UserRoleJpaRepository;
import com.reservas.users.repository.UserRepository;
import com.reservas.users.security.UserRoleAssignment;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Asignación de roles. Un usuario puede tener más de uno. */
@Service
@RequiredArgsConstructor
public class RoleService {

	private static final Logger log = LoggerFactory.getLogger(RoleService.class);

	private final UserRoleJpaRepository roleRepository;
	private final UserRepository userRepository;

	@Transactional(readOnly = true)
	@Cacheable(cacheNames = "userRoles", key = "@cacheKeys.of('user-roles', #userId)")
	public List<String> getRoleNames(UUID userId) {
		requireUser(userId);
		return names(loadStored(userId));
	}

	@Transactional
	@CachePut(cacheNames = "userRoles", key = "@cacheKeys.of('user-roles', #userId)")
	public List<String> replace(UUID userId, List<String> requested) {
		requireUser(userId);
		Set<Role> roles = parse(requested);
		log.info("Replacing roles userId={} roles={}", userId, roles);
		roleRepository.deleteByUserId(userId);
		for (Role role : roles) {
			roleRepository.save(assignment(userId, role));
		}
		return names(roles);
	}

	@Transactional
	@CacheEvict(cacheNames = "userRoles", key = "@cacheKeys.of('user-roles', #userId)")
	public void assignDefault(UUID userId, Role role) {
		if (roleRepository.existsByUserIdAndRole(userId, role)) {
			return;
		}
		log.info("Assigning default role userId={} role={}", userId, role);
		roleRepository.save(assignment(userId, role));
	}

	/**
	 * Roles persistidos. Si el usuario es anterior a la tabla de roles, se siembra el de su tipo.
	 */
	@Transactional
	public Set<Role> loadOrSeed(UUID userId, Role fallback) {
		Set<Role> stored = loadStored(userId);
		if (!stored.isEmpty()) {
			return stored;
		}
		log.info("Seeding fallback role userId={} role={}", userId, fallback);
		roleRepository.save(assignment(userId, fallback));
		return EnumSet.of(fallback);
	}

	private Set<Role> loadStored(UUID userId) {
		EnumSet<Role> roles = EnumSet.noneOf(Role.class);
		roleRepository.findByUserId(userId).forEach(row -> roles.add(row.getRole()));
		return roles;
	}

	private void requireUser(UUID userId) {
		if (userRepository.findById(userId).isEmpty()) {
			throw new ResourceNotFoundException("User not found: " + userId);
		}
	}

	private static Set<Role> parse(List<String> requested) {
		if (requested == null || requested.isEmpty()) {
			throw new BadRequestException("At least one role is required");
		}
		EnumSet<Role> roles = EnumSet.noneOf(Role.class);
		for (String raw : requested) {
			if (raw == null || raw.isBlank()) {
				throw new BadRequestException("Role name is required");
			}
			try {
				roles.add(Role.valueOf(raw.trim().toUpperCase()));
			} catch (IllegalArgumentException ex) {
				throw new BadRequestException("Unknown role: " + raw.trim());
			}
		}
		return roles;
	}

	private static List<String> names(Set<Role> roles) {
		return roles.stream().map(Role::name).sorted().toList();
	}

	private static UserRoleAssignment assignment(UUID userId, Role role) {
		UserRoleAssignment row = new UserRoleAssignment();
		row.setUserId(userId);
		row.setRole(role);
		return row;
	}
}
