package com.amigoscode.bookstore;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Calls the real DeepSeek API. Costs a fraction of a cent.
 * Skipped by default. Run with: ./mvnw test -Plive
 */
@Tag("live")
@SpringBootTest
class LiveDeepSeekTest {

	@Autowired
	BookAssistant bookAssistant;

	@Test
	void streamsMoreThanOnePiece() {
		var pieces = bookAssistant.stream("Name three classic programming books.")
			.collectList()
			.block(Duration.ofSeconds(60));

		assertThat(pieces).hasSizeGreaterThan(1);
	}

	@Test
	void remembersOnlyWhatYouSendBack() {
		List<Message> history = List.of(new UserMessage("My name is Ana."), new AssistantMessage("Hi Ana!"));

		assertThat(bookAssistant.followUp(history, "What is my name? Reply with the name only.")).contains("Ana");
	}

}
