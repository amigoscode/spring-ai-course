package com.amigoscode.bookstore.advisor;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.core.Ordered;

/**
 * Blocks the most common prompt injection attempts before they reach the model.
 * A pattern list is a first line of defence, not a complete one: see the README.
 */
public class PromptInjectionGuardAdvisor implements CallAdvisor {

	private static final Logger log = LoggerFactory.getLogger(PromptInjectionGuardAdvisor.class);

	public static final String REFUSAL = "I can only help with questions about books and your orders.";

	static final List<Pattern> PATTERNS = List.of(
			Pattern.compile("ignore (all |any |the )?(previous|prior|above|your) (instructions|rules|prompt)", Pattern.CASE_INSENSITIVE),
			Pattern.compile("(reveal|show|print|repeat) (me )?(your|the) (system )?prompt", Pattern.CASE_INSENSITIVE),
			Pattern.compile("you are now (?!talking)", Pattern.CASE_INSENSITIVE));

	@Override
	public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
		// TODO task-3: if the user message matches one of the PATTERNS, don't call the model.
		// Return a ChatClientResponse whose answer is REFUSAL instead.
		return chain.nextCall(request);
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
