package com.amigoscode.bookstore.advisor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.core.Ordered;

/**
 * Records the token usage of every call in the {@link TokenUsageTracker}.
 */
public class TokenUsageAdvisor implements CallAdvisor {

	private static final Logger log = LoggerFactory.getLogger(TokenUsageAdvisor.class);

	private final TokenUsageTracker tracker;

	public TokenUsageAdvisor(TokenUsageTracker tracker) {
		this.tracker = tracker;
	}

	@Override
	public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
		// TODO task-1: let the call go through, then record its token usage in the tracker
		return chain.nextCall(request);
	}

	@Override
	public String getName() {
		return "TokenUsageAdvisor";
	}

	/**
	 * Runs last on the way in and first on the way out, right next to the model.
	 */
	@Override
	public int getOrder() {
		return Ordered.LOWEST_PRECEDENCE - 1;
	}

}
