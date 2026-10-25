package com.reservas.users.security;

import com.reservas.security.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/** Asignación de un rol a un usuario. Vive solo en ms-users. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
		name = "user_role",
		uniqueConstraints = @UniqueConstraint(name = "uk_user_role_user_role", columnNames = {"user_id", "role_name"})
)
public class UserRoleAssignment {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "user_id", nullable = false)
	private UUID userId;

	@Enumerated(EnumType.STRING)
	@Column(name = "role_name", nullable = false, length = 32)
	private Role role;
}
