package com.amigoscode.bookstore;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {

	private final OrderExtractor orderExtractor;

	public OrderController(OrderExtractor orderExtractor) {
		this.orderExtractor = orderExtractor;
	}

	/**
	 * Paste a customer email as the request body and get a validated order back.
	 */
	@PostMapping("/api/orders/extract")
	public OrderRequest extract(@RequestBody String email) {
		return orderExtractor.extractValidOrder(email);
	}

	@ExceptionHandler(InvalidOrderException.class)
	ProblemDetail invalidOrder(InvalidOrderException e) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, e.getMessage());
	}

}
