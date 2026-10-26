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
			new Book("Effective Java", "Joshua Bloch", 2018, List.of("java", "best practices"), new BigDecimal("42.00"), 7,
					"Ninety items of practical advice on writing robust, clear Java: builders, generics, lambdas, streams and avoiding common mistakes."),
			new Book("Clean Code", "Robert C. Martin", 2008, List.of("craftsmanship", "refactoring"), new BigDecimal("37.50"), 3,
					"How to write code other people can read: meaningful names, small functions, comments, formatting and handling errors well."),
			new Book("Java Concurrency in Practice", "Brian Goetz", 2006, List.of("java", "concurrency"), new BigDecimal("49.99"), 0,
					"Threads, locks, thread pools and the Java memory model. How to write programs that run safely on many cores at once."),
			new Book("Designing Data-Intensive Applications", "Martin Kleppmann", 2017, List.of("distributed systems", "databases"), new BigDecimal("45.00"), 12,
					"The ideas behind databases and distributed systems: replication, partitioning, transactions, stream processing and consistency."),
			new Book("Test Driven Development: By Example", "Kent Beck", 2002, List.of("testing", "tdd"), new BigDecimal("33.00"), 4,
					"Write a failing test first, make it pass, then refactor. Worked examples that show the red, green, refactor cycle."),
			new Book("Refactoring", "Martin Fowler", 2018, List.of("refactoring", "craftsmanship"), new BigDecimal("44.00"), 5,
					"A catalogue of small, safe steps to improve the design of existing code without changing what it does."),
			new Book("Spring in Action", "Craig Walls", 2022, List.of("spring", "java"), new BigDecimal("47.99"), 9,
					"Build applications with Spring and Spring Boot: dependency injection, web apps, data access, security and REST APIs."),
			new Book("Domain-Driven Design", "Eric Evans", 2003, List.of("architecture", "design"), new BigDecimal("52.00"), 2,
					"Model complex business domains with a shared language, bounded contexts, entities, value objects and aggregates."),
			new Book("The Pragmatic Programmer", "David Thomas, Andrew Hunt", 2019, List.of("craftsmanship", "career"), new BigDecimal("39.95"), 6,
					"Habits of effective developers: taking responsibility, avoiding duplication, automation, estimating and growing your career."),
			new Book("Head First Design Patterns", "Eric Freeman, Elisabeth Robson", 2020, List.of("design patterns", "java"), new BigDecimal("41.00"), 8,
					"A visual, beginner friendly tour of the classic design patterns like strategy, observer, decorator and factory."));

	public List<Book> findAll() {
		return books;
	}

	public Optional<Book> findByTitle(String title) {
		return books.stream().filter(b -> b.title().equalsIgnoreCase(title)).findFirst();
	}

	/**
	 * The catalog as plain text, one book per line, ready to paste into a prompt.
	 * Stock changes with every order, so it's not in here: the model asks the checkStock tool.
	 */
	public String asPromptText() {
		return books.stream()
			.map(b -> "- %s by %s (%d). Topics: %s. Price: $%s".formatted(b.title(), b.author(), b.year(),
					String.join(", ", b.topics()), b.price()))
			.collect(Collectors.joining("\n"));
	}

}
