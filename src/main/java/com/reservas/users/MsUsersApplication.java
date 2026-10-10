package com.reservas.users;

import com.reservas.users.config.PaginationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.liquibase.autoconfigure.LiquibaseAutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;

/**
 * Punto de arranque de ms-users (Eureka + JPA sobre entidades commons).
 */
@SpringBootApplication(exclude = LiquibaseAutoConfiguration.class)
@EnableDiscoveryClient
@EntityScan(basePackages = "com.reservas.commons.entity")
@EnableConfigurationProperties(PaginationProperties.class)
public class MsUsersApplication {

	/**
	 * @param args argumentos de línea de comandos de Spring Boot
	 */
	public static void main(String[] args) {
		SpringApplication.run(MsUsersApplication.class, args);
	}
}
