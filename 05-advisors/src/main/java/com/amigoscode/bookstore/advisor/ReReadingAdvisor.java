package com.amigoscode.bookstore.advisor;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;

/**
 * Re-Reading (Re2): repeating the question makes models answer tricky questions more
 * accurately, at the cost of more prompt tokens. See https://arxiv.org/abs/2309.06275
 */
public class ReReadingAdvisor implements BaseAdvisor {

	@Override
	public ChatClientRequest before(ChatClientRequest request, AdvisorChain chain) {
		// TODO task-4: change the user message to "<question>\nRead the question again: <question>"
		return request;
	}

	@Override
	public ChatClientResponse after(ChatClientResponse response, AdvisorChain chain) {
		return response;
	}

	@Override
	public int getOrder() {
		return 0;
	}

}
