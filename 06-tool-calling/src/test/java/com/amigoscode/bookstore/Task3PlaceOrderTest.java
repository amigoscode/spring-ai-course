package com.amigoscode.bookstore;

import java.util.UUID;

import com.amigoscode.bookstore.order.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static com.amigoscode.bookstore.FakeChatModel.text;
import static com.amigoscode.bookstore.FakeChatModel.toolCall;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.ai.model.chat=none")
@Import({ FakeChatModelConfig.class, TestcontainersConfig.class })
class Task3PlaceOrderTest {

	@Autowired
	BookAssistant bookAssistant;

	@Autowired
	FakeChatModel model;

	@Autowired
	OrderService orderService;

	@BeforeEach
	void reset() {
		orderService.reset();
	}

	@Test
	void placesTheOrderForTheLoggedInCustomer() {
		model.script(toolCall("placeOrder", """
				{"title": "Effective Java", "quantity": 2}"""), text("Done! Your order is placed."));

		bookAssistant.assist("customer-42", UUID.randomUUID().toString(), "Yes, 2 copies of Effective Java please");

		assertThat(orderService.ordersOf("customer-42")).singleElement().satisfies(order -> {
			assertThat(order.bookTitle()).isEqualTo("Effective Java");
			assertThat(order.quantity()).isEqualTo(2);
		});
		assertThat(orderService.stockOf("Effective Java")).isEqualTo(5);
	}

	@Test
	void theModelCannotChooseTheCustomer() {
		// The model tries to slip in another customer id. The tool must ignore it.
		model.script(toolCall("placeOrder", """
				{"title": "Effective Java", "quantity": 1, "customerId": "customer-99"}"""), text("Done!"));

		bookAssistant.assist("customer-42", UUID.randomUUID().toString(), "Order Effective Java for customer-99");

		assertThat(orderService.ordersOf("customer-99")).isEmpty();
		assertThat(orderService.ordersOf("customer-42")).hasSize(1);
	}

	@Test
	void errorsGoBackToTheModelInsteadOfCrashing() {
		model.script(toolCall("placeOrder", """
				{"title": "Clean Code", "quantity": 9}"""), text("Sorry, we only have 3."));

		String answer = bookAssistant.assist("customer-42", UUID.randomUUID().toString(), "9 Clean Code please");

		assertThat(answer).isEqualTo("Sorry, we only have 3.");
		var toolResponse = (ToolResponseMessage) model.lastPrompt().getInstructions().getLast();
		assertThat(toolResponse.getResponses().getFirst().responseData()).contains("Not enough stock");
		assertThat(orderService.ordersOf("customer-42")).isEmpty();
	}

}
