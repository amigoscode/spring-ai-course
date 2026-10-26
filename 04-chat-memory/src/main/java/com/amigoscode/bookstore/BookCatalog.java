package com.amigoscode.bookstore;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

/**
 * The books Amigos Books sells. In later modules this moves into Postgres.
 */
@Component
public class BookCatalog {

	private final List<Book> books = List.of(
			new Book("Effective Java", "Joshua Bloch", 2018, List.of("java", "best practices"), new BigDecimal("42.00"), 7),
			new Book("Clean Code", "Robert C. Martin", 2008, List.of("craftsmanship", "refactoring"), new BigDecimal("37.50"), 3),
			new Book("Java Concurrency in Practice", "Brian Goetz", 2006, List.of("java", "concurrency"), new BigDecimal("49.99"), 0),
			new Book("Designing Data-Intensive Applications", "Martin Kleppmann", 2017, List.of("distributed systems", "databases"), new BigDecimal("45.00"), 12),
			new Book("Test Driven Development: By Example", "Kent Beck", 2002, List.of("testing", "tdd"), new BigDecimal("33.00"), 4),
			new Book("Refactoring", "Martin Fowler", 2018, List.of("refactoring", "craftsmanship"), new BigDecimal("44.00"), 5),
			new Book("Spring in Action", "Craig Walls", 2022, List.of("spring", "java"), new BigDecimal("47.99"), 9),
			new Book("Domain-Driven Design", "Eric Evans", 2003, List.of("architecture", "design"), new BigDecimal("52.00"), 2),
			new Book("The Pragmatic Programmer", "David Thomas, Andrew Hunt", 2019, List.of("craftsmanship", "career"), new BigDecimal("39.95"), 6),
			new Book("Head First Design Patterns", "Eric Freeman, Elisabeth Robson", 2020, List.of("design patterns", "java"), new BigDecimal("41.00"), 8));

	public List<Book> findAll() {
		return books;
	}

	public Optional<Book> findByTitle(String title) {
		return books.stream().filter(b -> b.title().equalsIgnoreCase(title)).findFirst();
	}

	/**
	 * The catalog as plain text, one book per line, ready to paste into a prompt.
	 */
	public String asPromptText() {
		return books.stream()
			.map(b -> "- %s by %s (%d). Topics: %s. Price: $%s. In stock: %d".formatted(b.title(), b.author(),
					b.year(), String.join(", ", b.topics()), b.price(), b.stock()))
			.collect(Collectors.joining("\n"));
	}

}
