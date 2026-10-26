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
class Task4OrderStatusTest {

	@Autowired
	BookAssistant bookAssistant;

	@Autowired
	FakeChatModel model;

	@Autowired
	OrderService orderService;

	String orderId;

	@BeforeEach
	void anOrderOfAna() {
		orderService.reset();
		orderId = orderService.placeOrder("ana", "Refactoring", 1).id();
	}

	@Test
	void customersSeeTheirOwnOrders() {
		model.script(toolCall("orderStatus", "{\"orderId\": \"" + orderId + "\"}"), text("It's on its way."));

		bookAssistant.assist("ana", UUID.randomUUID().toString(), "Where is " + orderId + "?");

		assertThat(lastToolResult()).contains(orderId).contains("Refactoring");
	}

	@Test
	void customersCannotSeeSomeoneElsesOrder() {
		model.script(toolCall("orderStatus", "{\"orderId\": \"" + orderId + "\"}"), text("I can't find it."));

		bookAssistant.assist("bob", UUID.randomUUID().toString(), "Where is " + orderId + "?");

		assertThat(lastToolResult()).doesNotContain("Refactoring").contains("No order");
	}

	private String lastToolResult() {
		var toolResponse = (ToolResponseMessage) model.lastPrompt().getInstructions().getLast();
		return toolResponse.getResponses().getFirst().responseData();
	}

}
