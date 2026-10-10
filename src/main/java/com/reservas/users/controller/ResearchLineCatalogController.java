package com.reservas.users.controller;

import com.reservas.commons.dto.CatalogDto;
import com.reservas.users.service.ResearchLineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/catalogs/research-lines")
@RequiredArgsConstructor
@Tag(name = "Catalogs")
public class ResearchLineCatalogController {

	private static final Logger log = LoggerFactory.getLogger(ResearchLineCatalogController.class);

	private final ResearchLineService researchLineService;

	@GetMapping
	@Operation(summary = "Research line catalog (OpenAPI contract)")
	public List<CatalogDto> listActive() {
		log.info("Incoming GET /catalogs/research-lines");
		return researchLineService.listAllActive();
	}
}
