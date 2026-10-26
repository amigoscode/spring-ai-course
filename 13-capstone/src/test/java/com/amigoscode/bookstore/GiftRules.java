package com.amigoscode.bookstore;

import java.math.BigDecimal;

import com.amigoscode.bookstore.gifts.GiftFinder;
import com.amigoscode.bookstore.gifts.GiftRequest;
import com.amigoscode.bookstore.gifts.GiftSuggestions;
import com.amigoscode.bookstore.order.OrderService;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The rules every answer must follow, whatever the model said. These are not about taste,
 * they're about money and stock, so they must hold 100% of the time.
 */
final class GiftRules {

	private GiftRules() {
	}

	static void check(GiftSuggestions result, GiftRequest request, BookCatalog catalog, OrderService orderService) {
		assertThat(result.suggestions()).as("at most %d books", GiftFinder.MAX_SUGGESTIONS)
			.hasSizeLessThanOrEqualTo(GiftFinder.MAX_SUGGESTIONS);
		assertThat(result.message()).as("a message for the customer").isNotBlank();

		BigDecimal sum = BigDecimal.ZERO;
		for (var suggestion : result.suggestions()) {
			var book = catalog.findByTitle(suggestion.title());
			assertThat(book).as("%s is in the catalog", suggestion.title()).isPresent();
			assertThat(orderService.stockOf(suggestion.title())).as("%s is in stock", suggestion.title()).isPositive();
			assertThat(suggestion.price()).as("the price of %s comes from the catalog", suggestion.title())
				.isEqualByComparingTo(book.get().price());
			assertThat(suggestion.author()).isEqualTo(book.get().author());
			assertThat(suggestion.reason()).as("a reason for %s", suggestion.title()).isNotBlank();
			sum = sum.add(book.get().price());
		}
		assertThat(result.suggestions()).extracting(GiftSuggestions.Suggestion::title).doesNotHaveDuplicates();
		assertThat(result.total()).as("the total is the sum of the prices").isEqualByComparingTo(sum);
		assertThat(result.total()).as("within the budget").isLessThanOrEqualTo(request.budget());
	}

}
