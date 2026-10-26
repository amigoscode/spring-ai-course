package com.amigoscode.bookstore;

import java.math.BigDecimal;
import java.util.List;

public record Book(String title, String author, int year, List<String> topics, BigDecimal price, int stock) {
}
