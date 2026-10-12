package com.reservas.users.controller;

import com.reservas.commons.dto.CatalogDto;
import com.reservas.users.dto.CatalogPageDto;
import com.reservas.users.dto.request.CatalogWriteRequest;
import com.reservas.users.dto.request.ResearchLineSearchRequest;
import com.reservas.commons.web.BaseController;
import com.reservas.users.service.ResearchLineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST del catálogo simple {@link com.reservas.commons.entity.ResearchLine}.
 */
@RestController
@RequestMapping("/research-lines")
@RequiredArgsConstructor
@Tag(name = "Research lines")
public class ResearchLineController extends BaseController {

	private final ResearchLineService researchLineService;

	@PostMapping
	@Operation(summary = "Create a research line")
	public ResponseEntity<CatalogDto> create(@Valid @RequestBody CatalogWriteRequest request) {
		log.info("Incoming POST /research-lines name={}", request.getName());
		CatalogDto created = researchLineService.create(request);
		return created("/research-lines/" + created.getId(), created);
	}

	@GetMapping
	@Operation(summary = "List research lines with pagination (query parameters)")
	public CatalogPageDto list(
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer size,
			@RequestParam(required = false) String name,
			@RequestParam(required = false) Boolean active
	) {
		log.info("Incoming GET /research-lines");
		ResearchLineSearchRequest request = new ResearchLineSearchRequest();
		request.setPage(page);
		request.setSize(size);
		request.setName(name);
		request.setActive(active);
		return researchLineService.search(request);
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
		return noContent();
	}
}
