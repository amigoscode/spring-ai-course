package com.amigoscode.bookstore;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * The assistant that answers questions for Amigos Books.
 */
@Service
public class BookAssistant {

	private static final Logger log = LoggerFactory.getLogger(BookAssistant.class);

	static final String SYSTEM_PROMPT = """
			You are the assistant of Amigos Books, an online bookstore for software developers.
			Answer in a friendly, concise way, in at most three sentences.
			If a question is not about books or reading, politely say you can only help with books.
			""";

	private final ChatClient chatClient;

	public BookAssistant(ChatClient.Builder chatClientBuilder) {
		// TODO task-1: give every request the SYSTEM_PROMPT
		// TODO task-2: default every request to temperature 0.3 and at most 500 tokens
		this.chatClient = chatClientBuilder.build();
	}

	public String ask(String question) {
		return chatClient.prompt()
				.user(question)
				.call()
				.content();
	}

	/**
	 * Writes a short marketing blurb. Creative writing wants a higher temperature
	 * than answering questions, so this call overrides the default.
	 */
	public String writeBlurb(String bookTitle) {
		// TODO task-3: ask for a two sentence marketing blurb for the book,
		// with temperature 1.2 for this request only
		throw new UnsupportedOperationException("TODO task-3");
	}

	/**
	 * Streams the answer piece by piece instead of waiting for the whole text.
	 */
	public Flux<String> stream(String question) {
		// TODO task-4: stream the answer instead of calling the model once
		return Flux.error(new UnsupportedOperationException("TODO task-4"));
	}

	/**
	 * The model remembers nothing between calls. To continue a conversation,
	 * you send the earlier messages again with the new question.
	 */
	public String followUp(List<Message> history, String question) {
		// TODO task-5: send the earlier messages, then the new question
		throw new UnsupportedOperationException("TODO task-5");
	}

	public Answer askWithUsage(String question) {
		ChatResponse response = chatClient.prompt()
				.user(question)
				.call()
				.chatResponse();

		String text = response.getResult().getOutput().getText();
		Usage usage = response.getMetadata().getUsage();
		log.info("prompt tokens={} completion tokens={} total={}",
				usage.getPromptTokens(), usage.getCompletionTokens(), usage.getTotalTokens());

		return new Answer(text, usage.getPromptTokens(), usage.getCompletionTokens());
	}

	public record Answer(String text, Integer promptTokens, Integer completionTokens) {
	}

}
