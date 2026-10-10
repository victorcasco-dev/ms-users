package com.reservas.users.repository.jpa;

import com.reservas.commons.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/** Spring Data JPA para {@link Student}. */
public interface StudentJpaRepository extends JpaRepository<Student, UUID> {

	boolean existsByStudentCodeIgnoreCase(String studentCode);

	boolean existsByStudentCodeIgnoreCaseAndIdNot(String studentCode, UUID id);
}
