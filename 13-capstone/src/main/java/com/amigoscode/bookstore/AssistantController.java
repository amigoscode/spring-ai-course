package com.amigoscode.bookstore;

import java.security.Principal;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AssistantController {

	private final BookAssistant bookAssistant;

	public AssistantController(BookAssistant bookAssistant) {
		this.bookAssistant = bookAssistant;
	}

	/**
	 * The customer is whoever is logged in. Nothing the caller sends, and nothing the
	 * model says, can change that.
	 */
	@PostMapping("/api/assistant/{conversationId}")
	public String assist(Principal principal, @PathVariable String conversationId, @RequestBody String message) {
		return bookAssistant.assist(principal.getName(), conversationId, message);
	}

}
