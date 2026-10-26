package com.amigoscode.bookstore;

import com.amigoscode.bookstore.agents.SupportRouter;
import com.amigoscode.bookstore.agents.SupportRouter.Route;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.ai.model.chat=none")
@Import({ FakeChatModelConfig.class, TestcontainersConfig.class })
class Task2RouterTest {

	@Autowired
	SupportRouter router;

	@Autowired
	FakeChatModel model;

	@Test
	void policyQuestionsGoToThePolicyAssistant() {
		model.reply("""
				{"route": "POLICY", "reason": "asks about returns"}""", "You have 30 days.");

		var routed = router.handle("Can I send a book back?");

		assertThat(routed).isEqualTo(new SupportRouter.Routed(Route.POLICY, "You have 30 days."));
		assertThat(model.lastPrompt().getUserMessage().getText()).as("RAG context from the policies")
			.contains("within 30 days of delivery");
	}

	@Test
	void recommendationsGoToTheBookAssistant() {
		model.reply("""
				{"route": "RECOMMENDATION", "reason": "wants a book"}""", "Try Effective Java.");

		var routed = router.handle("I'm learning Java, what should I read?");

		assertThat(routed.route()).isEqualTo(Route.RECOMMENDATION);
		assertThat(routed.answer()).isEqualTo("Try Effective Java.");
	}

	@Test
	void everythingElseGetsAFixedAnswerWithoutASecondCall() {
		model.reply("""
				{"route": "OTHER", "reason": "weather"}""");

		var routed = router.handle("Will it rain tomorrow?");

		assertThat(routed.answer()).isEqualTo("I can help with book recommendations and questions about orders, shipping, returns and payments.");
		assertThat(model.prompts()).as("only the classification call").hasSize(1);
	}

}
