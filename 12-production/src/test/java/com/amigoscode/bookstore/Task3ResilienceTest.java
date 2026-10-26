package com.amigoscode.bookstore;

import java.time.Duration;

import com.amigoscode.bookstore.ops.RateLimiter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.retry.NonTransientAiException;
import org.springframework.ai.retry.TransientAiException;
import org.springframework.ai.retry.autoconfigure.SpringAiRetryProperties;
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
class Task3ResilienceTest {

	@Autowired
	MockMvcTester mvc;

	@Autowired
	FakeChatModel model;

	@Autowired
	SpringAiRetryProperties retry;

	@Autowired
	RateLimiter rateLimiter;

	@BeforeEach
	void reset() {
		rateLimiter.reset();
	}

	@Test
	void retriesAFewTimesThenGivesUpFast() {
		assertThat(retry.getMaxAttempts()).isEqualTo(3);
		assertThat(retry.getBackoff().getInitialInterval()).isEqualTo(Duration.ofMillis(500));
		assertThat(retry.getBackoff().getMultiplier()).isEqualTo(2);
		assertThat(retry.getBackoff().getMaxInterval()).isEqualTo(Duration.ofSeconds(5));
	}

	@Test
	void aBusyModelBecomesA503() {
		model.failWith(new TransientAiException("HTTP 503 from api.deepseek.com, request id abc-123"));

		var result = mvc.get().uri("/api/chat").param("question", "Hi").with(httpBasic("ana", "ana-password"));

		assertThat(result).hasStatus(HttpStatus.SERVICE_UNAVAILABLE);
		assertThat(result).bodyText().contains("try again").doesNotContain("abc-123").doesNotContain("deepseek");
	}

	@Test
	void aRejectedRequestBecomesA503WithoutDetails() {
		model.failWith(new NonTransientAiException("401 Unauthorized: invalid api key sk-1234"));

		var result = mvc.get().uri("/api/chat").param("question", "Hi").with(httpBasic("ana", "ana-password"));

		assertThat(result).hasStatus(HttpStatus.SERVICE_UNAVAILABLE);
		assertThat(result).bodyText().doesNotContain("sk-1234").doesNotContain("api key");
	}

}
