package com.amigoscode.bookstore;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatController {

	private final BookAssistant bookAssistant;

	public ChatController(BookAssistant bookAssistant) {
		this.bookAssistant = bookAssistant;
	}

	// TODO task-5: expose GET /api/chat?question=... that returns BookAssistant.Answer as JSON

}
