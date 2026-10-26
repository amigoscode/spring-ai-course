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

	@Autowired
	OrderExtractor orderExtractor;

	@Test
	void recommendsAListOfRealCatalogBooks() {
		var books = bookAssistant.recommendMany("java", 2);

		assertThat(books).hasSizeBetween(1, 2);
		assertThat(books).allSatisfy(book -> assertThat(book.title()).isNotBlank());
	}

	@Test
	void extractsAnOrderFromAMessyEmail() {
		OrderRequest order = orderExtractor.extractValidOrder("""
				hey there, can u send me 3 of the clean code book by uncle bob?
				ship to Berlin pls. my mail is max@example.com
				""");

		assertThat(order.bookTitle()).isEqualTo("Clean Code");
		assertThat(order.quantity()).isEqualTo(3);
		assertThat(order.deliveryCity()).isEqualTo("Berlin");
	}

}
