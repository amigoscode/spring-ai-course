package com.amigoscode.bookstore;

import java.math.BigDecimal;

import com.amigoscode.bookstore.gifts.GiftFinder;
import com.amigoscode.bookstore.gifts.GiftRequest;
import com.amigoscode.bookstore.ops.RateLimiter;
import com.amigoscode.bookstore.order.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;

/**
 * The offline part of the acceptance criteria. They don't depend on how you design the
 * prompts, so the fake model is only used to check you don't call it when you shouldn't.
 */
@SpringBootTest(properties = "spring.ai.model.chat=none")
@AutoConfigureMockMvc
@Import({ FakeChatModelConfig.class, TestcontainersConfig.class })
class CapstoneAcceptanceTest {

	@Autowired
	MockMvcTester mvc;

	@Autowired
	FakeChatModel model;

	@Autowired
	GiftFinder giftFinder;

	@Autowired
	BookCatalog catalog;

	@Autowired
	OrderService orderService;

	@Autowired
	RateLimiter rateLimiter;

	@BeforeEach
	void reset() {
		rateLimiter.reset();
		orderService.reset();
		model.reply("ok");
	}

	@Test
	void customersMustLogIn() {
		assertThat(mvc.post().uri("/api/gifts").contentType(MediaType.APPLICATION_JSON).content("""
				{"recipient": "my sister", "budget": 50}""")).hasStatus(HttpStatus.UNAUTHORIZED);
	}

	@Test
	void theRequestIsValidated() {
		assertThat(post("""
				{"recipient": "", "budget": 50}""")).as("blank recipient").hasStatus(HttpStatus.BAD_REQUEST);
		assertThat(post("""
				{"recipient": "my sister", "budget": 5}""")).as("budget under $10").hasStatus(HttpStatus.BAD_REQUEST);
		assertThat(post("""
				{"recipient": "my sister", "budget": 5000}""")).as("budget over $500").hasStatus(HttpStatus.BAD_REQUEST);
		assertThat(post("""
				{"recipient": "%s", "budget": 50}""".formatted("x".repeat(501)))).as("recipient too long")
			.hasStatus(HttpStatus.BAD_REQUEST);
		assertThat(model.prompts()).as("invalid requests never reach the model").isEmpty();
	}

	@Test
	void whenNothingFitsTheBudgetTheModelIsNotCalled() {
		// The cheapest book costs $33.00.
		var request = new GiftRequest("my sister, she loves testing", "birthday", new BigDecimal("20.00"));

		var result = giftFinder.find(request);

		assertThat(result.suggestions()).isEmpty();
		assertThat(result.message()).isNotBlank();
		assertThat(model.prompts()).as("no point paying for a model call").isEmpty();
		GiftRules.check(result, request, catalog, orderService);
	}

	@Test
	void theGiftEndpointIsRateLimited() {
		for (int i = 0; i < RateLimiter.MAX_REQUESTS; i++) {
			post("""
					{"recipient": "my sister", "budget": 20}""");
		}
		assertThat(post("""
				{"recipient": "my sister", "budget": 20}""")).hasStatus(HttpStatus.TOO_MANY_REQUESTS);
	}

	private org.springframework.test.web.servlet.assertj.MvcTestResult post(String json) {
		return mvc.post()
			.uri("/api/gifts")
			.with(httpBasic("ana", "ana-password"))
			.contentType(MediaType.APPLICATION_JSON)
			.content(json)
			.exchange();
	}

}
