package com.amigoscode.bookstore;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.ai.model.chat=none")
@Import(FakeChatModelConfig.class)
class Task5FollowUpTest {

	@Autowired
	BookAssistant bookAssistant;

	@Autowired
	FakeChatModel model;

	@Test
	void sendsTheHistoryBeforeTheNewQuestion() {
		List<Message> history = List.of(
				new UserMessage("My name is Ana and I like Kotlin."),
				new AssistantMessage("Nice to meet you, Ana!"));

		bookAssistant.followUp(history, "What's my name?");

		var types = model.lastPrompt().getInstructions().stream().map(Message::getMessageType).toList();
		assertThat(types).containsExactly(MessageType.SYSTEM, MessageType.USER, MessageType.ASSISTANT,
				MessageType.USER);
		assertThat(model.lastPrompt().getInstructions().getLast().getText()).isEqualTo("What's my name?");
	}

}
