package com.reservas.users.security;

import com.reservas.security.JwtClaims;
import com.reservas.security.JwtTokenProvider;
import com.reservas.security.Role;
import com.reservas.security.SecurityProperties;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtTokenProviderTest {

	@Test
	void generateAndParseKeepsSubjectEmailAndRoles() {
		JwtTokenProvider provider = provider(Clock.systemUTC());
		UUID userId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

		String token = provider.generate(userId, "ana@test.com", EnumSet.of(Role.PROFESSOR, Role.STUDENT));
		JwtClaims claims = provider.parse(token);

		assertEquals(userId, claims.userId());
		assertEquals("ana@test.com", claims.email());
		assertEquals(2, claims.roles().size());
		assertTrue(claims.roles().contains("STUDENT"));
		assertTrue(claims.roles().contains("PROFESSOR"));
	}

	@Test
	void expiredTokenIsRejected() {
		Clock issuedAt = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);
		JwtTokenProvider issuer = provider(issuedAt);
		String token = issuer.generate(UUID.randomUUID(), "ana@test.com", EnumSet.of(Role.STUDENT));

		Clock later = Clock.fixed(Instant.parse("2026-01-01T03:00:00Z"), ZoneOffset.UTC);
		JwtTokenProvider parser = provider(later);

		assertThrows(RuntimeException.class, () -> parser.parse(token));
	}

	private static JwtTokenProvider provider(Clock clock) {
		SecurityProperties properties = new SecurityProperties();
		properties.getJwt().setSecret("local-dev-only-change-this-secret-key");
		properties.getJwt().setIssuer("reservas-ms-users");
		properties.getJwt().setExpiration(Duration.ofHours(1));
		properties.getJwt().setClockSkew(Duration.ofSeconds(30));
		return new JwtTokenProvider(properties, clock);
	}
}
