package com.reservas.users.repository.jpa;

import com.reservas.commons.entity.ProfessorResearchLine;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Spring Data JPA para filas M:N {@link ProfessorResearchLine}. */
public interface ProfessorResearchLineJpaRepository extends JpaRepository<ProfessorResearchLine, UUID>,
		JpaSpecificationExecutor<ProfessorResearchLine> {

	@EntityGraph(attributePaths = "researchLine")
	List<ProfessorResearchLine> findByProfessor_Id(UUID professorId);

	boolean existsByProfessor_IdAndResearchLine_Id(UUID professorId, UUID researchLineId);

	Optional<ProfessorResearchLine> findByIdAndProfessor_Id(UUID id, UUID professorId);
}
