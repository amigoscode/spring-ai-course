package com.amigoscode.bookstore;

import java.util.UUID;

import com.amigoscode.bookstore.policy.PolicyAssistant;
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
	PolicyAssistant policyAssistant;

	@Autowired
	BookAssistant bookAssistant;

	@Test
	void answersFromTheDocuments() {
		var result = policyAssistant.answerWithSources("How many days do I have to return a printed book?");

		assertThat(result.answer()).contains("30");
		assertThat(result.sources()).contains("returns.md");
	}

	@Test
	void combinesFactsFromTwoDocuments() {
		String answer = policyAssistant.answerAdvanced("Do you ship to Brazil, and can I pay with Bitcoin?").toLowerCase();

		// shipping.md: Europe only, e-books worldwide. payments.md: the accepted payment methods.
		assertThat(answer).contains("e-book").containsAnyOf("paypal", "visa", "gift card");
	}

	@Test
	void theAssistantLooksUpPoliciesWhenNeeded() {
		String answer = bookAssistant.assist("ana", UUID.randomUUID().toString(),
				"How much does express delivery cost?");

		assertThat(answer).contains("9.90");
	}

}
