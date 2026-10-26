package com.amigoscode.bookstore;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.UUID;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.DefaultUsage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import reactor.core.publisher.Flux;

/**
 * A ChatModel that never calls a real LLM. It records every prompt it receives
 * and answers with scripted replies, so tests are fast, free and offline.
 * It can also script tool calls, so the real Spring AI tool loop runs in tests.
 * Thread safe, for the parallel workflow.
 */
public class FakeChatModel implements ChatModel {

	public static final int PROMPT_TOKENS = 12;

	public static final int COMPLETION_TOKENS = 34;

	private final List<Prompt> prompts = new ArrayList<>();

	private RuntimeException failure;

	private final Deque<AssistantMessage> replies = new ArrayDeque<>(List.of(text("Fake answer from the model")));

	/**
	 * The model answers with these texts in order. The last one repeats forever.
	 * Also forgets the prompts recorded so far, so each test starts clean.
	 */
	public FakeChatModel reply(String... replies) {
		return script(Arrays.stream(replies).map(FakeChatModel::text).toArray(AssistantMessage[]::new));
	}

	/**
	 * Like {@link #reply(String...)}, but each reply can be a {@link #toolCall} or a {@link #text}.
	 */
	public synchronized FakeChatModel script(AssistantMessage... replies) {
		this.failure = null;
		this.prompts.clear();
		this.replies.clear();
		this.replies.addAll(List.of(replies));
		return this;
	}

	/**
	 * Every call fails with this exception, like a model that is down or out of credit.
	 */
	public synchronized FakeChatModel failWith(RuntimeException failure) {
		this.prompts.clear();
		this.failure = failure;
		return this;
	}

	/**
	 * A reply in which the model asks Spring AI to run a tool.
	 */
	public static AssistantMessage toolCall(String toolName, String argumentsJson) {
		var call = new AssistantMessage.ToolCall("call-" + UUID.randomUUID(), "function", toolName, argumentsJson);
		return AssistantMessage.builder().content("").toolCalls(List.of(call)).build();
	}

	public static AssistantMessage text(String text) {
		return new AssistantMessage(text);
	}

	public synchronized List<Prompt> prompts() {
		return List.copyOf(prompts);
	}

	public synchronized Prompt lastPrompt() {
		if (prompts.isEmpty()) {
			throw new AssertionError("The model was never called");
		}
		return prompts.getLast();
	}

	private AssistantMessage nextReply() {
		return replies.size() > 1 ? replies.removeFirst() : replies.getFirst();
	}

	/**
	 * Tool calling options, so Spring AI's tool calling advisor runs the tool loop.
	 */
	@Override
	public ChatOptions getOptions() {
		return ToolCallingChatOptions.builder().build();
	}

	@Override
	public synchronized ChatResponse call(Prompt prompt) {
		prompts.add(prompt);
		if (failure != null) {
			throw failure;
		}
		return response(nextReply());
	}

	@Override
	public synchronized Flux<ChatResponse> stream(Prompt prompt) {
		prompts.add(prompt);
		String text = nextReply().getText();
		return Flux.fromIterable(Arrays.asList(text.split("(?<= )"))).map(piece -> response(text(piece)));
	}

	private static ChatResponse response(AssistantMessage message) {
		ChatResponseMetadata metadata = ChatResponseMetadata.builder()
			.model("fake-model")
			.usage(new DefaultUsage(PROMPT_TOKENS, COMPLETION_TOKENS))
			.build();
		return new ChatResponse(List.of(new Generation(message)), metadata);
	}

}
