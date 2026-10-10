package com.reservas.users.mapper;

import com.reservas.commons.dto.UserCreateDto;
import com.reservas.commons.dto.UserDto;
import com.reservas.commons.entity.Administrator;
import com.reservas.commons.entity.Professor;
import com.reservas.commons.entity.Student;
import com.reservas.commons.entity.User;
import com.reservas.users.dto.request.UserUpdateRequest;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

/** MapStruct: {@link com.reservas.commons.entity.User} y subtipos ↔ DTOs commons. */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

	UserDto toDto(User user);

	@AfterMapping
	default void mapSubtypeFields(User user, @MappingTarget UserDto dto) {
		if (user instanceof Student student) {
			dto.setStudentCode(student.getStudentCode());
			dto.setTutorProfessorId(student.getTutor() != null ? student.getTutor().getId() : null);
		} else if (user instanceof Professor professor) {
			dto.setAcademicTitle(professor.getAcademicTitle());
		} else if (user instanceof Administrator administrator) {
			dto.setPosition(administrator.getPosition());
		}
	}

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "passwordHash", source = "passwordHash")
	@Mapping(target = "active", constant = "true")
	@Mapping(target = "email", source = "dto.email")
	@Mapping(target = "firstName", source = "dto.firstName")
	@Mapping(target = "lastName", source = "dto.lastName")
	@Mapping(target = "studentCode", source = "dto.studentCode")
	@Mapping(target = "tutor", source = "tutor")
	Student toStudent(UserCreateDto dto, String passwordHash, Professor tutor);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "passwordHash", source = "passwordHash")
	@Mapping(target = "active", constant = "true")
	@Mapping(target = "email", source = "dto.email")
	@Mapping(target = "firstName", source = "dto.firstName")
	@Mapping(target = "lastName", source = "dto.lastName")
	@Mapping(target = "academicTitle", source = "dto.academicTitle")
	Professor toProfessor(UserCreateDto dto, String passwordHash);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "passwordHash", source = "passwordHash")
	@Mapping(target = "active", constant = "true")
	@Mapping(target = "email", source = "dto.email")
	@Mapping(target = "firstName", source = "dto.firstName")
	@Mapping(target = "lastName", source = "dto.lastName")
	@Mapping(target = "position", source = "dto.position")
	Administrator toAdministrator(UserCreateDto dto, String passwordHash);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "passwordHash", ignore = true)
	@Mapping(target = "active", ignore = true)
	void updateCommon(UserUpdateRequest request, @MappingTarget User user);
}
