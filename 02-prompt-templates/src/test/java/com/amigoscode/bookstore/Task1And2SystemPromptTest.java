package com.amigoscode.bookstore;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.ai.model.chat=none")
@Import(FakeChatModelConfig.class)
class Task1And2SystemPromptTest {

	@Autowired
	BookAssistant bookAssistant;

	@Autowired
	BookCatalog catalog;

	@Autowired
	FakeChatModel model;

	@Test
	void task1_systemPromptIsLoadedFromAFile() {
		assertThat(new ClassPathResource("prompts/system.st").exists())
			.as("src/main/resources/prompts/system.st exists")
			.isTrue();

		bookAssistant.ask("Hi");

		var system = model.lastPrompt().getInstructions().getFirst();
		assertThat(system.getMessageType()).isEqualTo(MessageType.SYSTEM);
		assertThat(system.getText()).contains("Amigos Books");
	}

	@Test
	void task2_catalogIsInTheSystemPrompt() {
		bookAssistant.ask("Hi");

		String system = model.lastPrompt().getSystemMessage().getText();
		catalog.findAll().forEach(book -> assertThat(system).contains(book.title()));
		assertThat(system).doesNotContain("{catalog}");
	}

}
