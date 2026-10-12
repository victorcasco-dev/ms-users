package com.reservas.users.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

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

	@Bean
	public OpenApiCustomizer gatewayServerUrl(@Value("${app.openapi.server-url}") String gatewayUrl) {
		return openApi -> openApi.servers(List.of(
				new Server().url(gatewayUrl).description("API Gateway")));
	}
}
