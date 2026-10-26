package com.amigoscode.bookstore;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.ai.model.chat=none")
@Import({ FakeChatModelConfig.class, TestcontainersConfig.class })
class Task2SafeGuardTest {

	@Autowired
	BookAssistant bookAssistant;

	@Autowired
	FakeChatModel model;

	@Test
	void sensitiveDataNeverReachesTheModel() {
		model.reply("ok");

		String answer = bookAssistant.ask("My card was declined, my credit card number is 4111 1111 1111 1111");

		assertThat(answer).isEqualTo(BookAssistant.SENSITIVE_REFUSAL);
		assertThat(model.prompts()).as("the model was never called").isEmpty();
	}

	@Test
	void normalQuestionsGoThrough() {
		model.reply("We have 7 copies.");

		assertThat(bookAssistant.ask("How many copies of Effective Java do you have?")).isEqualTo("We have 7 copies.");
	}

}
