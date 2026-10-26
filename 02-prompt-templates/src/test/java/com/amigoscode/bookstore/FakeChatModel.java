package com.amigoscode.bookstore;

import java.util.ArrayList;
import java.util.Arrays;
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

	private String reply = "Fake answer from the model";

	public FakeChatModel reply(String reply) {
		this.reply = reply;
		return this;
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
		return response(reply);
	}

	@Override
	public Flux<ChatResponse> stream(Prompt prompt) {
		prompts.add(prompt);
		return Flux.fromIterable(Arrays.asList(reply.split("(?<= )"))).map(FakeChatModel::response);
	}

	private static ChatResponse response(String text) {
		ChatResponseMetadata metadata = ChatResponseMetadata.builder()
			.model("fake-model")
			.usage(new DefaultUsage(PROMPT_TOKENS, COMPLETION_TOKENS))
			.build();
		return new ChatResponse(List.of(new Generation(new AssistantMessage(text))), metadata);
	}

}
