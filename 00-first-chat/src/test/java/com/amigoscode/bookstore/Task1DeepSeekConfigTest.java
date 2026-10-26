package com.amigoscode.bookstore;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "DEEPSEEK_API_KEY=sk-test-key")
class Task1DeepSeekConfigTest {

	@Autowired
	Environment environment;

	@Autowired
	ChatModel chatModel;

	@Test
	void apiKeyComesFromTheEnvironmentVariable() {
		assertThat(environment.getProperty("spring.ai.deepseek.api-key")).isEqualTo("sk-test-key");
	}

	@Test
	void usesDeepSeekV4Flash() {
		assertThat(environment.getProperty("spring.ai.deepseek.chat.model")).isEqualTo("deepseek-v4-flash");
		assertThat(chatModel).isInstanceOf(DeepSeekChatModel.class);
	}

}
