package com.amigoscode.bookstore;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.DefaultUsage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;

/**
 * A ChatModel that never calls a real LLM. It records every prompt it receives
 * and answers with a fixed reply, so tests are fast, free and offline.
 */
public class FakeChatModel implements ChatModel {

	public static final int PROMPT_TOKENS = 12;

	public static final int COMPLETION_TOKENS = 34;

	private final List<Prompt> prompts = new ArrayList<>();

	private final Deque<String> replies = new ArrayDeque<>(List.of("Fake answer from the model"));

	/**
	 * The model answers with these replies in order. The last one repeats forever.
	 * Also forgets the prompts recorded so far, so each test starts clean.
	 */
	public FakeChatModel reply(String... replies) {
		this.prompts.clear();
		this.replies.clear();
		this.replies.addAll(List.of(replies));
		return this;
	}

	private String nextReply() {
		return replies.size() > 1 ? replies.removeFirst() : replies.getFirst();
	}

	public List<Prompt> prompts() {
		return prompts;
	}

	public Prompt lastPrompt() {
		if (prompts.isEmpty()) {
			throw new AssertionError("The model was never called");
		}
		return prompts.getLast();
	}

	@Override
	public ChatResponse call(Prompt prompt) {
		prompts.add(prompt);
		return response(nextReply());
	}

	@Override
	public Flux<ChatResponse> stream(Prompt prompt) {
		prompts.add(prompt);
		return Flux.fromIterable(Arrays.asList(nextReply().split("(?<= )"))).map(FakeChatModel::response);
	}

	private static ChatResponse response(String text) {
		ChatResponseMetadata metadata = ChatResponseMetadata.builder()
			.model("fake-model")
			.usage(new DefaultUsage(PROMPT_TOKENS, COMPLETION_TOKENS))
			.build();
		return new ChatResponse(List.of(new Generation(new AssistantMessage(text))), metadata);
	}

}
