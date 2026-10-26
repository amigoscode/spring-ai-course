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

	@Tool(description = "Check how many copies of a book are in stock right now. Use the exact catalog title.")
	public StockInfo checkStock(@ToolParam(description = "The exact title of the book") String title) {
		int copies = orderService.stockOf(title);
		return new StockInfo(title, copies > 0, copies);
	}

	@Tool(description = "Find the books in the catalog about a topic, such as java, testing or architecture")
	public List<Book> findBooksByTopic(@ToolParam(description = "One topic, in lower case") String topic) {
		return catalog.findAll()
			.stream()
			.filter(book -> book.topics().stream().anyMatch(t -> t.contains(topic.toLowerCase())))
			.toList();
	}

	@Tool(description = "Place an order for the customer who is chatting. Only call this after the customer clearly confirmed the book and the quantity.")
	public String placeOrder(@ToolParam(description = "The exact title of the book") String title,
			@ToolParam(description = "How many copies, from 1 to 10") int quantity, ToolContext toolContext) {
		String customerId = (String) toolContext.getContext().get(CUSTOMER_ID);
		Order order = orderService.placeOrder(customerId, title, quantity);
		return "Order " + order.id() + " placed: " + order.quantity() + " x " + order.bookTitle() + ", total $"
				+ order.total();
	}

	@Tool(description = "Get the status of one of the customer's own orders")
	public String orderStatus(@ToolParam(description = "The order id, like ORD-1001") String orderId,
			ToolContext toolContext) {
		String customerId = (String) toolContext.getContext().get(CUSTOMER_ID);
		return orderService.findOrder(orderId)
			.filter(order -> order.customerId().equals(customerId))
			.map(order -> "Order " + order.id() + ": " + order.quantity() + " x " + order.bookTitle()
					+ ", total $" + order.total() + ", status: preparing for shipment")
			.orElse("No order " + orderId + " found for this customer");
	}

	public record StockInfo(String title, boolean inStock, int copies) {
	}

}
