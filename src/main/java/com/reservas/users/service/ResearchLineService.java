package com.reservas.users.service;

import com.reservas.commons.dto.CatalogDto;
import com.reservas.commons.entity.ResearchLine;
import com.reservas.commons.exception.ConflictException;
import com.reservas.commons.exception.ResourceNotFoundException;
import com.reservas.commons.pagination.PageRequestFactory;
import com.reservas.users.cache.CacheKeyFactory;
import com.reservas.users.cache.IndividualCache;
import com.reservas.users.dto.CatalogPageDto;
import com.reservas.users.dto.request.CatalogWriteRequest;
import com.reservas.users.dto.request.ResearchLineSearchRequest;
import com.reservas.users.mapper.PageMapper;
import com.reservas.users.mapper.ResearchLineMapper;
import com.reservas.users.repository.ResearchLineRepository;
import com.reservas.users.specification.ResearchLineSpecifications;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * CRUD del catálogo {@link ResearchLine} y listado compacto para UI.
 */
@Service
@RequiredArgsConstructor
public class ResearchLineService {

	private static final Logger log = LoggerFactory.getLogger(ResearchLineService.class);

	private final ResearchLineRepository researchLineRepository;
	private final ResearchLineMapper researchLineMapper;
	private final PageRequestFactory pageRequestFactory;
	private final PageMapper pageMapper;
	private final IndividualCache individualCache;
	private final CacheKeyFactory cacheKeys;

	@Transactional
	@Caching(
			put = @CachePut(cacheNames = "researchLines", key = "@cacheKeys.of('research-line', #result.id)"),
			evict = @CacheEvict(cacheNames = "researchLineCatalog", allEntries = true)
	)
	public CatalogDto create(CatalogWriteRequest request) {
		log.info("Creating research line name={}", request.getName());
		if (researchLineRepository.existsByName(request.getName())) {
			throw new ConflictException("Research line already exists: " + request.getName());
		}
		ResearchLine entity = researchLineMapper.toEntity(request);
		return researchLineMapper.toDto(researchLineRepository.save(entity));
	}

	@Transactional(readOnly = true)
	@Cacheable(cacheNames = "researchLineCatalog", key = "@cacheKeys.of('catalog', 'active')")
	public List<CatalogDto> listAllActive() {
		log.info("Listing active research lines catalog");
		return researchLineRepository.findAllActive().stream()
				.map(researchLineMapper::toDto)
				.toList();
	}

	@Transactional(readOnly = true)
	@Cacheable(cacheNames = "researchLines", key = "@cacheKeys.of('research-line', #id)")
	public CatalogDto getById(UUID id) {
		log.info("Getting research line id={}", id);
		return researchLineMapper.toDto(findOrThrow(id));
	}

	@Transactional(readOnly = true)
	public CatalogPageDto search(ResearchLineSearchRequest request) {
		log.info("Searching research lines name={} active={}", request.getName(), request.getActive());
		Page<ResearchLine> page = researchLineRepository.search(
				ResearchLineSpecifications.from(request),
				pageRequestFactory.from(request.getPage(), request.getSize(), Sort.by("name").ascending())
		);
		CatalogPageDto result = pageMapper.toCatalogPage(page);
		individualCache.putEach("researchLines", result.getContent(), item -> cacheKeys.of("research-line", item.getId()));
		return result;
	}

	@Transactional
	@Caching(
			put = @CachePut(cacheNames = "researchLines", key = "@cacheKeys.of('research-line', #id)"),
			evict = @CacheEvict(cacheNames = "researchLineCatalog", allEntries = true)
	)
	public CatalogDto update(UUID id, CatalogWriteRequest request) {
		log.info("Updating research line id={}", id);
		ResearchLine entity = findOrThrow(id);
		if (researchLineRepository.existsByNameAndIdNot(request.getName(), id)) {
			throw new ConflictException("Research line already exists: " + request.getName());
		}
		researchLineMapper.update(request, entity);
		return researchLineMapper.toDto(researchLineRepository.save(entity));
	}

	@Transactional
	@Caching(evict = {
			@CacheEvict(cacheNames = "researchLines", key = "@cacheKeys.of('research-line', #id)"),
			@CacheEvict(cacheNames = "researchLineCatalog", allEntries = true)
	})
	public void deleteLogically(UUID id) {
		log.info("Logical delete research line id={}", id);
		ResearchLine entity = findOrThrow(id);
		entity.setActive(false);
		researchLineRepository.save(entity);
	}

	private ResearchLine findOrThrow(UUID id) {
		return researchLineRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Research line not found: " + id));
	}
}
