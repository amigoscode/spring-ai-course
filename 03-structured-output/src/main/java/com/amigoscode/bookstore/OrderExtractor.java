package com.amigoscode.bookstore;

import java.util.Set;
import java.util.stream.Collectors;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.stereotype.Service;

/**
 * Turns messy customer emails into orders the shop can process.
 */
@Service
public class OrderExtractor {

	private static final String INSTRUCTIONS = """
			Extract the book order from the customer's email.
			Use the exact book title from the catalog when the customer's spelling is close.
			If a field is missing from the email, leave it empty.
			""";

	private final ChatClient chatClient;

	private final BookCatalog catalog;

	private final Validator validator;

	public OrderExtractor(ChatClient.Builder chatClientBuilder, BookCatalog catalog, Validator validator) {
		this.chatClient = chatClientBuilder
				.defaultSystem(INSTRUCTIONS + "\nCatalog:\n" + catalog.asPromptText())
				.defaultOptions(ChatOptions.builder().temperature(0.0))
				.build();
		this.catalog = catalog;
		this.validator = validator;
	}

	/**
	 * Asks the model for an {@link OrderRequest}. If the answer doesn't match the schema,
	 * Spring AI sends the error back to the model and tries again.
	 */
	public OrderRequest extract(String email) {
		// SOLUTION-START task-3
		return chatClient.prompt()
				.user(email)
				.call()
				.entity(OrderRequest.class, spec -> spec.validateSchema());
		// SOLUTION-ELSE
		// TASK: // TODO task-3: return the email as an OrderRequest, and let Spring AI
		// TASK: // retry when the answer doesn't match the JSON schema
		// TASK: throw new UnsupportedOperationException("TODO task-3");
		// SOLUTION-END
	}

	/**
	 * Valid JSON is not the same as a valid order. Check the business rules before
	 * the order goes anywhere near the warehouse.
	 */
	public OrderRequest extractValidOrder(String email) {
		OrderRequest order = extract(email);
		// SOLUTION-START task-4
		Set<ConstraintViolation<OrderRequest>> violations = validator.validate(order);
		if (!violations.isEmpty()) {
			String problems = violations.stream()
				.map(v -> v.getPropertyPath() + " " + v.getMessage())
				.sorted()
				.collect(Collectors.joining(", "));
			throw new InvalidOrderException("Invalid order: " + problems);
		}
		if (catalog.findByTitle(order.bookTitle()).isEmpty()) {
			throw new InvalidOrderException("We don't sell: " + order.bookTitle());
		}
		return order;
		// SOLUTION-ELSE
		// TASK: // TODO task-4: validate the order with the Validator (the annotations on OrderRequest)
		// TASK: // and check the book is in the catalog. Throw InvalidOrderException if not.
		// TASK: return order;
		// SOLUTION-END
	}

}
