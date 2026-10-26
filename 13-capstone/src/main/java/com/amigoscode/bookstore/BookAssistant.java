package com.amigoscode.bookstore;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import com.amigoscode.bookstore.advisor.PromptInjectionGuardAdvisor;
import com.amigoscode.bookstore.advisor.ReReadingAdvisor;
import com.amigoscode.bookstore.advisor.TokenUsageAdvisor;
import com.amigoscode.bookstore.advisor.TokenUsageTracker;
import com.amigoscode.bookstore.tools.BookstoreTools;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
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

	/**
	 * Words customers should never send us, and the model should never see.
	 */
	static final List<String> SENSITIVE_WORDS = List.of("password", "credit card number", "cvv");

	static final String SENSITIVE_REFUSAL = "Please never share passwords or card details in the chat. "
			+ "For payment problems, contact support@amigosbooks.example.";

	private final ChatClient chatClient;

	private final Resource recommendPrompt;

	private final BookstoreTools tools;

	/**
	 * Only used by {@link #chat}. Memory needs a conversation id on every request,
	 * so it's added per request instead of as a default.
	 */
	private final MessageChatMemoryAdvisor memoryAdvisor;

	public BookAssistant(ChatClient.Builder chatClientBuilder, BookCatalog catalog, ChatMemory chatMemory,
			TokenUsageTracker tokenUsageTracker, BookstoreTools tools,
			@Value("classpath:prompts/system.st") Resource systemPrompt,
			@Value("classpath:prompts/recommend.st") Resource recommendPrompt) {
		this.recommendPrompt = recommendPrompt;
		this.tools = tools;
		this.chatClient = chatClientBuilder
				.defaultSystem(system -> system
						.text(systemPrompt)
						.param("catalog", catalog.asPromptText()))
				.defaultOptions(ChatOptions.builder()
						.temperature(0.3)
						.maxTokens(500))
				.defaultAdvisors(
						new PromptInjectionGuardAdvisor(),
						SafeGuardAdvisor.builder()
							.sensitiveWords(SENSITIVE_WORDS)
							.failureResponse(SENSITIVE_REFUSAL)
							.order(Ordered.HIGHEST_PRECEDENCE + 1)
							.build(),
						new TokenUsageAdvisor(tokenUsageTracker))
				.build();
		this.memoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory).build();
	}

	/**
	 * Chats within a conversation. The model sees the earlier messages of the same conversation.
	 */
	public String chat(String conversationId, String message) {
		return chatClient.prompt()
				.user(message)
				.advisors(advisor -> advisor
						.advisors(memoryAdvisor)
						.param(ChatMemory.CONVERSATION_ID, conversationId))
				.call()
				.content();
	}

	/**
	 * The full assistant: remembers the conversation and can act for the logged-in customer.
	 */
	public String assist(String customerId, String conversationId, String message) {
		// The memory key includes the customer, so nobody can read or write into
		// someone else's conversation by guessing its id. It's turned into a UUID because
		// Spring AI's conversation_id column holds at most 36 characters.
		String memoryKey = UUID.nameUUIDFromBytes(("assistant:" + customerId + ":" + conversationId)
				.getBytes(StandardCharsets.UTF_8)).toString();
		return chatClient.prompt()
				.user(message)
				.advisors(advisor -> advisor
						.advisors(memoryAdvisor)
						.param(ChatMemory.CONVERSATION_ID, memoryKey))
				.tools(tools)
				.toolContext(Map.of(BookstoreTools.CUSTOMER_ID, customerId))
				.call()
				.content();
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
		return chatClient.prompt()
				.user(user -> user.text("Recommend one book about {topic} from the catalog.").param("topic", topic))
				.call()
				.entity(BookRecommendation.class);
	}

	/**
	 * Recommends several books as a list.
	 */
	public List<BookRecommendation> recommendMany(String topic, int maxBooks) {
		return chatClient.prompt()
				.user(user -> user
						.text("Recommend up to {maxBooks} books about {topic} from the catalog.")
						.param("topic", topic)
						.param("maxBooks", maxBooks))
				.call()
				.entity(new ParameterizedTypeReference<List<BookRecommendation>>() {
				});
	}

	/**
	 * For questions that are easy to misread. Uses the Re-Reading advisor for this request only.
	 */
	public String answerCarefully(String question) {
		return chatClient.prompt()
				.user(question)
				.advisors(new ReReadingAdvisor())
				.call()
				.content();
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
