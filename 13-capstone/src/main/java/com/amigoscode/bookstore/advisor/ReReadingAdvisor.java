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
		String question = request.prompt().getUserMessage().getText();
		String reRead = question + "\nRead the question again: " + question;
		return request.mutate().prompt(request.prompt().augmentUserMessage(reRead)).build();
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
