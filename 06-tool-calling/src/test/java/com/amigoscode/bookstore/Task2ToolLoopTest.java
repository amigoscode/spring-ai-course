package com.amigoscode.bookstore;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static com.amigoscode.bookstore.FakeChatModel.text;
import static com.amigoscode.bookstore.FakeChatModel.toolCall;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.ai.model.chat=none")
@Import({ FakeChatModelConfig.class, TestcontainersConfig.class })
class Task2ToolLoopTest {

	@Autowired
	BookAssistant bookAssistant;

	@Autowired
	FakeChatModel model;

	@Test
	void theModelCanSeeTheTools() {
		model.reply("Hello!");

		bookAssistant.assist("customer-1", UUID.randomUUID().toString(), "Hi");

		var options = (ToolCallingChatOptions) model.lastPrompt().getOptions();
		assertThat(options.getToolCallbacks()).extracting(c -> c.getToolDefinition().name())
			.contains("checkStock", "findBooksByTopic");
	}

	@Test
	void springAiRunsTheToolAndSendsTheResultBack() {
		model.script(toolCall("checkStock", """
				{"title": "Clean Code"}"""), text("Yes! We have 3 copies of Clean Code."));

		String answer = bookAssistant.assist("customer-1", UUID.randomUUID().toString(), "Is Clean Code in stock?");

		assertThat(answer).isEqualTo("Yes! We have 3 copies of Clean Code.");
		assertThat(model.prompts()).as("one call that asked for the tool, one with the tool result").hasSize(2);

		var lastMessage = model.lastPrompt().getInstructions().getLast();
		assertThat(lastMessage.getMessageType()).isEqualTo(MessageType.TOOL);
		var toolResponse = ((ToolResponseMessage) lastMessage).getResponses().getFirst();
		assertThat(toolResponse.name()).isEqualTo("checkStock");
		assertThat(toolResponse.responseData()).contains("\"copies\":3");
	}

	@Test
	void conversationsBelongToOneCustomer() {
		model.reply("ok");

		bookAssistant.assist("ana", "shared-id", "My address is Rua Augusta 1, Lisbon");
		bookAssistant.assist("bob", "shared-id", "What's my address?");

		var texts = model.lastPrompt().getInstructions().stream().map(m -> m.getText()).toList();
		assertThat(texts).as("bob must not see ana's messages").doesNotContain("My address is Rua Augusta 1, Lisbon");
	}

}
