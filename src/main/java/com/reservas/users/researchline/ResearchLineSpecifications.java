package com.reservas.users.researchline;

import com.reservas.commons.entity.ResearchLine;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public final class ResearchLineSpecifications {

	private ResearchLineSpecifications() {
	}

	public static Specification<ResearchLine> from(ResearchLineSearchRequest request) {
		return (root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();
			if (StringUtils.hasText(request.getName())) {
				predicates.add(cb.like(cb.lower(root.get("name")), "%" + request.getName().trim().toLowerCase() + "%"));
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
