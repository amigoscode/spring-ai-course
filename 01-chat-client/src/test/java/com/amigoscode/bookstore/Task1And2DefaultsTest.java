package com.amigoscode.bookstore;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.ai.model.chat=none")
@Import(FakeChatModelConfig.class)
class Task1And2DefaultsTest {

	@Autowired
	BookAssistant bookAssistant;

	@Autowired
	FakeChatModel model;

	@Test
	void task1_everyRequestStartsWithTheSystemPrompt() {
		bookAssistant.ask("Hi");

		var messages = model.lastPrompt().getInstructions();
		assertThat(messages.getFirst().getMessageType()).isEqualTo(MessageType.SYSTEM);
		assertThat(messages.getFirst().getText()).isEqualTo(BookAssistant.SYSTEM_PROMPT);
		assertThat(messages.getLast().getText()).isEqualTo("Hi");
	}

	@Test
	void task2_defaultOptionsAreSent() {
		bookAssistant.ask("Hi");

		var options = model.lastPrompt().getOptions();
		assertThat(options.getTemperature()).isEqualTo(0.3);
		assertThat(options.getMaxTokens()).isEqualTo(500);
	}

}
