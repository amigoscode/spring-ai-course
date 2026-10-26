package com.amigoscode.bookstore;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.ai.model.chat=none")
@Import(FakeChatModelConfig.class)
class Task3ExtractOrderTest {

	static final String EMAIL = "Hi! Could I get 2 copies of effective java sent to Lisbon? Thanks, ana@example.com";

	static final String ORDER_JSON = """
			{"bookTitle": "Effective Java", "quantity": 2, "deliveryCity": "Lisbon", "customerEmail": "ana@example.com"}
			""";

	@Autowired
	OrderExtractor orderExtractor;

	@Autowired
	FakeChatModel model;

	@Test
	void extractsTheOrder() {
		model.reply(ORDER_JSON);

		OrderRequest order = orderExtractor.extract(EMAIL);

		assertThat(order).isEqualTo(new OrderRequest("Effective Java", 2, "Lisbon", "ana@example.com"));
		assertThat(model.lastPrompt().getUserMessage().getText()).contains(EMAIL);
	}

	@Test
	void retriesWhenTheAnswerIsNotValidJson() {
		model.reply("Sure! The customer wants Effective Java, 2 copies, to Lisbon.", ORDER_JSON);

		OrderRequest order = orderExtractor.extract(EMAIL);

		assertThat(model.prompts()).as("one failed attempt, one retry").hasSize(2);
		assertThat(order.bookTitle()).isEqualTo("Effective Java");
	}

}
