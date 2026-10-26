package com.amigoscode.bookstore.order;

import java.math.BigDecimal;
import java.time.Instant;

public record Order(String id, String customerId, String bookTitle, int quantity, BigDecimal total,
		Instant placedAt) {
}
