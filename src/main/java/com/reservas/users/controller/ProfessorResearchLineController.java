package com.reservas.users.controller;

import com.reservas.commons.dto.ProfessorResearchLineDto;
import com.reservas.users.dto.ProfessorResearchLinePageDto;
import com.reservas.users.dto.request.ProfessorResearchLineSearchRequest;
import com.reservas.users.dto.request.ProfessorResearchLineUpdateRequest;
import com.reservas.commons.web.BaseController;
import com.reservas.users.service.ProfessorResearchLineService;
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
 * REST del módulo cabecera-detalle M:N: filas {@code professor_research_line}.
 *
 * <p>La creación se expone en {@code POST /professors/{id}/research-lines} (cabecera profesor).</p>
 */
@RestController
@RequestMapping("/professor-research-lines")
@RequiredArgsConstructor
@Tag(name = "Professor research lines (M:N detail)")
public class ProfessorResearchLineController extends BaseController {

	private final ProfessorResearchLineService professorResearchLineService;

	/**
	 * Listado paginado vía query string (Parte #1 — GET con paginación).
	 */
	@GetMapping
	@Operation(summary = "List professor–research-line associations with pagination")
	public ProfessorResearchLinePageDto list(
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer size,
			@RequestParam(required = false) UUID professorId,
			@RequestParam(required = false) UUID researchLineId
	) {
		log.info("Incoming GET /professor-research-lines");
		ProfessorResearchLineSearchRequest request = new ProfessorResearchLineSearchRequest();
		request.setPage(page);
		request.setSize(size);
		request.setProfessorId(professorId);
		request.setResearchLineId(researchLineId);
		return professorResearchLineService.search(request);
	}

	@PostMapping("/search")
	@Operation(summary = "Search associations with filters in the request body")
	public ProfessorResearchLinePageDto search(@Valid @RequestBody ProfessorResearchLineSearchRequest request) {
		log.info("Incoming POST /professor-research-lines/search");
		return professorResearchLineService.search(request);
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get association row by id")
	public ProfessorResearchLineDto getById(@PathVariable UUID id) {
		log.info("Incoming GET /professor-research-lines/{}", id);
		return professorResearchLineService.getById(id);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update experience level on a detail row")
	public ProfessorResearchLineDto update(
			@PathVariable UUID id,
			@Valid @RequestBody ProfessorResearchLineUpdateRequest request
	) {
		log.info("Incoming PUT /professor-research-lines/{}", id);
		return professorResearchLineService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Dissociate professor and research line (remove bridge row)")
	public ResponseEntity<Void> delete(@PathVariable UUID id) {
		log.info("Incoming DELETE /professor-research-lines/{}", id);
		professorResearchLineService.dissociate(id);
		return noContent();
	}
}
