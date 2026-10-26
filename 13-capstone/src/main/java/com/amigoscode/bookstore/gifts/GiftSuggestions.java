package com.amigoscode.bookstore.gifts;

import java.math.BigDecimal;
import java.util.List;

/**
 * The contract. Don't change it: the acceptance tests use it.
 *
 * @param suggestions at most 3 books, all in the catalog, all in stock, together within the budget
 * @param total       the sum of the suggested books' catalog prices
 * @param message     a short, friendly note for the customer
 */
public record GiftSuggestions(List<Suggestion> suggestions, BigDecimal total, String message) {

	public record Suggestion(String title, String author, BigDecimal price, String reason) {
	}

}
