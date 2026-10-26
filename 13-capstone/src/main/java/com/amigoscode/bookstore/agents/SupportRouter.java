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
		RouteDecision decision = chatClient.prompt()
			.system("""
					Classify the customer's message for a bookstore:
					RECOMMENDATION: they want book suggestions
					POLICY: shipping, delivery, returns, refunds, payments, gift cards or loyalty points
					OTHER: anything else""")
			.user(message)
			.call()
			.entity(RouteDecision.class);

		String answer = switch (decision.route()) {
			case RECOMMENDATION -> bookAssistant.recommend(message, 3);
			case POLICY -> policyAssistant.answer(message);
			case OTHER -> OTHER_ANSWER;
		};
		return new Routed(decision.route(), answer);
	}

	public enum Route {

		RECOMMENDATION, POLICY, OTHER

	}

	public record RouteDecision(Route route, String reason) {
	}

	public record Routed(Route route, String answer) {
	}

}
