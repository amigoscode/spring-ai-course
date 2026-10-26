package com.amigoscode.bookstore.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Who is calling. From here on the customer id is the logged-in user, never a header.
 * HTTP Basic with users in memory keeps the module small. A real app would use OAuth2 / OIDC.
 */
@Configuration
public class SecurityConfig {

	// TODO task-1: a SecurityFilterChain that
	//   - lets anyone call /actuator/health and the read-only MCP server at /mcp
	//   - only lets ADMIN users call the other /actuator endpoints and /api/usage
	//   - requires login (HTTP Basic) for everything else
	//   - is stateless, with CSRF off (a JSON API without cookies)
	// and a UserDetailsService with ana / ana-password and bob / bob-password (CUSTOMER),
	// and admin / admin-password (ADMIN)

	@Bean
	PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}

}
