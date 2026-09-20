package com.reservas.users.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reservas.commons.dto.UserCreateDto;
import com.reservas.commons.dto.UserDto;
import com.reservas.commons.dto.UserPageDto;
import com.reservas.commons.dto.UserTypeDto;
import com.reservas.commons.exception.ApiExceptionHandler;
import com.reservas.commons.exception.ResourceNotFoundException;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc
@Import(ApiExceptionHandler.class)
class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@MockitoBean
	private UserService userService;

	@Test
	void createUserReturns201() throws Exception {
		UUID id = UUID.fromString("11111111-1111-1111-1111-111111111111");
		UserCreateDto request = new UserCreateDto()
				.email("alumno@test.com")
				.password("secret123")
				.firstName("Ana")
				.lastName("Perez")
				.type(UserTypeDto.STUDENT)
				.studentCode("STU-1");
		UserDto response = new UserDto()
				.id(id)
				.email("alumno@test.com")
				.firstName("Ana")
				.lastName("Perez")
				.type(UserTypeDto.STUDENT)
				.active(true)
				.studentCode("STU-1");
		when(userService.create(any(UserCreateDto.class))).thenReturn(response);

		mockMvc.perform(post("/users")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", "/users/" + id))
				.andExpect(jsonPath("$.email").value("alumno@test.com"));
	}

	@Test
	void searchUsersUsesRequestBodyNotQueryString() throws Exception {
		UserPageDto page = new UserPageDto().content(List.of());
		when(userService.search(any(UserSearchRequest.class))).thenReturn(page);

		mockMvc.perform(post("/users/search")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"page\":0,\"size\":10,\"email\":\"ana\"}"))
				.andExpect(status().isOk());
	}

	@Test
	void missingUserReturns404() throws Exception {
		UUID id = UUID.fromString("11111111-1111-1111-1111-111111111111");
		when(userService.getById(id)).thenThrow(new ResourceNotFoundException("User not found: " + id));

		mockMvc.perform(get("/users/" + id))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
	}

	@Test
	void logicalDeleteReturns204() throws Exception {
		UUID id = UUID.fromString("11111111-1111-1111-1111-111111111111");
		doNothing().when(userService).deleteLogically(id);

		mockMvc.perform(delete("/users/" + id))
				.andExpect(status().isNoContent());
	}
}
