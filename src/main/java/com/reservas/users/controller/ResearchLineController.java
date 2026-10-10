package com.reservas.users.controller;

import com.reservas.commons.dto.CatalogDto;
import com.reservas.users.dto.CatalogPageDto;
import com.reservas.users.dto.request.CatalogWriteRequest;
import com.reservas.users.dto.request.ResearchLineSearchRequest;
import com.reservas.users.service.ResearchLineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/research-lines")
@RequiredArgsConstructor
@Tag(name = "Research lines")
public class ResearchLineController {

	private static final Logger log = LoggerFactory.getLogger(ResearchLineController.class);

	private final ResearchLineService researchLineService;

	@PostMapping
	@Operation(summary = "Create a research line")
	public ResponseEntity<CatalogDto> create(@Valid @RequestBody CatalogWriteRequest request) {
		log.info("Incoming POST /research-lines name={}", request.getName());
		CatalogDto created = researchLineService.create(request);
		return ResponseEntity.created(URI.create("/research-lines/" + created.getId())).body(created);
	}

	@PostMapping("/search")
	@Operation(summary = "Search research lines with pagination and filters in the request body")
	public CatalogPageDto search(@Valid @RequestBody ResearchLineSearchRequest request) {
		log.info("Incoming POST /research-lines/search");
		return researchLineService.search(request);
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get research line by id")
	public CatalogDto getById(@PathVariable UUID id) {
		log.info("Incoming GET /research-lines/{}", id);
		return researchLineService.getById(id);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update research line")
	public CatalogDto update(@PathVariable UUID id, @Valid @RequestBody CatalogWriteRequest request) {
		log.info("Incoming PUT /research-lines/{}", id);
		return researchLineService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Logical delete research line (active = false)")
	public ResponseEntity<Void> delete(@PathVariable UUID id) {
		log.info("Incoming DELETE /research-lines/{}", id);
		researchLineService.deleteLogically(id);
		return ResponseEntity.noContent().build();
	}
}
