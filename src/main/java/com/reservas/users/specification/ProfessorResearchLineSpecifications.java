package com.reservas.users.specification;

import com.reservas.commons.entity.ProfessorResearchLine;
import com.reservas.users.dto.request.ProfessorResearchLineSearchRequest;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Criterios JPA para filtrar {@link ProfessorResearchLine}.
 */
public final class ProfessorResearchLineSpecifications {

	private ProfessorResearchLineSpecifications() {
	}

	/**
	 * Construye la especificación a partir del request de búsqueda.
	 *
	 * @param request filtros opcionales de profesor y línea
	 * @return especificación compuesta
	 */
	public static Specification<ProfessorResearchLine> from(ProfessorResearchLineSearchRequest request) {
		return (root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();
			if (request.getProfessorId() != null) {
				predicates.add(cb.equal(root.get("professor").get("id"), request.getProfessorId()));
			}
			if (request.getResearchLineId() != null) {
				predicates.add(cb.equal(root.get("researchLine").get("id"), request.getResearchLineId()));
			}
			return cb.and(predicates.toArray(Predicate[]::new));
		};
	}
}
