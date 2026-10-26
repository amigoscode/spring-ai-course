package com.amigoscode.bookstore.tools;

import java.util.List;

import com.amigoscode.bookstore.Book;
import com.amigoscode.bookstore.BookCatalog;
import com.amigoscode.bookstore.order.Order;
import com.amigoscode.bookstore.order.OrderService;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * The actions the assistant may take. The model reads each description to decide when
 * to call a tool, so write them like documentation for a new colleague.
 */
@Component
public class BookstoreTools {

	/**
	 * The key under which the logged-in customer travels in the {@link ToolContext}.
	 */
	public static final String CUSTOMER_ID = "customerId";

	private final BookCatalog catalog;

	private final OrderService orderService;

	public BookstoreTools(BookCatalog catalog, OrderService orderService) {
		this.catalog = catalog;
		this.orderService = orderService;
	}

	// TODO task-1: turn this into a tool (@Tool and @ToolParam, with descriptions)
	// and return the copies in stock from orderService.stockOf(title)
	public StockInfo checkStock(String title) {
		throw new UnsupportedOperationException("TODO task-1");
	}

	// TODO task-1: turn this into a tool, and return the catalog books with a topic containing the given one
	public List<Book> findBooksByTopic(String topic) {
		throw new UnsupportedOperationException("TODO task-1");
	}

	// TODO task-3: add a placeOrder(title, quantity, ToolContext) tool. Take the customer id
	// from the ToolContext, never from the model, and call orderService.placeOrder.

	// TODO task-4: add an orderStatus(orderId, ToolContext) tool that only shows
	// the logged-in customer's own orders

	public record StockInfo(String title, boolean inStock, int copies) {
	}

}
