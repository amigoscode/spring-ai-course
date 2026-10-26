package com.amigoscode.bookstore;

import com.amigoscode.bookstore.ops.RateLimiter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;

@SpringBootTest(properties = "spring.ai.model.chat=none")
@AutoConfigureMockMvc
@Import({ FakeChatModelConfig.class, TestcontainersConfig.class })
class Task1SecurityTest {

	@Autowired
	MockMvcTester mvc;

	@Autowired
	RateLimiter rateLimiter;

	@BeforeEach
	void reset() {
		rateLimiter.reset();
	}

	@Test
	void theApiNeedsALogin() {
		assertThat(mvc.get().uri("/api/usage")).hasStatus(HttpStatus.UNAUTHORIZED);
	}

	@Test
	void customersCanLogIn() {
		assertThat(mvc.get().uri("/api/usage").with(httpBasic("ana", "ana-password"))).hasStatusOk();
		assertThat(mvc.get().uri("/api/usage").with(httpBasic("bob", "bob-password"))).hasStatusOk();
	}

	@Test
	void aWrongPasswordIsRefused() {
		assertThat(mvc.get().uri("/api/usage").with(httpBasic("ana", "guess"))).hasStatus(HttpStatus.UNAUTHORIZED);
	}

	@Test
	void healthIsPublicButMetricsAreForAdmins() {
		assertThat(mvc.get().uri("/actuator/health")).hasStatusOk();
		assertThat(mvc.get().uri("/actuator/metrics").with(httpBasic("ana", "ana-password"))).hasStatus(HttpStatus.FORBIDDEN);
		assertThat(mvc.get().uri("/actuator/metrics").with(httpBasic("admin", "admin-password"))).hasStatusOk();
	}

}
