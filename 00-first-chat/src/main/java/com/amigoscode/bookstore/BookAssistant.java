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
		// SOLUTION-START task-2
		this.chatClient = chatClientBuilder.build();
		// SOLUTION-ELSE
		// TASK: // TODO task-2: build a ChatClient from the builder Spring AI gives you
		// TASK: this.chatClient = null;
		// SOLUTION-END
	}

	/**
	 * Sends the question to the model and returns its text answer.
	 */
	public String ask(String question) {
		// SOLUTION-START task-3
		return chatClient.prompt()
				.user(question)
				.call()
				.content();
		// SOLUTION-ELSE
		// TASK: // TODO task-3: send the question as a user message and return the answer text
		// TASK: throw new UnsupportedOperationException("TODO task-3");
		// SOLUTION-END
	}

	/**
	 * Same as {@link #ask(String)}, but also reports how many tokens the call used.
	 */
	public Answer askWithUsage(String question) {
		// SOLUTION-START task-4
		ChatResponse response = chatClient.prompt()
				.user(question)
				.call()
				.chatResponse();

		String text = response.getResult().getOutput().getText();
		Usage usage = response.getMetadata().getUsage();
		log.info("prompt tokens={} completion tokens={} total={}",
				usage.getPromptTokens(), usage.getCompletionTokens(), usage.getTotalTokens());

		return new Answer(text, usage.getPromptTokens(), usage.getCompletionTokens());
		// SOLUTION-ELSE
		// TASK: // TODO task-4: ask for the full ChatResponse instead of just the content,
		// TASK: // then read the text and the token usage from it
		// TASK: throw new UnsupportedOperationException("TODO task-4");
		// SOLUTION-END
	}

	public record Answer(String text, Integer promptTokens, Integer completionTokens) {
	}

}
