package com.amigoscode.bookstore;

import com.amigoscode.bookstore.ops.RateLimiter;
import com.amigoscode.bookstore.order.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static com.amigoscode.bookstore.FakeChatModel.text;
import static com.amigoscode.bookstore.FakeChatModel.toolCall;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;

@SpringBootTest(properties = "spring.ai.model.chat=none")
@AutoConfigureMockMvc
@Import({ FakeChatModelConfig.class, TestcontainersConfig.class })
class Task2OwnershipTest {

	@Autowired
	MockMvcTester mvc;

	@Autowired
	FakeChatModel model;

	@Autowired
	OrderService orderService;

	@Autowired
	RateLimiter rateLimiter;

	@BeforeEach
	void reset() {
		rateLimiter.reset();
		orderService.reset();
	}

	@Test
	void theAssistantActsForTheLoggedInCustomerNotTheHeader() {
		model.script(toolCall("placeOrder", """
				{"title": "Refactoring", "quantity": 1}"""), text("Done!"));

		assertThat(mvc.post().uri("/api/assistant/c1")
			.with(httpBasic("ana", "ana-password"))
			.header("X-Customer-Id", "bob")
			.contentType(MediaType.TEXT_PLAIN)
			.content("Yes, order 1 Refactoring")).hasStatusOk();

		assertThat(orderService.ordersOf("ana")).hasSize(1);
		assertThat(orderService.ordersOf("bob")).isEmpty();
	}

	@Test
	void nobodyCanReadSomeoneElsesConversation() {
		model.reply("Noted!");
		mvc.post().uri("/api/conversations/shared").with(httpBasic("ana", "ana-password"))
			.contentType(MediaType.TEXT_PLAIN).content("My address is Rua Augusta 1").exchange();

		assertThat(mvc.get().uri("/api/conversations/shared").with(httpBasic("bob", "bob-password")))
			.bodyJson().isEqualTo("[]");
		assertThat(mvc.get().uri("/api/conversations/shared").with(httpBasic("ana", "ana-password")))
			.bodyText().contains("Rua Augusta 1");
	}

	@Test
	void nobodyCanDeleteSomeoneElsesConversation() {
		model.reply("Noted!");
		mvc.post().uri("/api/conversations/mine").with(httpBasic("ana", "ana-password"))
			.contentType(MediaType.TEXT_PLAIN).content("Remember: I like Kotlin").exchange();

		mvc.delete().uri("/api/conversations/mine").with(httpBasic("bob", "bob-password")).exchange();

		assertThat(mvc.get().uri("/api/conversations/mine").with(httpBasic("ana", "ana-password")))
			.bodyText().contains("I like Kotlin");
	}

}
