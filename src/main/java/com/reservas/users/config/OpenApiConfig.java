package com.reservas.users.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
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
						.description("Users, professors, research lines and JWT login. Send Authorization: Bearer <token> except on POST /auth/login."))
				.components(new Components().addSecuritySchemes("bearerAuth", bearerScheme()))
				.addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
	}

	private static SecurityScheme bearerScheme() {
		return new SecurityScheme()
				.type(SecurityScheme.Type.HTTP)
				.scheme("bearer")
				.bearerFormat("JWT");
	}

	@Bean
	public OpenApiCustomizer gatewayServerUrl(@Value("${app.openapi.server-url}") String gatewayUrl) {
		return openApi -> openApi.servers(List.of(
				new Server().url(gatewayUrl).description("API Gateway")));
	}
}
