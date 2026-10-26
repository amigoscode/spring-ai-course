package com.amigoscode.bookstore;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.ai.model.chat=none")
@Import({ FakeChatModelConfig.class, TestcontainersConfig.class })
class Task1And2MemoryTest {

	@Autowired
	BookAssistant bookAssistant;

	@Autowired
	FakeChatModel model;

	@Test
	void task1_theSecondMessageSeesTheFirst() {
		String conversation = UUID.randomUUID().toString();
		model.reply("Nice to meet you, Ana!");

		bookAssistant.chat(conversation, "My name is Ana.");
		bookAssistant.chat(conversation, "What's my name?");

		var texts = model.lastPrompt().getInstructions().stream().map(Message::getText).toList();
		assertThat(texts).containsSubsequence("My name is Ana.", "Nice to meet you, Ana!", "What's my name?");
		assertThat(model.lastPrompt().getInstructions().getFirst().getMessageType()).isEqualTo(MessageType.SYSTEM);
	}

	@Test
	void task1_conversationsDontLeakIntoEachOther() {
		model.reply("ok");

		bookAssistant.chat(UUID.randomUUID().toString(), "My name is Ana.");
		bookAssistant.chat(UUID.randomUUID().toString(), "What's my name?");

		var texts = model.lastPrompt().getInstructions().stream().map(Message::getText).toList();
		assertThat(texts).doesNotContain("My name is Ana.");
	}

	@Test
	void task2_onlyTheLastMessagesAreSent() {
		String conversation = UUID.randomUUID().toString();
		model.reply("ok");

		for (int i = 1; i <= 8; i++) {
			bookAssistant.chat(conversation, "Message " + i);
		}

		var history = model.lastPrompt().getInstructions().stream()
			.filter(m -> m.getMessageType() != MessageType.SYSTEM)
			.toList();
		// The last MAX_MESSAGES messages from memory, plus the new question.
		// 7 earlier turns are 14 messages, so the first 2 turns were dropped.
		assertThat(history).hasSize(ChatMemoryConfig.MAX_MESSAGES + 1);
		assertThat(history.getFirst().getText()).as("the oldest messages were dropped").isEqualTo("Message 3");
		assertThat(history.getLast().getText()).isEqualTo("Message 8");
	}

}
