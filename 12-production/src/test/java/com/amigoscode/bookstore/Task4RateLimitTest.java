package com.amigoscode.bookstore;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import com.amigoscode.bookstore.ops.RateLimiter;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;

class Task4RateLimitTest {

	/**
	 * Plain Java, with a clock the test moves forward by hand.
	 */
	@Nested
	class TheLimiter {

		final MovableClock clock = new MovableClock();

		final RateLimiter limiter = new RateLimiter(clock);

		@Test
		void allowsTenRequestsAMinute() {
			for (int i = 0; i < RateLimiter.MAX_REQUESTS; i++) {
				assertThat(limiter.tryAcquire("ana")).as("request " + (i + 1)).isTrue();
			}
			assertThat(limiter.tryAcquire("ana")).as("request 11").isFalse();
		}

		@Test
		void usersHaveTheirOwnLimit() {
			for (int i = 0; i < RateLimiter.MAX_REQUESTS; i++) {
				limiter.tryAcquire("ana");
			}
			assertThat(limiter.tryAcquire("bob")).isTrue();
		}

		@Test
		void theWindowSlides() {
			for (int i = 0; i < RateLimiter.MAX_REQUESTS; i++) {
				limiter.tryAcquire("ana");
			}
			clock.advance(Duration.ofSeconds(61));

			assertThat(limiter.tryAcquire("ana")).isTrue();
		}

	}

	@Nested
	@SpringBootTest(properties = "spring.ai.model.chat=none")
	@AutoConfigureMockMvc
	@Import({ FakeChatModelConfig.class, TestcontainersConfig.class })
	class TheApi {

		@Autowired
		MockMvcTester mvc;

		@Autowired
		RateLimiter rateLimiter;

		@Test
		void theEleventhRequestIsRefused() {
			rateLimiter.reset();
			for (int i = 0; i < RateLimiter.MAX_REQUESTS; i++) {
				assertThat(mvc.get().uri("/api/conversations/any").with(httpBasic("bob", "bob-password"))).hasStatusOk();
			}

			var refused = mvc.get().uri("/api/conversations/any").with(httpBasic("bob", "bob-password"));

			assertThat(refused).hasStatus(HttpStatus.TOO_MANY_REQUESTS).hasHeader("Retry-After", "60");
		}

	}

	static class MovableClock extends Clock {

		private Instant now = Instant.parse("2026-01-01T10:00:00Z");

		void advance(Duration duration) {
			now = now.plus(duration);
		}

		@Override
		public Instant instant() {
			return now;
		}

		@Override
		public ZoneId getZone() {
			return ZoneOffset.UTC;
		}

		@Override
		public Clock withZone(ZoneId zone) {
			return this;
		}

	}

}
