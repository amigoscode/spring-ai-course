package com.amigoscode.bookstore;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

	@GetMapping(value = "/api/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<String> stream(@RequestParam String question) {
		return bookAssistant.stream(question);
	}

	@GetMapping("/api/recommendations")
	public String recommend(@RequestParam String topic, @RequestParam(defaultValue = "3") int max) {
		return bookAssistant.recommend(topic, max);
	}

	@PostMapping("/api/reviews/sentiment")
	public Sentiment sentiment(@RequestBody String review) {
		return bookAssistant.classify(review);
	}

}
