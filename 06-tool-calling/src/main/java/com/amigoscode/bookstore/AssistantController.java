package com.amigoscode.bookstore;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AssistantController {

	private final BookAssistant bookAssistant;

	public AssistantController(BookAssistant bookAssistant) {
		this.bookAssistant = bookAssistant;
	}

	/**
	 * In a real app the customer id comes from the logged-in user (Spring Security),
	 * never from the request body the model or the user controls. Here a header stands in for it.
	 */
	@PostMapping("/api/assistant/{conversationId}")
	public String assist(@RequestHeader("X-Customer-Id") String customerId, @PathVariable String conversationId,
			@RequestBody String message) {
		return bookAssistant.assist(customerId, conversationId, message);
	}

}
