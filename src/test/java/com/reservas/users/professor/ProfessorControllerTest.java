package com.reservas.users.professor;

import com.reservas.commons.dto.ProfessorResearchLineCreateDto;
import com.reservas.commons.dto.ProfessorResearchLineDto;
import com.reservas.commons.dto.UserPageDto;
import com.reservas.commons.exception.ApiExceptionHandler;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ProfessorController.class)
@AutoConfigureMockMvc
@Import(ApiExceptionHandler.class)
class ProfessorControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ProfessorService professorService;

	@Test
	void searchUsesRequestBody() throws Exception {
		when(professorService.search(any(ProfessorSearchRequest.class))).thenReturn(new UserPageDto().content(List.of()));

		mockMvc.perform(post("/professors/search")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"page\":0,\"size\":10,\"name\":\"Garcia\"}"))
				.andExpect(status().isOk());
	}

	@Test
	void addResearchLineReturns201() throws Exception {
		UUID professorId = UUID.fromString("33333333-3333-3333-3333-333333333333");
		ProfessorResearchLineDto dto = new ProfessorResearchLineDto().id(UUID.randomUUID()).professorId(professorId);
		when(professorService.addResearchLine(eq(professorId), any(ProfessorResearchLineCreateDto.class))).thenReturn(dto);

		mockMvc.perform(post("/professors/" + professorId + "/research-lines")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"researchLineId\":\"44444444-4444-4444-4444-444444444444\",\"experienceLevel\":\"EXPERT\"}"))
				.andExpect(status().isCreated());
	}

	@Test
	void listResearchLinesReturns200() throws Exception {
		UUID professorId = UUID.fromString("33333333-3333-3333-3333-333333333333");
		when(professorService.listResearchLines(professorId)).thenReturn(List.of());

		mockMvc.perform(get("/professors/" + professorId + "/research-lines"))
				.andExpect(status().isOk());
	}
}
