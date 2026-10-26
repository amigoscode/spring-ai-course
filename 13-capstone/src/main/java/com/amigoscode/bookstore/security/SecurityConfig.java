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

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/actuator/health").permitAll()
				// Read-only catalog tools for any MCP client (module 09). Nothing here acts for a user.
				.requestMatchers("/mcp", "/mcp/**").permitAll()
				.requestMatchers("/actuator/**").hasRole("ADMIN")
				// What the whole app has spent is business data, not something customers should see.
				.requestMatchers("/api/usage").hasRole("ADMIN")
				.anyRequest().authenticated())
			.httpBasic(Customizer.withDefaults())
			// A stateless JSON API with no browser sessions or cookies, so no CSRF tokens needed.
			.csrf(csrf -> csrf.disable())
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.build();
	}

	@Bean
	UserDetailsService users(PasswordEncoder encoder) {
		return new InMemoryUserDetailsManager(
				User.withUsername("ana").password(encoder.encode("ana-password")).roles("CUSTOMER").build(),
				User.withUsername("bob").password(encoder.encode("bob-password")).roles("CUSTOMER").build(),
				User.withUsername("admin").password(encoder.encode("admin-password")).roles("ADMIN").build());
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}

}
