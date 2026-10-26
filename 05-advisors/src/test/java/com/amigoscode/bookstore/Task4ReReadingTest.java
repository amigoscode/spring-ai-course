package com.amigoscode.bookstore;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.ai.model.chat=none")
@Import({ FakeChatModelConfig.class, TestcontainersConfig.class })
class Task4ReReadingTest {

	@Autowired
	BookAssistant bookAssistant;

	@Autowired
	FakeChatModel model;

	@Test
	void theQuestionIsSentTwice() {
		model.reply("ok");

		bookAssistant.answerCarefully("Which is older, Refactoring or Clean Code?");

		assertThat(model.lastPrompt().getUserMessage().getText()).isEqualTo("""
				Which is older, Refactoring or Clean Code?
				Read the question again: Which is older, Refactoring or Clean Code?""");
	}

	@Test
	void otherRequestsAreUnchanged() {
		model.reply("ok");

		bookAssistant.ask("Hi");

		assertThat(model.lastPrompt().getUserMessage().getText()).isEqualTo("Hi");
	}

}
