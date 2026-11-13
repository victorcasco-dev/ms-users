package com.reservas.users.service;

import com.reservas.commons.enums.UserType;
import com.reservas.commons.exception.BadRequestException;
import com.reservas.commons.exception.ConflictException;
import com.reservas.commons.exception.ResourceNotFoundException;
import com.reservas.users.dto.request.UserUpdateRequest;
import com.reservas.users.repository.StudentRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.UUID;

/**
 * Cambia el tipo de un usuario (estudiante, profesor o administrador).
 *
 * <p>La jerarquía es JOINED: cada tipo tiene su tabla ({@code student}, {@code professor},
 * {@code administrator}). Hibernate no permite cambiar la clase de una entidad, así que se mueve la
 * fila con SQL nativo dentro de la transacción del llamador. Se rechaza el cambio si el usuario tiene
 * datos que dependen de su tipo actual (turnos o líneas de investigación de un profesor, reservas de un
 * estudiante) para no borrarlos en cascada.</p>
 */
@Component
@RequiredArgsConstructor
class UserTypeChanger {

	private static final Logger log = LoggerFactory.getLogger(UserTypeChanger.class);

	private final EntityManager entityManager;
	private final StudentRepository studentRepository;

	/**
	 * Debe ejecutarse dentro de una transacción. Al terminar limpia el contexto de persistencia: el
	 * llamador tiene que volver a cargar el usuario.
	 */
	void change(UUID userId, UserType from, UserType to, UserUpdateRequest request) {
		if (from == to) {
			return;
		}
		log.info("Changing user type id={} from={} to={}", userId, from, to);
		ensureNoDependents(userId, from);
		validateTarget(userId, to, request);

		entityManager.flush();
		entityManager.clear();

		entityManager.createNativeQuery("DELETE FROM " + table(from) + " WHERE id = :id")
				.setParameter("id", userId)
				.executeUpdate();
		entityManager.createNativeQuery("UPDATE users SET user_type = :type, updated_at = NOW() WHERE id = :id")
				.setParameter("type", to.name())
				.setParameter("id", userId)
				.executeUpdate();
		switch (to) {
			case STUDENT -> entityManager.createNativeQuery(
							"INSERT INTO student (id, student_code, tutor_professor_id) VALUES (:id, :code, :tutor)")
					.setParameter("id", userId)
					.setParameter("code", request.getStudentCode().trim())
					.setParameter("tutor", request.getTutorProfessorId())
					.executeUpdate();
			case PROFESSOR -> entityManager.createNativeQuery(
							"INSERT INTO professor (id, academic_title) VALUES (:id, :title)")
					.setParameter("id", userId)
					.setParameter("title", blankToNull(request.getAcademicTitle()))
					.executeUpdate();
			case ADMINISTRATOR -> entityManager.createNativeQuery(
							"INSERT INTO administrator (id, position) VALUES (:id, :position)")
					.setParameter("id", userId)
					.setParameter("position", blankToNull(request.getPosition()))
					.executeUpdate();
		}
		entityManager.clear();
	}

	private void ensureNoDependents(UUID userId, UserType from) {
		if (from == UserType.PROFESSOR) {
			if (count("SELECT COUNT(*) FROM time_slot WHERE professor_id = :id", userId) > 0) {
				throw new ConflictException(
						"No se puede cambiar el tipo: el profesor tiene turnos publicados. Cancelalos o reasignalos primero.");
			}
			if (count("SELECT COUNT(*) FROM professor_research_line WHERE professor_id = :id", userId) > 0) {
				throw new ConflictException(
						"No se puede cambiar el tipo: el profesor tiene líneas de investigación asignadas.");
			}
		}
		if (from == UserType.STUDENT
				&& count("SELECT COUNT(*) FROM reservation WHERE student_id = :id", userId) > 0) {
			throw new ConflictException("No se puede cambiar el tipo: el estudiante tiene reservas registradas.");
		}
	}

	private void validateTarget(UUID userId, UserType to, UserUpdateRequest request) {
		if (to != UserType.STUDENT) {
			return;
		}
		if (!StringUtils.hasText(request.getStudentCode())) {
			throw new BadRequestException("El código de estudiante es obligatorio para un estudiante.");
		}
		if (studentRepository.existsByStudentCodeAndIdNot(request.getStudentCode().trim(), userId)) {
			throw new ConflictException("El código de estudiante ya está registrado: " + request.getStudentCode());
		}
		UUID tutor = request.getTutorProfessorId();
		if (tutor != null && count("SELECT COUNT(*) FROM professor WHERE id = :id", tutor) == 0) {
			throw new ResourceNotFoundException("No se encontró el profesor tutor.");
		}
	}

	private long count(String sql, UUID id) {
		Object value = entityManager.createNativeQuery(sql).setParameter("id", id).getSingleResult();
		return ((Number) value).longValue();
	}

	private static String table(UserType type) {
		return switch (type) {
			case STUDENT -> "student";
			case PROFESSOR -> "professor";
			case ADMINISTRATOR -> "administrator";
		};
	}

	private static String blankToNull(String value) {
		return StringUtils.hasText(value) ? value.trim() : null;
	}
}
