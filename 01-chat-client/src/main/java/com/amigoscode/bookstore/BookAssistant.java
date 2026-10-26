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
		// SOLUTION-START task-1 task-2
		this.chatClient = chatClientBuilder
				.defaultSystem(SYSTEM_PROMPT)
				.defaultOptions(ChatOptions.builder()
						.temperature(0.3)
						.maxTokens(500))
				.build();
		// SOLUTION-ELSE
		// TASK: // TODO task-1: give every request the SYSTEM_PROMPT
		// TASK: // TODO task-2: default every request to temperature 0.3 and at most 500 tokens
		// TASK: this.chatClient = chatClientBuilder.build();
		// SOLUTION-END
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
		// SOLUTION-START task-3
		return chatClient.prompt()
				.user("Write a two sentence marketing blurb for the book: " + bookTitle)
				.options(ChatOptions.builder().temperature(1.2))
				.call()
				.content();
		// SOLUTION-ELSE
		// TASK: // TODO task-3: ask for a two sentence marketing blurb for the book,
		// TASK: // with temperature 1.2 for this request only
		// TASK: throw new UnsupportedOperationException("TODO task-3");
		// SOLUTION-END
	}

	/**
	 * Streams the answer piece by piece instead of waiting for the whole text.
	 */
	public Flux<String> stream(String question) {
		// SOLUTION-START task-4
		return chatClient.prompt()
				.user(question)
				.stream()
				.content();
		// SOLUTION-ELSE
		// TASK: // TODO task-4: stream the answer instead of calling the model once
		// TASK: return Flux.error(new UnsupportedOperationException("TODO task-4"));
		// SOLUTION-END
	}

	/**
	 * The model remembers nothing between calls. To continue a conversation,
	 * you send the earlier messages again with the new question.
	 */
	public String followUp(List<Message> history, String question) {
		// SOLUTION-START task-5
		return chatClient.prompt()
				.messages(history)
				.user(question)
				.call()
				.content();
		// SOLUTION-ELSE
		// TASK: // TODO task-5: send the earlier messages, then the new question
		// TASK: throw new UnsupportedOperationException("TODO task-5");
		// SOLUTION-END
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
