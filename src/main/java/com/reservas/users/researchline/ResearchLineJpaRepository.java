package com.reservas.users.researchline;

import com.reservas.commons.entity.ResearchLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface ResearchLineJpaRepository extends JpaRepository<ResearchLine, UUID>, JpaSpecificationExecutor<ResearchLine> {

	boolean existsByNameIgnoreCase(String name);

	boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);
}
