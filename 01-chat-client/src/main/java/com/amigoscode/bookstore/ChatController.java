package com.amigoscode.bookstore;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class ChatController {

	private final BookAssistant bookAssistant;

	public ChatController(BookAssistant bookAssistant) {
		this.bookAssistant = bookAssistant;
	}

	@GetMapping("/api/chat")
	public BookAssistant.Answer chat(@RequestParam String question) {
		return bookAssistant.askWithUsage(question);
	}

	@GetMapping("/api/blurb")
	public String blurb(@RequestParam String title) {
		return bookAssistant.writeBlurb(title);
	}

	// TODO task-4: expose GET /api/chat/stream?question=... as Server-Sent Events

}
