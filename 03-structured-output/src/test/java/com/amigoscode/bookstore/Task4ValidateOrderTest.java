package com.amigoscode.bookstore;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = "spring.ai.model.chat=none")
@Import(FakeChatModelConfig.class)
class Task4ValidateOrderTest {

	@Autowired
	OrderExtractor orderExtractor;

	@Autowired
	FakeChatModel model;

	@Test
	void acceptsAValidOrder() {
		model.reply("""
				{"bookTitle": "Clean Code", "quantity": 1, "deliveryCity": "Porto", "customerEmail": "bo@example.com"}
				""");

		assertThat(orderExtractor.extractValidOrder("1 Clean Code to Porto, bo@example.com").bookTitle())
			.isEqualTo("Clean Code");
	}

	@Test
	void rejectsAQuantityOutOfRange() {
		model.reply("""
				{"bookTitle": "Clean Code", "quantity": 500, "deliveryCity": "Porto", "customerEmail": "bo@example.com"}
				""");

		assertThatThrownBy(() -> orderExtractor.extractValidOrder("500 Clean Code please"))
			.isInstanceOf(InvalidOrderException.class)
			.hasMessageContaining("quantity");
	}

	@Test
	void rejectsAMissingCity() {
		model.reply("""
				{"bookTitle": "Clean Code", "quantity": 1, "deliveryCity": "", "customerEmail": "bo@example.com"}
				""");

		assertThatThrownBy(() -> orderExtractor.extractValidOrder("1 Clean Code please"))
			.isInstanceOf(InvalidOrderException.class)
			.hasMessageContaining("deliveryCity");
	}

	@Test
	void rejectsABookWeDontSell() {
		model.reply("""
				{"bookTitle": "Harry Potter", "quantity": 1, "deliveryCity": "Porto", "customerEmail": "bo@example.com"}
				""");

		assertThatThrownBy(() -> orderExtractor.extractValidOrder("1 Harry Potter to Porto"))
			.isInstanceOf(InvalidOrderException.class)
			.hasMessageContaining("Harry Potter");
	}

}
