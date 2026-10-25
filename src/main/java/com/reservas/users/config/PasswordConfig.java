package com.reservas.users.config;

import com.reservas.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/** BCrypt. La fuerza sale de {@code app.security.password.bcrypt-strength}. */
@Configuration
public class PasswordConfig {

	@Bean
	public PasswordEncoder passwordEncoder(SecurityProperties properties) {
		return new BCryptPasswordEncoder(properties.getPassword().getBcryptStrength());
	}
}
