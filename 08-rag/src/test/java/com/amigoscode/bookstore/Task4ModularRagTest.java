package com.amigoscode.bookstore;

import com.amigoscode.bookstore.policy.PolicyAssistant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.ai.model.chat=none")
@Import({ FakeChatModelConfig.class, TestcontainersConfig.class })
class Task4ModularRagTest {

	@Autowired
	PolicyAssistant policyAssistant;

	@Autowired
	FakeChatModel model;

	@Test
	void rewritesTheQuestionThenAnswersFromTheDocuments() {
		model.reply("how long is express delivery", "Express takes 2 business days.");

		String answer = policyAssistant.answerAdvanced(
				"hi!! my sister's birthday is on friday, i'm so stressed, how fast can you get a book to her??");

		assertThat(answer).isEqualTo("Express takes 2 business days.");
		assertThat(model.prompts()).as("one call to rewrite the query, one to answer").hasSize(2);
		assertThat(model.prompts().getFirst().getUserMessage().getText()).contains("rewrite");
		assertThat(model.lastPrompt().getUserMessage().getText()).contains("Express delivery takes 2 business days");
	}

	@Test
	void saysItCantAnswerWhenNothingRelevantIsFound() {
		model.reply("chocolate cake recipe", "Sorry, I can only help with the shop.");

		policyAssistant.answerAdvanced("What's a good chocolate cake recipe?");

		assertThat(model.lastPrompt().getUserMessage().getText()).contains("outside your knowledge base");
	}

}
