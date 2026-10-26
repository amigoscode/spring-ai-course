package com.amigoscode.bookstore.gifts;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.amigoscode.bookstore.Book;
import com.amigoscode.bookstore.BookCatalog;
import com.amigoscode.bookstore.order.OrderService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.stereotype.Service;

/**
 * The capstone feature. Read 13-capstone/TASKS.md: the design is up to you.
 */
@Service
public class GiftFinder {

	public static final int MAX_SUGGESTIONS = 3;

	// SOLUTION-START capstone
	private final ChatClient chatClient;

	private final BookCatalog catalog;

	private final OrderService orderService;

	public GiftFinder(ChatClient.Builder chatClientBuilder, BookCatalog catalog, OrderService orderService) {
		this.chatClient = chatClientBuilder.clone()
			.defaultSystem("""
					You help customers of Amigos Books, a bookstore for software developers, choose books as gifts.
					Only suggest books from the catalog you are given, using their exact titles.""")
			.defaultOptions(ChatOptions.builder().temperature(0.3))
			.build();
		this.catalog = catalog;
		this.orderService = orderService;
	}

	public GiftSuggestions find(GiftRequest request) {
		// Only show the model books it may actually suggest: in stock and within the budget.
		List<Book> candidates = catalog.findAll()
			.stream()
			.filter(book -> orderService.stockOf(book.title()) > 0)
			.filter(book -> book.price().compareTo(request.budget()) <= 0)
			.toList();
		if (candidates.isEmpty()) {
			return new GiftSuggestions(List.of(), BigDecimal.ZERO,
					"Sorry, we have nothing in stock within that budget. Could you stretch it a little?");
		}

		ModelPicks picks = chatClient.prompt()
			.user(u -> u.text("""
					Suggest 1 to {max} books as a gift.
					Recipient: {recipient}
					Occasion: {occasion}
					Budget for all books together: ${budget}
					For each book give the exact title and one sentence on why it suits this person.
					Also write a short, friendly message for the customer.

					Books you can choose from (title, author, topics, price):
					{candidates}""")
				.param("max", MAX_SUGGESTIONS)
				.param("recipient", request.recipient())
				.param("occasion", request.occasion() == null ? "not given" : request.occasion())
				.param("budget", request.budget())
				.param("candidates", candidates.stream()
					.map(b -> "- %s by %s (%s) $%s".formatted(b.title(), b.author(), String.join(", ", b.topics()), b.price()))
					.reduce((a, b) -> a + "\n" + b)
					.orElse("")))
			.call()
			.entity(ModelPicks.class);

		return verify(picks, request.budget());
	}

	/**
	 * Trust, but verify: the model's picks are a suggestion. Prices, stock and the budget come
	 * from our own data, and anything that doesn't check out is dropped.
	 */
	GiftSuggestions verify(ModelPicks picks, BigDecimal budget) {
		List<GiftSuggestions.Suggestion> suggestions = new ArrayList<>();
		BigDecimal total = BigDecimal.ZERO;
		for (ModelPick pick : picks.picks() == null ? List.<ModelPick>of() : picks.picks()) {
			Optional<Book> book = catalog.findByTitle(pick.title());
			if (book.isEmpty() || orderService.stockOf(book.get().title()) <= 0
					|| suggestions.stream().anyMatch(s -> s.title().equals(book.get().title()))) {
				continue;
			}
			BigDecimal newTotal = total.add(book.get().price());
			if (newTotal.compareTo(budget) > 0 || suggestions.size() == MAX_SUGGESTIONS) {
				continue;
			}
			suggestions.add(new GiftSuggestions.Suggestion(book.get().title(), book.get().author(), book.get().price(),
					pick.reason()));
			total = newTotal;
		}
		String message = suggestions.isEmpty()
				? "Sorry, I couldn't find a good match within that budget. Could you tell me more about them?"
				: picks.message();
		return new GiftSuggestions(suggestions, total, message);
	}

	record ModelPicks(List<ModelPick> picks, String message) {
	}

	record ModelPick(String title, String reason) {
	}
	// SOLUTION-ELSE
	// TASK: public GiftSuggestions find(GiftRequest request) {
	// TASK: 	// TODO capstone: this is yours to design. See TASKS.md for what it must do.
	// TASK: 	throw new UnsupportedOperationException("TODO capstone");
	// TASK: }
	// SOLUTION-END

}
