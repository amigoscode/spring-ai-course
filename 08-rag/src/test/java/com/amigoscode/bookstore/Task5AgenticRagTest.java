package com.amigoscode.bookstore;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static com.amigoscode.bookstore.FakeChatModel.text;
import static com.amigoscode.bookstore.FakeChatModel.toolCall;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.ai.model.chat=none")
@Import({ FakeChatModelConfig.class, TestcontainersConfig.class })
class Task5AgenticRagTest {

	@Autowired
	BookAssistant bookAssistant;

	@Autowired
	FakeChatModel model;

	@Test
	void theAssistantCanSearchThePoliciesItself() {
		model.script(toolCall("searchShopPolicies", """
				{"question": "loyalty points voucher"}"""), text("100 points get you a $5 voucher."));

		bookAssistant.assist("ana", UUID.randomUUID().toString(), "What are my points worth?");

		var toolResponse = (ToolResponseMessage) model.lastPrompt().getInstructions().getLast();
		assertThat(toolResponse.getResponses().getFirst().responseData()).contains("100 points");
	}

}
