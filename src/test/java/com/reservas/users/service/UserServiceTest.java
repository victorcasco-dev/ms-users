package com.reservas.users.service;

import com.reservas.commons.dto.UserCreateDto;
import com.reservas.commons.dto.UserDto;
import com.reservas.commons.dto.UserTypeDto;
import com.reservas.commons.entity.Student;
import com.reservas.commons.exception.BadRequestException;
import com.reservas.commons.exception.ConflictException;
import com.reservas.commons.exception.ResourceNotFoundException;
import com.reservas.commons.pagination.PageRequestFactory;
import com.reservas.users.mapper.PageMapper;
import com.reservas.users.mapper.UserMapper;
import com.reservas.users.repository.ProfessorRepository;
import com.reservas.users.repository.StudentRepository;
import com.reservas.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	@Mock
	private UserRepository userRepository;
	@Mock
	private StudentRepository studentRepository;
	@Mock
	private ProfessorRepository professorRepository;
	@Mock
	private UserMapper userMapper;
	@Mock
	private PageMapper pageMapper;
	@Mock
	private PageRequestFactory pageRequestFactory;
	@Mock
	private PasswordEncoder passwordEncoder;

	private UserService userService;

	@BeforeEach
	void setUp() {
		userService = new UserService(
				userRepository,
				studentRepository,
				professorRepository,
				userMapper,
				pageMapper,
				pageRequestFactory,
				passwordEncoder
		);
	}

	@Test
	void createStudentRequiresStudentCode() {
		UserCreateDto dto = new UserCreateDto()
				.email("alumno@test.com")
				.password("secret123")
				.firstName("Ana")
				.lastName("Perez")
				.type(UserTypeDto.STUDENT);

		assertThrows(BadRequestException.class, () -> userService.create(dto));
	}

	@Test
	void createRejectsDuplicateEmail() {
		UserCreateDto dto = new UserCreateDto()
				.email("alumno@test.com")
				.password("secret123")
				.firstName("Ana")
				.lastName("Perez")
				.type(UserTypeDto.STUDENT)
				.studentCode("STU-1");
		when(userRepository.existsByEmail("alumno@test.com")).thenReturn(true);

		assertThrows(ConflictException.class, () -> userService.create(dto));
	}

	@Test
	void createStudentPersistsMappedEntity() {
		UserCreateDto dto = new UserCreateDto()
				.email("alumno@test.com")
				.password("secret123")
				.firstName("Ana")
				.lastName("Perez")
				.type(UserTypeDto.STUDENT)
				.studentCode("STU-1");
		Student student = new Student();
		student.setEmail(dto.getEmail());
		student.setStudentCode(dto.getStudentCode());
		UserDto mapped = new UserDto().email(dto.getEmail()).type(UserTypeDto.STUDENT);

		when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
		when(studentRepository.existsByStudentCode(dto.getStudentCode())).thenReturn(false);
		when(passwordEncoder.encode("secret123")).thenReturn("hash");
		when(userMapper.toStudent(dto, "hash", null)).thenReturn(student);
		when(userRepository.save(student)).thenReturn(student);
		when(userMapper.toDto(student)).thenReturn(mapped);

		UserDto result = userService.create(dto);

		assertEquals("alumno@test.com", result.getEmail());
		verify(userRepository).save(student);
	}

	@Test
	void getByIdThrowsWhenMissing() {
		UUID id = UUID.randomUUID();
		when(userRepository.findById(id)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class, () -> userService.getById(id));
	}

	@Test
	void logicalDeleteMarksUserInactive() {
		UUID id = UUID.randomUUID();
		Student student = new Student();
		student.setActive(true);
		when(userRepository.findById(id)).thenReturn(Optional.of(student));
		when(userRepository.save(student)).thenReturn(student);

		userService.deleteLogically(id);

		assertFalse(student.isActive());
		verify(userRepository).save(student);
	}
}
