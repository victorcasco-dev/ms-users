package com.reservas.users.user;

import com.reservas.commons.dto.UserTypeDto;
import com.reservas.commons.entity.Administrator;
import com.reservas.commons.entity.Professor;
import com.reservas.commons.entity.Student;
import com.reservas.commons.entity.User;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public final class UserSpecifications {

	private UserSpecifications() {
	}

	public static Specification<User> from(UserSearchRequest request) {
		return (root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();

			if (StringUtils.hasText(request.getFirstName())) {
				predicates.add(cb.like(cb.lower(root.get("firstName")), contains(request.getFirstName())));
			}
			if (StringUtils.hasText(request.getLastName())) {
				predicates.add(cb.like(cb.lower(root.get("lastName")), contains(request.getLastName())));
			}
			if (StringUtils.hasText(request.getEmail())) {
				predicates.add(cb.like(cb.lower(root.get("email")), contains(request.getEmail())));
			}
			if (request.getType() != null) {
				predicates.add(cb.equal(root.type(), toEntityClass(request.getType())));
			}
			if (request.getActive() != null) {
				predicates.add(cb.equal(root.get("active"), request.getActive()));
			} else {
				predicates.add(cb.isTrue(root.get("active")));
			}

			return cb.and(predicates.toArray(Predicate[]::new));
		};
	}

	private static String contains(String value) {
		return "%" + value.trim().toLowerCase() + "%";
	}

	private static Class<? extends User> toEntityClass(UserTypeDto type) {
		return switch (type) {
			case STUDENT -> Student.class;
			case PROFESSOR -> Professor.class;
			case ADMINISTRATOR -> Administrator.class;
		};
	}
}
