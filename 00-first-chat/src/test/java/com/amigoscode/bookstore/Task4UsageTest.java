package com.amigoscode.bookstore;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.ai.model.chat=none")
@Import(FakeChatModelConfig.class)
class Task4UsageTest {

	@Autowired
	BookAssistant bookAssistant;

	@Autowired
	FakeChatModel model;

	@Test
	void returnsTheTextAndTheTokenUsage() {
		model.reply("We open at 9am.");

		BookAssistant.Answer answer = bookAssistant.askWithUsage("When do you open?");

		assertThat(answer.text()).isEqualTo("We open at 9am.");
		assertThat(answer.promptTokens()).isEqualTo(FakeChatModel.PROMPT_TOKENS);
		assertThat(answer.completionTokens()).isEqualTo(FakeChatModel.COMPLETION_TOKENS);
	}

}
