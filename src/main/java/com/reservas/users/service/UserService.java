package com.reservas.users.service;

import com.reservas.commons.dto.UserCreateDto;
import com.reservas.commons.dto.UserDto;
import com.reservas.commons.dto.UserPageDto;
import com.reservas.commons.entity.Administrator;
import com.reservas.commons.entity.Professor;
import com.reservas.commons.entity.Student;
import com.reservas.commons.entity.User;
import com.reservas.commons.exception.BadRequestException;
import com.reservas.commons.exception.ConflictException;
import com.reservas.commons.exception.ResourceNotFoundException;
import com.reservas.users.config.PageRequestFactory;
import com.reservas.users.dto.request.UserSearchRequest;
import com.reservas.users.dto.request.UserUpdateRequest;
import com.reservas.users.mapper.PageMapper;
import com.reservas.users.mapper.UserMapper;
import com.reservas.users.repository.ProfessorRepository;
import com.reservas.users.repository.StudentRepository;
import com.reservas.users.repository.UserRepository;
import com.reservas.users.specification.UserSpecifications;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

	private static final Logger log = LoggerFactory.getLogger(UserService.class);

	private final UserRepository userRepository;
	private final StudentRepository studentRepository;
	private final ProfessorRepository professorRepository;
	private final UserMapper userMapper;
	private final PageMapper pageMapper;
	private final PageRequestFactory pageRequestFactory;
	private final PasswordEncoder passwordEncoder;

	@Transactional
	public UserDto create(UserCreateDto dto) {
		log.info("Creating user type={} email={}", dto.getType(), dto.getEmail());
		validateCreate(dto);
		if (userRepository.existsByEmail(dto.getEmail())) {
			throw new ConflictException("Email already registered: " + dto.getEmail());
		}

		String passwordHash = passwordEncoder.encode(dto.getPassword());
		User created = switch (dto.getType()) {
			case STUDENT -> createStudent(dto, passwordHash);
			case PROFESSOR -> userRepository.save(userMapper.toProfessor(dto, passwordHash));
			case ADMINISTRATOR -> userRepository.save(userMapper.toAdministrator(dto, passwordHash));
			case null -> throw new BadRequestException("type is required");
		};
		return userMapper.toDto(created);
	}

	@Transactional(readOnly = true)
	public UserDto getById(UUID id) {
		log.info("Getting user id={}", id);
		return userMapper.toDto(findOrThrow(id));
	}

	@Transactional(readOnly = true)
	public UserPageDto search(UserSearchRequest request) {
		log.info("Searching users type={} email={}", request.getType(), request.getEmail());
		Page<User> page = userRepository.search(
				UserSpecifications.from(request),
				pageRequestFactory.from(
						request.getPage(),
						request.getSize(),
						Sort.by("lastName").ascending().and(Sort.by("firstName").ascending())
				)
		);
		return pageMapper.toUserPage(page);
	}

	@Transactional
	public UserDto update(UUID id, UserUpdateRequest request) {
		log.info("Updating user id={}", id);
		User user = findOrThrow(id);
		if (userRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
			throw new ConflictException("Email already registered: " + request.getEmail());
		}
		userMapper.updateCommon(request, user);
		if (StringUtils.hasText(request.getPassword())) {
			user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
		}
		updateSubtype(user, request);
		return userMapper.toDto(userRepository.save(user));
	}

	@Transactional
	public void deleteLogically(UUID id) {
		log.info("Logical delete user id={}", id);
		User user = findOrThrow(id);
		user.setActive(false);
		userRepository.save(user);
	}

	private User createStudent(UserCreateDto dto, String passwordHash) {
		if (!StringUtils.hasText(dto.getStudentCode())) {
			throw new BadRequestException("studentCode is required for students");
		}
		if (studentRepository.existsByStudentCode(dto.getStudentCode())) {
			throw new ConflictException("Student code already registered: " + dto.getStudentCode());
		}
		Professor tutor = resolveTutor(dto.getTutorProfessorId());
		return userRepository.save(userMapper.toStudent(dto, passwordHash, tutor));
	}

	private void updateSubtype(User user, UserUpdateRequest request) {
		if (user instanceof Student student) {
			if (!StringUtils.hasText(request.getStudentCode())) {
				throw new BadRequestException("studentCode is required for students");
			}
			if (studentRepository.existsByStudentCodeAndIdNot(request.getStudentCode(), student.getId())) {
				throw new ConflictException("Student code already registered: " + request.getStudentCode());
			}
			student.setStudentCode(request.getStudentCode());
			student.setTutor(resolveTutor(request.getTutorProfessorId()));
		} else if (user instanceof Professor professor) {
			professor.setAcademicTitle(request.getAcademicTitle());
		} else if (user instanceof Administrator administrator) {
			administrator.setPosition(request.getPosition());
		}
	}

	private Professor resolveTutor(UUID tutorProfessorId) {
		if (tutorProfessorId == null) {
			return null;
		}
		return professorRepository.findById(tutorProfessorId)
				.orElseThrow(() -> new ResourceNotFoundException("Tutor professor not found: " + tutorProfessorId));
	}

	private void validateCreate(UserCreateDto dto) {
		if (!StringUtils.hasText(dto.getEmail())
				|| !StringUtils.hasText(dto.getPassword())
				|| !StringUtils.hasText(dto.getFirstName())
				|| !StringUtils.hasText(dto.getLastName())) {
			throw new BadRequestException("email, password, firstName and lastName are required");
		}
		if (dto.getPassword().length() < 8) {
			throw new BadRequestException("password must be at least 8 characters");
		}
	}

	private User findOrThrow(UUID id) {
		return userRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
	}
}
