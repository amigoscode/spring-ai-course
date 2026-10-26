package com.amigoscode.bookstore.agents;

import com.amigoscode.bookstore.BookAssistant;
import com.amigoscode.bookstore.policy.PolicyAssistant;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.stereotype.Service;

/**
 * Pattern 2: routing. A cheap first call classifies the message, then a specialised
 * handler deals with it. Each handler can have its own prompt, tools and model.
 *
 *                       ┌─► RECOMMENDATION ─► BookAssistant.recommend
 * message ─► classify ──┼─► POLICY ─────────► PolicyAssistant.answer
 *                       └─► OTHER ──────────► a polite fixed answer, no model call
 */
@Service
public class SupportRouter {

	static final String OTHER_ANSWER = "I can help with book recommendations and questions about orders, shipping, returns and payments.";

	private final ChatClient chatClient;

	private final BookAssistant bookAssistant;

	private final PolicyAssistant policyAssistant;

	public SupportRouter(ChatClient.Builder chatClientBuilder, BookAssistant bookAssistant,
			PolicyAssistant policyAssistant) {
		this.chatClient = chatClientBuilder.clone().defaultOptions(ChatOptions.builder().temperature(0.0)).build();
		this.bookAssistant = bookAssistant;
		this.policyAssistant = policyAssistant;
	}

	public Routed handle(String message) {
		// TODO task-2: classify the message into a RouteDecision (structured output),
		// then send it to the right handler. OTHER gets OTHER_ANSWER without another model call.
		throw new UnsupportedOperationException("TODO task-2");
	}

	public enum Route {

		RECOMMENDATION, POLICY, OTHER

	}

	public record RouteDecision(Route route, String reason) {
	}

	public record Routed(Route route, String answer) {
	}

}
