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

	// SOLUTION-START task-4
	@GetMapping(value = "/api/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<String> stream(@RequestParam String question) {
		return bookAssistant.stream(question);
	}
	// SOLUTION-ELSE
	// TASK: // TODO task-4: expose GET /api/chat/stream?question=... as Server-Sent Events
	// SOLUTION-END

}
