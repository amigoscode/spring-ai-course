package com.amigoscode.bookstore.advisor;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.core.Ordered;
import reactor.core.publisher.Flux;

/**
 * Blocks the most common prompt injection attempts before they reach the model.
 * A pattern list is a first line of defence, not a complete one: see the README.
 * It guards both normal and streaming requests, or the stream endpoint would be a way around it.
 */
public class PromptInjectionGuardAdvisor implements CallAdvisor, StreamAdvisor {

	private static final Logger log = LoggerFactory.getLogger(PromptInjectionGuardAdvisor.class);

	public static final String REFUSAL = "I can only help with questions about books and your orders.";

	static final List<Pattern> PATTERNS = List.of(
			Pattern.compile("ignore (all |any |the )?(previous|prior|above|your) (instructions|rules|prompt)", Pattern.CASE_INSENSITIVE),
			Pattern.compile("(reveal|show|print|repeat) (me )?(your|the) (system )?prompt", Pattern.CASE_INSENSITIVE),
			Pattern.compile("you are now (?!talking)", Pattern.CASE_INSENSITIVE));

	@Override
	public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
		// TODO task-3: if isInjection(request), don't call the model: return refusal(request)
		return chain.nextCall(request);
	}

	@Override
	public Flux<ChatClientResponse> adviseStream(ChatClientRequest request, StreamAdvisorChain chain) {
		if (isInjection(request)) {
			return Flux.just(refusal(request));
		}
		return chain.nextStream(request);
	}

	boolean isInjection(ChatClientRequest request) {
		// TODO task-3: return true if the user message matches one of the PATTERNS.
		// Log which pattern matched, but not the message itself.
		return false;
	}

	ChatClientResponse refusal(ChatClientRequest request) {
		ChatResponse refusal = ChatResponse.builder()
			.generations(List.of(new Generation(new AssistantMessage(REFUSAL))))
			.build();
		return ChatClientResponse.builder().chatResponse(refusal).context(Map.copyOf(request.context())).build();
	}

	@Override
	public String getName() {
		return "PromptInjectionGuardAdvisor";
	}

	/**
	 * Runs first, before memory, so blocked messages are never saved in the conversation.
	 */
	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE;
	}

}
