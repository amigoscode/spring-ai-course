package com.amigoscode.bookstore;

import java.util.UUID;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Calls the real DeepSeek API. Costs a fraction of a cent.
 * Skipped by default. Run with: ./mvnw test -Plive
 */
@Tag("live")
@SpringBootTest
@Import(TestcontainersConfig.class)
class LiveDeepSeekTest {

	@Autowired
	BookAssistant bookAssistant;

	@Test
	void remembersAcrossRequests() {
		String conversation = UUID.randomUUID().toString();

		bookAssistant.chat(conversation, "Hi, my name is Ana and I love Kotlin.");

		assertThat(bookAssistant.chat(conversation, "What is my name? Reply with the name only.")).contains("Ana");
	}

}
