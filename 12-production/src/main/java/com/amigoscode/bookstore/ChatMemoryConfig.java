package com.amigoscode.bookstore;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatMemoryConfig {

	/**
	 * How many messages (user and assistant) the model sees from the past.
	 */
	public static final int MAX_MESSAGES = 10;

	@Bean
	ChatMemory chatMemory(ChatMemoryRepository chatMemoryRepository) {
		return MessageWindowChatMemory.builder()
			.chatMemoryRepository(chatMemoryRepository)
			.maxMessages(MAX_MESSAGES)
			.build();
	}

}
