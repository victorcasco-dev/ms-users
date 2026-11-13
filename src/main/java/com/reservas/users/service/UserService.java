package com.reservas.users.service;

import com.reservas.commons.dto.UserCreateDto;
import com.reservas.commons.dto.UserDto;
import com.reservas.commons.dto.UserPageDto;
import com.reservas.commons.entity.Administrator;
import com.reservas.commons.entity.Professor;
import com.reservas.commons.entity.Student;
import com.reservas.commons.entity.User;
import com.reservas.commons.enums.UserType;
import com.reservas.commons.exception.BadRequestException;
import com.reservas.commons.exception.ConflictException;
import com.reservas.commons.exception.ResourceNotFoundException;
import com.reservas.commons.pagination.PageRequestFactory;
import com.reservas.security.Role;
import com.reservas.users.cache.CacheKeyFactory;
import com.reservas.users.cache.IndividualCache;
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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

/**
 * Alta, consulta, búsqueda, actualización y borrado lógico de {@link User} y subtipos.
 */
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
	private final RoleService roleService;
	private final UserTypeChanger typeChanger;
	private final IndividualCache individualCache;
	private final CacheKeyFactory cacheKeys;

	public boolean emailExists(String email) {
		return userRepository.existsByEmail(email);
	}

	/**
	 * Crea un usuario según {@code type} (STUDENT, PROFESSOR, ADMINISTRATOR).
	 *
	 * @param dto datos de alta incl. contraseña en claro (se hashea)
	 * @return usuario persistido
	 */
	@Transactional
	@CachePut(cacheNames = "users", key = "@cacheKeys.of('user', #result.id)")
	public UserDto create(UserCreateDto dto) {
		log.info("Creating user type={} email={}", dto.getType(), dto.getEmail());
		validateCreate(dto);
		if (userRepository.existsByEmail(dto.getEmail())) {
			throw new ConflictException("El correo ya está registrado: " + dto.getEmail());
		}

		String passwordHash = passwordEncoder.encode(dto.getPassword());
		User created = switch (dto.getType()) {
			case STUDENT -> createStudent(dto, passwordHash);
			case PROFESSOR -> userRepository.save(userMapper.toProfessor(dto, passwordHash));
			case ADMINISTRATOR -> userRepository.save(userMapper.toAdministrator(dto, passwordHash));
			case null -> throw new BadRequestException("El tipo de usuario es obligatorio.");
		};
		if (dto.getRoles() != null && !dto.getRoles().isEmpty()) {
			roleService.replace(created.getId(), dto.getRoles());
		} else {
			roleService.assignDefault(created.getId(), Role.valueOf(created.getType().name()));
		}
		UserDto dtoResult = userMapper.toDto(created);
		if (created instanceof Professor) {
			individualCache.put("professors", cacheKeys.of("professor", created.getId()), dtoResult);
		}
		return dtoResult;
	}

	/**
	 * @param id identificador del usuario
	 * @return DTO del usuario activo o inactivo
	 */
	@Transactional(readOnly = true)
	@Cacheable(cacheNames = "users", key = "@cacheKeys.of('user', #id)")
	public UserDto getById(UUID id) {
		log.info("Getting user id={}", id);
		return userMapper.toDto(findOrThrow(id));
	}

	/**
	 * @param request filtros y paginación
	 * @return página de usuarios
	 */
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
		UserPageDto result = pageMapper.toUserPage(page);
		individualCache.putEach("users", result.getContent(), item -> cacheKeys.of("user", item.getId()));
		return result;
	}

	/**
	 * @param id      usuario a modificar
	 * @param request campos opcionales; contraseña solo si se envía
	 * @return usuario actualizado
	 */
	@Transactional
	@CachePut(cacheNames = "users", key = "@cacheKeys.of('user', #id)")
	public UserDto update(UUID id, UserUpdateRequest request) {
		log.info("Updating user id={}", id);
		User user = findOrThrow(id);
		if (userRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
			throw new ConflictException("El correo ya está registrado: " + request.getEmail());
		}
		UserType previousType = user.getType();
		userMapper.updateCommon(request, user);
		if (StringUtils.hasText(request.getPassword())) {
			user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
		}
		boolean typeChanged = request.getType() != null && request.getType() != previousType;
		if (typeChanged) {
			userRepository.save(user);
			typeChanger.change(id, previousType, request.getType(), request);
			user = findOrThrow(id);
			if (previousType == UserType.PROFESSOR) {
				individualCache.evict("professors", cacheKeys.of("professor", id));
			}
		} else {
			updateSubtype(user, request);
		}
		UserDto dtoResult = userMapper.toDto(userRepository.save(user));
		if (request.getRoles() != null && !request.getRoles().isEmpty()) {
			roleService.replace(id, request.getRoles());
		} else if (typeChanged) {
			roleService.assignDefault(id, Role.valueOf(request.getType().name()));
		}
		if (user instanceof Professor) {
			individualCache.put("professors", cacheKeys.of("professor", id), dtoResult);
		}
		return dtoResult;
	}

	/**
	 * Borrado lógico Parte #1: {@code active = false}.
	 *
	 * @param id usuario a desactivar
	 */
	@Transactional
	@Caching(evict = {
			@CacheEvict(cacheNames = "users", key = "@cacheKeys.of('user', #id)"),
			@CacheEvict(cacheNames = "professors", key = "@cacheKeys.of('professor', #id)"),
			@CacheEvict(cacheNames = "userRoles", key = "@cacheKeys.of('user-roles', #id)")
	})
	public void deleteLogically(UUID id) {
		log.info("Logical delete user id={}", id);
		User user = findOrThrow(id);
		user.setActive(false);
		userRepository.save(user);
	}

	private User createStudent(UserCreateDto dto, String passwordHash) {
		if (!StringUtils.hasText(dto.getStudentCode())) {
			throw new BadRequestException("El código de estudiante es obligatorio para un estudiante.");
		}
		if (studentRepository.existsByStudentCode(dto.getStudentCode())) {
			throw new ConflictException("El código de estudiante ya está registrado: " + dto.getStudentCode());
		}
		Professor tutor = resolveTutor(dto.getTutorProfessorId());
		return userRepository.save(userMapper.toStudent(dto, passwordHash, tutor));
	}

	private void updateSubtype(User user, UserUpdateRequest request) {
		if (user instanceof Student student) {
			if (!StringUtils.hasText(request.getStudentCode())) {
				throw new BadRequestException("El código de estudiante es obligatorio para un estudiante.");
			}
			if (studentRepository.existsByStudentCodeAndIdNot(request.getStudentCode(), student.getId())) {
				throw new ConflictException("El código de estudiante ya está registrado: " + request.getStudentCode());
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
				.orElseThrow(() -> new ResourceNotFoundException("No se encontró el profesor tutor."));
	}

	private void validateCreate(UserCreateDto dto) {
		if (!StringUtils.hasText(dto.getEmail())
				|| !StringUtils.hasText(dto.getPassword())
				|| !StringUtils.hasText(dto.getFirstName())
				|| !StringUtils.hasText(dto.getLastName())) {
			throw new BadRequestException("Correo, contraseña, nombre y apellido son obligatorios.");
		}
		if (dto.getPassword().length() < 8) {
			throw new BadRequestException("La contraseña debe tener al menos 8 caracteres.");
		}
	}

	private User findOrThrow(UUID id) {
		return userRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("No se encontró el usuario."));
	}
}
