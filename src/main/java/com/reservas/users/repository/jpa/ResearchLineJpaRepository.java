package com.reservas.users.repository.jpa;

import com.reservas.commons.entity.ResearchLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface ResearchLineJpaRepository extends JpaRepository<ResearchLine, UUID>, JpaSpecificationExecutor<ResearchLine> {

	List<ResearchLine> findAllByActiveTrueOrderByNameAsc();

	boolean existsByNameIgnoreCase(String name);

	boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);
}
