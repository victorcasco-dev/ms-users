package com.reservas.users.specification;

import com.reservas.commons.entity.Professor;
import com.reservas.commons.entity.ProfessorResearchLine;
import com.reservas.users.dto.request.ProfessorSearchRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

/** Filtros JPA para profesores (incl. línea de investigación). */
public final class ProfessorSpecifications {

	private ProfessorSpecifications() {
	}

	public static Specification<Professor> from(ProfessorSearchRequest request) {
		return (root, query, cb) -> {
			if (query != null) {
				query.distinct(true);
			}
			List<Predicate> predicates = new ArrayList<>();

			if (StringUtils.hasText(request.getName())) {
				String like = "%" + request.getName().trim().toLowerCase() + "%";
				predicates.add(cb.or(
						cb.like(cb.lower(root.get("firstName")), like),
						cb.like(cb.lower(root.get("lastName")), like)
				));
			}
			if (request.getResearchLineId() != null) {
				Join<Professor, ProfessorResearchLine> lines = root.join("researchLines", JoinType.INNER);
				predicates.add(cb.equal(lines.get("researchLine").get("id"), request.getResearchLineId()));
			}
			if (request.getActive() != null) {
				predicates.add(cb.equal(root.get("active"), request.getActive()));
			} else {
				predicates.add(cb.isTrue(root.get("active")));
			}

			return cb.and(predicates.toArray(Predicate[]::new));
		};
	}
}
