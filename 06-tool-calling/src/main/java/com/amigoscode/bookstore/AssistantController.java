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
	 * WARNING: a header anyone can set stands in for the logged-in user, to keep this module
	 * small. In a real app the customer id comes from Spring Security (module 12 does this),
	 * never from anything the caller or the model controls.
	 */
	@PostMapping("/api/assistant/{conversationId}")
	public String assist(@RequestHeader("X-Customer-Id") String customerId, @PathVariable String conversationId,
			@RequestBody String message) {
		return bookAssistant.assist(customerId, conversationId, message);
	}

}
