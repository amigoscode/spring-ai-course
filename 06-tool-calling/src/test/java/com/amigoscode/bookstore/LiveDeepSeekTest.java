package com.amigoscode.bookstore;

import java.util.UUID;

import com.amigoscode.bookstore.order.OrderService;
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

	@Autowired
	OrderService orderService;

	@Test
	void checksTheStockWithATool() {
		String answer = bookAssistant.assist("ana", UUID.randomUUID().toString(),
				"How many copies of Clean Code do you have? Answer with the number.");

		assertThat(answer).contains("3");
	}

	@Test
	void placesAnOrderAfterConfirming() {
		orderService.reset();
		String conversation = UUID.randomUUID().toString();

		bookAssistant.assist("ana", conversation, "I'd like 2 copies of Refactoring.");
		bookAssistant.assist("ana", conversation, "Yes, I confirm: 2 copies of Refactoring. Please place the order now.");

		assertThat(orderService.ordersOf("ana")).singleElement()
			.satisfies(order -> assertThat(order.quantity()).isEqualTo(2));
	}

}
