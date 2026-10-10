package com.reservas.users.repository.jpa;

import com.reservas.commons.entity.Professor;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

/** Spring Data JPA para {@link Professor}. */
public interface ProfessorJpaRepository extends JpaRepository<Professor, UUID>, JpaSpecificationExecutor<Professor> {

	boolean existsByEmailIgnoreCaseAndIdNot(String email, UUID id);

	@EntityGraph(attributePaths = {"researchLines", "researchLines.researchLine"})
	@Query("SELECT p FROM Professor p WHERE p.id = :id")
	Optional<Professor> findDetailedById(@Param("id") UUID id);
}
