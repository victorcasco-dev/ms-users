package com.reservas.users.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Metadatos OpenAPI de ms-users (springdoc). */
@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI msUsersOpenApi() {
		return new OpenAPI()
				.info(new Info()
						.title("ms-users")
						.version("1.0.0")
						.description("Users, professors and research-line catalog. Pagination and filters go in the request body."));
	}
}
