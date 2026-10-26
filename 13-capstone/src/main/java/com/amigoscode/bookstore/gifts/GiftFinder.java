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

	public GiftSuggestions find(GiftRequest request) {
		// TODO capstone: this is yours to design. See TASKS.md for what it must do.
		throw new UnsupportedOperationException("TODO capstone");
	}

}
