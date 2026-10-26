package com.amigoscode.bookstore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Service;

/**
 * The assistant that answers questions for Amigos Books.
 * Everything in this module goes through the {@link ChatClient}.
 */
@Service
public class BookAssistant {

	private static final Logger log = LoggerFactory.getLogger(BookAssistant.class);

	private final ChatClient chatClient;

	public BookAssistant(ChatClient.Builder chatClientBuilder) {
		// TODO task-2: build a ChatClient from the builder Spring AI gives you
		this.chatClient = null;
	}

	/**
	 * Sends the question to the model and returns its text answer.
	 */
	public String ask(String question) {
		// TODO task-3: send the question as a user message and return the answer text
		throw new UnsupportedOperationException("TODO task-3");
	}

	/**
	 * Same as {@link #ask(String)}, but also reports how many tokens the call used.
	 */
	public Answer askWithUsage(String question) {
		// TODO task-4: ask for the full ChatResponse instead of just the content,
		// then read the text and the token usage from it
		throw new UnsupportedOperationException("TODO task-4");
	}

	public record Answer(String text, Integer promptTokens, Integer completionTokens) {
	}

}
