package com.amigoscode.bookstore;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Calls the real DeepSeek API. Costs a fraction of a cent.
 * Skipped by default. Run with: ./mvnw test -Plive
 */
@Tag("live")
@SpringBootTest
class LiveDeepSeekTest {

	@Autowired
	BookAssistant bookAssistant;

	@Test
	void deepSeekAnswers() {
		BookAssistant.Answer answer = bookAssistant.askWithUsage("Reply with exactly one word: pong");

		assertThat(answer.text()).containsIgnoringCase("pong");
		assertThat(answer.promptTokens()).isPositive();
	}

}
