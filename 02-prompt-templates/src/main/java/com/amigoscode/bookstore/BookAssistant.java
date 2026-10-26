package com.amigoscode.bookstore;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * The assistant that answers questions for Amigos Books.
 */
@Service
public class BookAssistant {

	private static final Logger log = LoggerFactory.getLogger(BookAssistant.class);

	/**
	 * Examples that show the model exactly what a good answer looks like.
	 */
	static final List<Message> SENTIMENT_EXAMPLES = List.of(
			new UserMessage("Review: Changed how I write Java. Every chapter is gold."),
			new AssistantMessage("POSITIVE"),
			new UserMessage("Review: The binding fell apart after a week and half the examples don't compile."),
			new AssistantMessage("NEGATIVE"),
			new UserMessage("Review: It arrived on Tuesday. I haven't started it yet."),
			new AssistantMessage("NEUTRAL"));

	private final ChatClient chatClient;

	private final Resource recommendPrompt;

	public BookAssistant(ChatClient.Builder chatClientBuilder, BookCatalog catalog,
			@Value("classpath:prompts/system.st") Resource systemPrompt,
			@Value("classpath:prompts/recommend.st") Resource recommendPrompt) {
		this.recommendPrompt = recommendPrompt;
		// TODO task-1: load the system prompt from prompts/system.st instead of a Java string
		// TODO task-2: fill the {catalog} placeholder in that file with catalog.asPromptText()
		this.chatClient = chatClientBuilder
				.defaultSystem("You are the assistant of Amigos Books, an online bookstore for software developers.")
				.defaultOptions(ChatOptions.builder()
						.temperature(0.3)
						.maxTokens(500))
				.build();
	}

	public String ask(String question) {
		return chatClient.prompt()
				.user(question)
				.call()
				.content();
	}

	/**
	 * Recommends books on a topic, using the prompts/recommend.st template.
	 */
	public String recommend(String topic, int maxBooks) {
		// TODO task-3: render prompts/recommend.st with the topic and maxBooks, and send it as the user message
		throw new UnsupportedOperationException("TODO task-3");
	}

	/**
	 * Classifies a customer review. Few-shot examples teach the model the exact output format.
	 */
	public Sentiment classify(String review) {
		// TODO task-4: replace the system prompt for this request, send SENTIMENT_EXAMPLES,
		// then the review, and turn the one word answer into a Sentiment
		throw new UnsupportedOperationException("TODO task-4");
	}

	public Flux<String> stream(String question) {
		return chatClient.prompt()
				.user(question)
				.stream()
				.content();
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
