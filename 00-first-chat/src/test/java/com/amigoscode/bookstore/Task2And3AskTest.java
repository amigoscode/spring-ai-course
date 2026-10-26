package com.amigoscode.bookstore;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.ai.model.chat=none")
@Import(FakeChatModelConfig.class)
class Task2And3AskTest {

	@Autowired
	BookAssistant bookAssistant;

	@Autowired
	FakeChatModel model;

	@Test
	void task2_buildsAChatClient() {
		assertThat(ReflectionTestUtils.getField(bookAssistant, "chatClient")).isNotNull();
	}

	@Test
	void task3_sendsTheQuestionAsAUserMessage() {
		bookAssistant.ask("Do you sell Effective Java?");

		var messages = model.lastPrompt().getInstructions();
		assertThat(messages).hasSize(1);
		assertThat(messages.getFirst().getMessageType()).isEqualTo(MessageType.USER);
		assertThat(messages.getFirst().getText()).isEqualTo("Do you sell Effective Java?");
	}

	@Test
	void task3_returnsTheModelsText() {
		model.reply("Yes, we have 3 copies.");

		assertThat(bookAssistant.ask("Do you sell Effective Java?")).isEqualTo("Yes, we have 3 copies.");
	}

}
