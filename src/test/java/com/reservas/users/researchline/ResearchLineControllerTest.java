package com.reservas.users.researchline;

import com.reservas.commons.dto.CatalogDto;
import com.reservas.commons.exception.ApiExceptionHandler;
import com.reservas.commons.exception.ResourceNotFoundException;
import com.reservas.users.shared.CatalogPageDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ResearchLineController.class)
@AutoConfigureMockMvc
@Import(ApiExceptionHandler.class)
class ResearchLineControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ResearchLineService researchLineService;

	@Test
	void searchUsesRequestBody() throws Exception {
		CatalogPageDto page = new CatalogPageDto();
		page.setContent(List.of());
		when(researchLineService.search(any(ResearchLineSearchRequest.class))).thenReturn(page);

		mockMvc.perform(post("/research-lines/search")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"page\":0,\"size\":5,\"name\":\"IA\"}"))
				.andExpect(status().isOk());
	}

	@Test
	void getByIdReturns404FromSharedHandler() throws Exception {
		UUID id = UUID.fromString("22222222-2222-2222-2222-222222222222");
		when(researchLineService.getById(id)).thenThrow(new ResourceNotFoundException("Research line not found: " + id));

		mockMvc.perform(get("/research-lines/" + id))
				.andExpect(status().isNotFound());
	}

	@Test
	void createReturns201() throws Exception {
		CatalogDto dto = new CatalogDto()
				.id(UUID.fromString("22222222-2222-2222-2222-222222222222"))
				.name("IA")
				.active(true);
		when(researchLineService.create(any(CatalogWriteRequest.class))).thenReturn(dto);

		mockMvc.perform(post("/research-lines")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"name\":\"IA\",\"description\":\"Inteligencia Artificial\"}"))
				.andExpect(status().isCreated());
	}
}
