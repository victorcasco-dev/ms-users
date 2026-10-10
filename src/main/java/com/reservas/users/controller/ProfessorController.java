package com.reservas.users.controller;

import com.reservas.commons.dto.ProfessorResearchLineCreateDto;
import com.reservas.commons.dto.ProfessorResearchLineDto;
import com.reservas.commons.dto.UserDto;
import com.reservas.commons.dto.UserPageDto;
import com.reservas.users.dto.request.ProfessorSearchRequest;
import com.reservas.users.dto.request.ProfessorUpdateRequest;
import com.reservas.users.service.ProfessorService;
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
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/professors")
@RequiredArgsConstructor
@Tag(name = "Professors")
public class ProfessorController {

	private static final Logger log = LoggerFactory.getLogger(ProfessorController.class);

	private final ProfessorService professorService;

	@PostMapping("/search")
	@Operation(summary = "Search professors with pagination and filters in the request body")
	public UserPageDto search(@Valid @RequestBody ProfessorSearchRequest request) {
		log.info("Incoming POST /professors/search");
		return professorService.search(request);
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get professor header")
	public UserDto getById(@PathVariable UUID id) {
		log.info("Incoming GET /professors/{}", id);
		return professorService.getById(id);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update professor header")
	public UserDto update(@PathVariable UUID id, @Valid @RequestBody ProfessorUpdateRequest request) {
		log.info("Incoming PUT /professors/{}", id);
		return professorService.update(id, request);
	}

	@GetMapping("/{id}/research-lines")
	@Operation(summary = "List professor research-line details")
	public List<ProfessorResearchLineDto> listResearchLines(@PathVariable UUID id) {
		log.info("Incoming GET /professors/{}/research-lines", id);
		return professorService.listResearchLines(id);
	}

	@PostMapping("/{id}/research-lines")
	@Operation(summary = "Add a research line to a professor")
	public ResponseEntity<ProfessorResearchLineDto> addResearchLine(
			@PathVariable UUID id,
			@Valid @RequestBody ProfessorResearchLineCreateDto request
	) {
		log.info("Incoming POST /professors/{}/research-lines", id);
		ProfessorResearchLineDto created = professorService.addResearchLine(id, request);
		return ResponseEntity
				.created(URI.create("/professors/" + id + "/research-lines/" + created.getId()))
				.body(created);
	}

	@DeleteMapping("/{id}/research-lines/{detailId}")
	@Operation(summary = "Remove a research line from a professor")
	public ResponseEntity<Void> removeResearchLine(@PathVariable UUID id, @PathVariable UUID detailId) {
		log.info("Incoming DELETE /professors/{}/research-lines/{}", id, detailId);
		professorService.removeResearchLine(id, detailId);
		return ResponseEntity.noContent().build();
	}
}
