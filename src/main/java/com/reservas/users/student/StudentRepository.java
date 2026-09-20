package com.reservas.users.student;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class StudentRepository {

	private static final Logger log = LoggerFactory.getLogger(StudentRepository.class);

	private final StudentJpaRepository jpa;

	public boolean existsByStudentCode(String studentCode) {
		log.debug("Checking student code uniqueness code={}", studentCode);
		return jpa.existsByStudentCodeIgnoreCase(studentCode);
	}

	public boolean existsByStudentCodeAndIdNot(String studentCode, UUID id) {
		log.debug("Checking student code uniqueness code={} excluding id={}", studentCode, id);
		return jpa.existsByStudentCodeIgnoreCaseAndIdNot(studentCode, id);
	}
}
