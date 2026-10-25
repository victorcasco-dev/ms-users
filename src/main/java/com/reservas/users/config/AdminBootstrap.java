package com.reservas.users.config;

import com.reservas.commons.dto.UserCreateDto;
import com.reservas.commons.dto.UserTypeDto;
import com.reservas.commons.exception.ConflictException;
import com.reservas.security.SecurityProperties;
import com.reservas.users.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/** Crea el administrador inicial si no existe. Email y password salen de la configuración. */
@Component
@ConditionalOnProperty(prefix = "app.security.bootstrap", name = "enabled", havingValue = "true")
public class AdminBootstrap implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

	private final SecurityProperties securityProperties;
	private final UserService userService;

	public AdminBootstrap(SecurityProperties securityProperties, UserService userService) {
		this.securityProperties = securityProperties;
		this.userService = userService;
	}

	@Override
	public void run(ApplicationArguments args) {
		SecurityProperties.Bootstrap bootstrap = securityProperties.getBootstrap();
		if (!StringUtils.hasText(bootstrap.getEmail()) || !StringUtils.hasText(bootstrap.getPassword())) {
			throw new IllegalStateException("app.security.bootstrap.email and password are required when bootstrap is enabled");
		}
		if (userService.emailExists(bootstrap.getEmail())) {
			log.info("Bootstrap administrator already present email={}", bootstrap.getEmail());
			return;
		}
		UserCreateDto dto = new UserCreateDto()
				.email(bootstrap.getEmail().trim())
				.password(bootstrap.getPassword())
				.firstName(bootstrap.getFirstName())
				.lastName(bootstrap.getLastName())
				.type(UserTypeDto.ADMINISTRATOR);
		try {
			userService.create(dto);
			log.info("Bootstrap administrator created email={}", bootstrap.getEmail());
		} catch (ConflictException ex) {
			log.info("Bootstrap administrator already present email={}", bootstrap.getEmail());
		}
	}
}
