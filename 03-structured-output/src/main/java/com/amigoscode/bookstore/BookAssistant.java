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
import org.springframework.core.ParameterizedTypeReference;
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
		this.chatClient = chatClientBuilder
				.defaultSystem(system -> system
						.text(systemPrompt)
						.param("catalog", catalog.asPromptText()))
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
		return chatClient.prompt()
				.user(user -> user
						.text(recommendPrompt)
						.param("topic", topic)
						.param("maxBooks", maxBooks))
				.call()
				.content();
	}

	/**
	 * Classifies a customer review. Few-shot examples teach the model the exact output format.
	 */
	public Sentiment classify(String review) {
		String answer = chatClient.prompt()
				.system("Classify the sentiment of book reviews. Reply with one word: POSITIVE, NEGATIVE or NEUTRAL.")
				.messages(SENTIMENT_EXAMPLES)
				.user("Review: " + review)
				.options(ChatOptions.builder().temperature(0.0))
				.call()
				.content();
		return Sentiment.valueOf(answer.strip().toUpperCase());
	}

	/**
	 * Recommends one book as a Java object instead of text.
	 */
	public BookRecommendation recommendOne(String topic) {
		// SOLUTION-START task-1
		return chatClient.prompt()
				.user(user -> user.text("Recommend one book about {topic} from the catalog.").param("topic", topic))
				.call()
				.entity(BookRecommendation.class);
		// SOLUTION-ELSE
		// TASK: // TODO task-1: ask for one book about the topic and return it as a BookRecommendation
		// TASK: throw new UnsupportedOperationException("TODO task-1");
		// SOLUTION-END
	}

	/**
	 * Recommends several books as a list.
	 */
	public List<BookRecommendation> recommendMany(String topic, int maxBooks) {
		// SOLUTION-START task-2
		return chatClient.prompt()
				.user(user -> user
						.text("Recommend up to {maxBooks} books about {topic} from the catalog.")
						.param("topic", topic)
						.param("maxBooks", maxBooks))
				.call()
				.entity(new ParameterizedTypeReference<List<BookRecommendation>>() {
				});
		// SOLUTION-ELSE
		// TASK: // TODO task-2: ask for up to maxBooks books and return them as a List<BookRecommendation>
		// TASK: throw new UnsupportedOperationException("TODO task-2");
		// SOLUTION-END
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
