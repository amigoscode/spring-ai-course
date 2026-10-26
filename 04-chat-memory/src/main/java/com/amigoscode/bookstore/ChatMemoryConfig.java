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

	// TODO task-2: define a ChatMemory bean that keeps only the last MAX_MESSAGES messages,
	// stored in the ChatMemoryRepository Spring AI gives you

}
