package com.amigoscode.bookstore;

import com.amigoscode.bookstore.eval.LlmJudge;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.ai.model.chat=none")
@Import({ FakeChatModelConfig.class, TestcontainersConfig.class })
class Task3JudgeTest {

	@Autowired
	LlmJudge judge;

	@Autowired
	FakeChatModel model;

	@Test
	void gradesWithTheRubric() {
		model.reply("""
				{"score": 2, "grounded": false, "reasoning": "The context says 30 days, not 60."}""");

		var judgement = judge.judge("How long can I return a book?", "You have 60 days.", "...within 30 days of delivery...");

		assertThat(judgement.score()).isEqualTo(2);
		assertThat(judgement.passes()).isFalse();
		var messages = model.lastPrompt().getInstructions();
		assertThat(messages.getFirst().getMessageType()).isEqualTo(MessageType.SYSTEM);
		assertThat(messages.getFirst().getText()).contains("Score from 1 to 5");
		assertThat(model.lastPrompt().getUserMessage().getText()).contains("How long can I return a book?")
			.contains("You have 60 days.")
			.contains("within 30 days of delivery");
	}

}
