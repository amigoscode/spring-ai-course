package com.amigoscode.bookstore.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.amigoscode.bookstore.Book;
import com.amigoscode.bookstore.BookCatalog;
import org.springframework.stereotype.Service;

/**
 * A plain Spring service. Nothing in it knows about AI: the model reaches it through tools.
 */
@Service
public class OrderService {

	private final BookCatalog catalog;

	private final Map<String, Integer> stock = new ConcurrentHashMap<>();

	private final Map<String, Order> orders = new ConcurrentHashMap<>();

	private final AtomicInteger nextId = new AtomicInteger(1000);

	public OrderService(BookCatalog catalog) {
		this.catalog = catalog;
		reset();
	}

	public int stockOf(String title) {
		return stock.getOrDefault(normalise(title), 0);
	}

	public synchronized Order placeOrder(String customerId, String title, int quantity) {
		Book book = catalog.findByTitle(title)
			.orElseThrow(() -> new IllegalArgumentException("We don't sell a book called " + title));
		if (quantity < 1 || quantity > 10) {
			throw new IllegalArgumentException("You can order between 1 and 10 copies");
		}
		int available = stockOf(book.title());
		if (quantity > available) {
			throw new IllegalArgumentException(
					"Not enough stock for " + book.title() + ": " + available + " left, " + quantity + " asked");
		}
		stock.put(normalise(book.title()), available - quantity);
		String id = "ORD-" + nextId.incrementAndGet();
		Order order = new Order(id, customerId, book.title(), quantity,
				book.price().multiply(BigDecimal.valueOf(quantity)), Instant.now());
		orders.put(id, order);
		return order;
	}

	public Optional<Order> findOrder(String orderId) {
		return Optional.ofNullable(orders.get(orderId));
	}

	public List<Order> ordersOf(String customerId) {
		return orders.values().stream().filter(o -> o.customerId().equals(customerId)).toList();
	}

	/**
	 * Back to the stock levels of the catalog, with no orders. Used by tests.
	 */
	public void reset() {
		orders.clear();
		stock.clear();
		catalog.findAll().forEach(book -> stock.put(normalise(book.title()), book.stock()));
	}

	private static String normalise(String title) {
		return title.toLowerCase().strip();
	}

}
