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
	void recommendsFromTheCatalog() {
		assertThat(bookAssistant.recommend("test driven development", 1)).contains("Kent Beck");
	}

	@Test
	void classifiesAReview() {
		assertThat(bookAssistant.classify("A complete waste of money, I returned it.")).isEqualTo(Sentiment.NEGATIVE);
	}

}
