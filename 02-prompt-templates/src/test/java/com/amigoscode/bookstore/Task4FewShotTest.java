package com.amigoscode.bookstore;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.ai.model.chat=none")
@Import(FakeChatModelConfig.class)
class Task4FewShotTest {

	@Autowired
	BookAssistant bookAssistant;

	@Autowired
	FakeChatModel model;

	@Test
	void sendsTheExamplesBeforeTheReview() {
		model.reply("NEGATIVE");

		bookAssistant.classify("Too long and too boring.");

		var messages = model.lastPrompt().getInstructions();
		assertThat(messages.stream().map(Message::getMessageType)).containsExactly(MessageType.SYSTEM,
				MessageType.USER, MessageType.ASSISTANT, MessageType.USER, MessageType.ASSISTANT, MessageType.USER,
				MessageType.ASSISTANT, MessageType.USER);
		assertThat(messages.getFirst().getText()).contains("POSITIVE").contains("NEGATIVE").contains("NEUTRAL");
		assertThat(messages.getLast().getText()).contains("Too long and too boring.");
	}

	@Test
	void turnsTheAnswerIntoASentiment() {
		model.reply(" positive\n");

		assertThat(bookAssistant.classify("Loved it")).isEqualTo(Sentiment.POSITIVE);
	}

	@Test
	void classifiesWithTemperatureZero() {
		model.reply("NEUTRAL");

		bookAssistant.classify("It's a book.");

		assertThat(model.lastPrompt().getOptions().getTemperature()).isZero();
	}

}
